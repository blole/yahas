package datanode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import common.BlockReport;
import common.Constants;
import common.RMIHelper;
import common.exceptions.BlockAlreadyExistsException;
import common.exceptions.BlockNotFoundException;
import common.protocols.DataNodeNameNodeProtocol;
import common.protocols.RemoteBlock;
import common.protocols.RemoteDataNode;

public class DataNode implements RemoteDataNode {

	private static final String BASE_BLOCK_PATH = "blocks/";
	public final String pathBaseDir;

	private static final Logger LOGGER = Logger.getLogger(DataNode.class
			.getCanonicalName());

	public final int id;
	public final BlockManager blocks;
	private HeartBeatSender heartBeatSender;
	private DataNodeNameNodeProtocol nameNode;

	public DataNode(int id, String baseDir, DataNodeNameNodeProtocol nameNode,
			InetSocketAddress nameNodeHeartBeatSocketAddress) {
		this.id = id;
		this.nameNode = nameNode;
		this.pathBaseDir = baseDir;
		this.blocks = new BlockManager(baseDir + BASE_BLOCK_PATH);

		try {
			heartBeatSender = new HeartBeatSender(
					nameNodeHeartBeatSocketAddress,
					Constants.DEFAULT_HEARTBEAT_INTERVAL_MS, id);
		} catch (SocketException e) {
			System.err.printf("Could not create HeartBeat sender: %s\n",
					e.getLocalizedMessage());
			System.exit(1);
		}
		
		blocks.readFromDisk();
	}

	public void start() {
		new Thread(heartBeatSender).start();
		LOGGER.debug("Sending HeartBeats every "
				+ heartBeatSender.getInterval() + " ms");
	}
	
	
	
	
	
	@Override
	public RemoteBlock createBlock(long blockID) throws RemoteException,
			BlockAlreadyExistsException {
		Block block = blocks.get(blockID);
		if (block != null) {
			LOGGER.warn(block+" already exists");
			throw new BlockAlreadyExistsException();
		}
		block = blocks.newBlock(blockID);
		LOGGER.debug(block+" created");
		return block.getStub();
	}

	@Override
	public RemoteBlock getBlock(long blockID) throws RemoteException,
			BlockNotFoundException {
		Block block = blocks.get(blockID);
		if (block == null)
			throw new BlockNotFoundException();
		
		LOGGER.debug(block+" served");
		return block.getStub();
	}

	@Override
	public RemoteBlock getOrCreateBlock(long blockID) throws RemoteException {
		Block block = blocks.get(blockID);
		if (block == null)
			block = blocks.newBlock(blockID);
		else
			LOGGER.debug(block+" served");
			
		return block.getStub();
	}
	
	
	
	

	@Override
	public BlockReport getBlockReport() throws RemoteException {
		return blocks.getBlockReport();
	}

	public RemoteDataNode getStub() throws RemoteException {
		return (RemoteDataNode) RMIHelper.getStub(this);
	}

	public static void main(String[] args) {
		String host = "localhost";
		String nameNodeAddress = "//" + host + "/NameNode";
		String baseDir;
		if (args.length > 0)
			baseDir = args[0] + "/";
		else
			baseDir = "datanode0/";

		InetSocketAddress nameNodeHeartBeatSocketAddress = new InetSocketAddress(
				host, Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);

		RMIHelper.maybeStartSecurityManager();
		RMIHelper.makeSureRegistryIsStarted(Constants.DEFAULT_DATA_NODE_PORT);

		DataNodeNameNodeProtocol nameNode = (DataNodeNameNodeProtocol) RMIHelper
				.lookupAndWaitForRemoteToStartIfNecessary(nameNodeAddress,
						1_000);

		int id = getSavedID(baseDir);
		if (id == -1) {
			id = registerForNewID(nameNode);
			saveID(baseDir, id);
		}

		DataNode dataNode = new DataNode(id, baseDir, nameNode,
				nameNodeHeartBeatSocketAddress);

		try {
			RMIHelper.rebindAndHookUnbind("DataNode" + dataNode.id,
					dataNode.getStub());
		} catch (RemoteException | MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}

		dataNode.start();
	}

	public static void saveID(String baseDir, int id) {
		File idFile = new File(baseDir+Constants.ID_FILE_NAME);
		idFile.getParentFile().mkdirs();
		
		try (FileWriter writer = new FileWriter(idFile)) {
			idFile.createNewFile();
			writer.write("" + id);
		} catch (IOException e) {
			System.err.printf("could not save ID to file '%s'\n",
					idFile.getAbsolutePath());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static int getSavedID(String baseDir) {
		File idFile = new File(baseDir+Constants.ID_FILE_NAME);
		if (idFile.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(idFile))) {
				String idString = reader.readLine();
				int id = Integer.parseInt(idString);
				System.out.printf("Read ID from file: %d\n", id);
				return id;
			} catch (IOException e) {
				throw new RuntimeException(String.format("Could not read saved ID from file '%s'",
						idFile.getAbsolutePath()), e);
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format("The file '%s' did not contain a valid ID.",
						idFile.getAbsolutePath()));
			}
		}
		else
			return -1;
	}

	public static int registerForNewID(DataNodeNameNodeProtocol nameNode) {
		try {
			int id = nameNode.register();
			System.out.printf("registered with the NameNode for the new ID: %d\n", id);
			return id;
		} catch (RemoteException e) {
			throw new RuntimeException("error while registering with the NameNode", e);
		}
	}
}
