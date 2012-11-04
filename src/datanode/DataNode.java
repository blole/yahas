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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import common.BlockReport;
import common.Constants;
import common.RMIHelper;
import common.exceptions.RemoteBlockAlreadyExistsException;
import common.exceptions.RemoteBlockAlreadyOpenException;
import common.exceptions.RemoteBlockNotFoundException;
import common.protocols.DataNodeNameNodeProtocol;
import common.protocols.RemoteBlock;
import common.protocols.RemoteDataNode;

public class DataNode implements RemoteDataNode {

	private static final String BASE_BLOCK_PATH = "blocks/";
	private static final String ID_FILE_NAME = "id.txt";
	public final String pathBaseDir;
	public final String pathBlocks;

	private static final Logger LOGGER = Logger.getLogger(DataNode.class
			.getCanonicalName());

	public final int id;
	private HeartBeatSender heartBeatSender;
	public final HashMap<Long, Block> openBlocks = new HashMap<>();

	private DataNodeNameNodeProtocol nameNode;
	// Consist of a List of Blocks and it's hash which is held by a DataNode;
	private BlockReport blockReport;
	private ArrayList<Long> listOfBlocks = new ArrayList<Long>();

	public DataNode(int id, String baseDir, DataNodeNameNodeProtocol nameNode,
			InetSocketAddress nameNodeHeartBeatSocketAddress) {
		this.id = id;
		this.nameNode = nameNode;
		this.pathBaseDir = baseDir; // String.format("../datanode%d/", id);
		this.pathBlocks = baseDir + BASE_BLOCK_PATH;
		this.blockReport = new BlockReport();

		try {
			heartBeatSender = new HeartBeatSender(
					nameNodeHeartBeatSocketAddress,
					Constants.DEFAULT_HEARTBEAT_INTERVAL_MS, id);
		} catch (SocketException e) {
			System.err.printf("Could not create HeartBeat sender: %s\n",
					e.getLocalizedMessage());
			System.exit(1);
		}
	}

	public void start() {
		new Thread(heartBeatSender).start();
		LOGGER.debug("Sending HeartBeats every "
				+ heartBeatSender.getInterval() + " ms");
	}

	@Override
	public RemoteBlock createBlock(long blockID) throws RemoteException,
			RemoteBlockAlreadyExistsException {
		return Block.create(blockID, this).getStub();
	}

	@Override
	public RemoteBlock openBlock(long blockID) throws RemoteException,
			RemoteBlockAlreadyOpenException, RemoteBlockNotFoundException {
		return Block.get(blockID, this).getStub();
	}

	@Override
	public RemoteBlock openOrCreateBlock(long blockID) throws RemoteException,
			RemoteBlockAlreadyOpenException {
		return Block.openOrCreate(blockID, this).getStub();
	}

	public void addBlockList(long blockId) {
		LOGGER.debug("Adding Block " + blockId);
		listOfBlocks.add(blockId);
		for (long id : listOfBlocks) {
			// TODO:Get the block with ID and Match the hash
			LOGGER.debug("Added to Block " + id + " to Block Report ");
			blockReport.addBlockReport(id);
		}
	}

	@Override
	public BlockReport getBlockReport() throws RemoteException {
		// Get All Blocks.
		// Check Hash Of Block
		// If Hash Matches add to BlockReport
		// TODO: only add blocks which matches the hash
		// TODO: To check added all the Blocks
		if (listOfBlocks.size() > 0)
			for (long id : listOfBlocks) {
				// TODO:Get the block with ID and Match the hash
				LOGGER.debug("Added to Block " + id + " to Block Report ");
				blockReport.addBlockReport(id);
			}
		return blockReport;

		// return blockReport;
		// return new BlockReport();
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
		if (id == 0) {
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
		File idFile = new File(baseDir + ID_FILE_NAME);
		idFile.getParentFile().mkdirs();
		try {
			idFile.createNewFile();
			FileWriter writer = new FileWriter(idFile);
			writer.write("" + id);
			writer.close();
		} catch (IOException e) {
			System.err.printf("could not save ID to file '%s'\n",
					idFile.getAbsolutePath());
			e.printStackTrace();
			System.exit(1);
		}
	}

	public static int getSavedID(String baseDir) {
		File idFile = new File(baseDir + ID_FILE_NAME);
		if (idFile.exists()) {
			try {
				FileReader reader = new FileReader(idFile);
				String idString = new BufferedReader(reader).readLine();
				reader.close();
				int id = Integer.parseInt(idString);
				System.out.printf("Read ID from file: %d\n", id);
				return id;
			} catch (IOException e) {
				System.err.printf("Could not read saved ID from file '%s'",
						idFile.getAbsolutePath());
				e.printStackTrace();
				System.exit(1);
			} catch (NumberFormatException e) {
				System.err.printf("The file '%s' did not contain a valid ID.",
						idFile.getAbsolutePath());
				System.exit(1);
			}
		}
		return 0;
	}

	public static int registerForNewID(DataNodeNameNodeProtocol nameNode) {
		try {
			int id = nameNode.register();
			System.out.printf(
					"registered with the NameNode for the new ID: %d\n", id);
			return id;
		} catch (RemoteException e) {
			System.err.println("error while registering with the NameNode:");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		return 0; // never executed;
	}
}
