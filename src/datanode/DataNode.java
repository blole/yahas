package datanode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

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
	
	public int id;
	private HeartBeatSender heartBeatSender;
	public final HashMap<Long, Block> openBlocks = new HashMap<>();
	private DataNodeNameNodeProtocol nameNode;
	
	public DataNode(DataNodeNameNodeProtocol nameNode, String pathBaseDir, InetSocketAddress nameNodeHeartBeatSocketAddress) {
		this.nameNode = nameNode;
		this.pathBaseDir = pathBaseDir; //String.format("../datanode%d/", id);
		this.pathBlocks = pathBaseDir + BASE_BLOCK_PATH;
		
		File saveDir = new File(pathBaseDir);
		if (!saveDir.exists())
			saveDir.mkdirs();
		
		File idFile = new File(pathBaseDir+ID_FILE_NAME);
		if (idFile.exists()) {
			try {
				FileReader reader = new FileReader(idFile);
				String idString = new BufferedReader(reader).readLine();
				reader.close();
				System.out.println(idFile.getAbsolutePath());
				id = Integer.parseInt(idString);
				System.out.printf("read ID from file: %d\n", id);
			} catch (NumberFormatException e) {
				throw new RuntimeException(String.format(
						"The file '%s' did not contain a valid ID.", idFile.getAbsolutePath()));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			int attempts = 0;
			while (true) {
				int retryTime = 1_000;
				try {
					id = nameNode.register(this.getStub());
					System.out.printf("registered with the NameNode for the new ID: %d\n", id);
					break;
				} catch (RemoteException e) {
					if (attempts == 0) {
						System.err.println("error while registering with the NameNode:");
						System.err.println(e.getMessage());
						System.err.printf("retrying in %d seconds\n", retryTime/1000);
					}
					else {
						System.err.printf("retry number %d failed...\n", attempts);
					}
					try {
						Thread.sleep(retryTime);
					} catch (InterruptedException e1) {}
					attempts++;
				}
			}
			try {
				idFile.createNewFile();
				FileWriter writer = new FileWriter(idFile);
				writer.write(""+id);
				writer.close();
			} catch (IOException e) {
				System.err.printf("could not save ID to file '%s'\n", idFile.getAbsolutePath());
				e.printStackTrace();
			}
		}
		
		try {
			heartBeatSender = new HeartBeatSender(nameNodeHeartBeatSocketAddress, Constants.DEFAULT_HEARTBEAT_INTERVAL_MS, id);
		} catch (SocketException e) {
			System.err.printf("Could not create HeartBeat sender: %s\n", e.getLocalizedMessage());
			System.exit(1);
		}
	}
	
	public void start() {
		new Thread(heartBeatSender).start();
		System.out.printf("Sending HeartBeats every %d ms\n", heartBeatSender.getInterval());
	}

	@Override
	public RemoteBlock createBlock(long blockID) throws RemoteException,
					RemoteBlockAlreadyExistsException {
		return Block.create(blockID, this).getStub();
	}

	@Override
	public RemoteBlock openBlock(long blockID) throws RemoteException,
					RemoteBlockAlreadyOpenException,
					RemoteBlockNotFoundException {
		return 	Block.get(blockID, this).getStub();
	}

	@Override
	public RemoteBlock openOrCreateBlock(long blockID) throws RemoteException,
					RemoteBlockAlreadyOpenException {
		return Block.getOrCreate(blockID, this).getStub();
	}

	@Override
	public BlockReport getBlockReport() throws RemoteException {
		return new BlockReport();
	}
	
	public RemoteDataNode getStub() throws RemoteException {
		return (RemoteDataNode) UnicastRemoteObject.exportObject(this, 0);
	}
	
	
	
	
	
	public static void main(String[] args) {
		String host = "localhost";
		String nameNodeAddress = "//"+host+"/NameNode";
		
		InetSocketAddress nameNodeHeartBeatSocketAddress =
				new InetSocketAddress(host, Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);
		
		RMIHelper.maybeStartSecurityManager();
		RMIHelper.makeSureRegistryIsStarted(Constants.DEFAULT_DATA_NODE_PORT);
		
		DataNodeNameNodeProtocol nameNode = null;
		try {
			nameNode = (DataNodeNameNodeProtocol) Naming.lookup(nameNodeAddress);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (RemoteException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		} catch (NotBoundException e) {
			System.err.printf("rmiregistry is likely running on the NameNode '%s', " +
					"but '%s' is not bound.\n", host, e.getMessage());
			System.exit(1);
		}
		
		DataNode dataNode = new DataNode(nameNode, "datanode0/", nameNodeHeartBeatSocketAddress);
		
		try {
			Naming.rebind("DataNode"+dataNode.id, dataNode.getStub());
		} catch (RemoteException | MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		dataNode.start();
	}
}
