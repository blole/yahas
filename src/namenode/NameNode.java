package namenode;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.javatuples.Pair;

import client.ClientFile;

import common.BlockInfo;
import common.Constants;
import common.RMIHelper;
import common.exceptions.BadFileName;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.exceptions.NotFileException;
import common.protocols.RemoteDataNode;
import common.protocols.RemoteDir;
import common.protocols.RemoteNameNode;

public class NameNode extends RemoteServer implements RemoteNameNode {
	private static final long serialVersionUID = -8076847401609606850L;
	private final static Logger LOGGER = Logger.getLogger(NameNode.class
			.getCanonicalName());
	
	private final HashMap<Integer, DataNodeImage> connectedDataNodes = new HashMap<>();
	private final HashMap<Long, BlockImage> allBlocks = new HashMap<>();

	
	
	
	
	private HeartBeatReceiver heartBeatReceiver;
	private BlockReportRetriever blockReportReceiver;
	private final NameNodeRootDir root;
	private int dataNodeIdCounter;
	private int blockIDCounter;

	public NameNode(int heartBeatPort) {
		heartBeatReceiver = new HeartBeatReceiver(this, heartBeatPort);
		blockReportReceiver = new BlockReportRetriever(this, Constants.DEFAULT_BLOCKREPORT_TIME_MS);
		root = new NameNodeRootDir();
		dataNodeIdCounter = 0;
		blockIDCounter = 0;
	}
	private void start() {
		new Thread(heartBeatReceiver).start();
		new Thread(blockReportReceiver).start();
	
		LOGGER.info("Listening for HeartBeats on port "
				+ heartBeatReceiver.getPort());
		LOGGER.info("NameNode Ready\n");
	}
	
	
	
	
	
	@Override
	public ClientFile createFile(String path, byte replicationFactor, int blockSize) throws FileAlreadyExistsException, NotDirectoryException, NoSuchFileOrDirectoryException, BadFileName {
		Pair<NameNodeDir, String> pair = root.getLastDir(path, false);
		
		if (pair.getValue0() == null)
			throw new BadFileName();
		else {
			NameNodeDir dir = pair.getValue0();
			String fileName = pair.getValue1();
			NameNodeFile file = new NameNodeFile(this, fileName, replicationFactor, blockSize);
			dir.moveHere(file, fileName);
			LOGGER.debug(file+" created");
			return file.getYAHASFile();
		}
	}
	
	@Override
	public ClientFile getFile(String path) throws RemoteException, NotDirectoryException, NoSuchFileOrDirectoryException, NotFileException {
		return root.getFile(path).getYAHASFile();
	}

	@Override
	public RemoteDir createDir(String path, boolean createParentsAsNeeded) throws NotDirectoryException, FileAlreadyExistsException, NoSuchFileOrDirectoryException {
		NameNodeDir dir = root.createDir(path, createParentsAsNeeded);
		LOGGER.debug(dir+" created");
		return dir.getStub();
	}

	@Override
	public RemoteDir getDir(String path) throws NotDirectoryException, NoSuchFileOrDirectoryException {
		NameNodeDir dir = root.getDir(path, false);
		LOGGER.debug(dir+" served");
		return dir.getStub();
	}
	
	
	
	
	
	@Override
	public int register() throws RemoteException {
		LOGGER.debug("Issued new DataNodeID: "+dataNodeIdCounter);
		return dataNodeIdCounter++;
	}

	public LinkedList<RemoteDataNode> getAppropriateDataNodes(int replicationFactor) {
		// TODO return APPROPRIATE DataNodes, not replicationFactor many
		LinkedList<RemoteDataNode> list = new LinkedList<>();
		for (DataNodeImage dataNode : connectedDataNodes.values()) {
			list.add(dataNode.getRemoteStub());
			if (list.size() >= replicationFactor)
				break;
		}
		return list;
	}
	
	
	
	
	
	public void dataNodeConnected(int dataNodeID, InetSocketAddress address) {
		DataNodeImage dataNode;
		try {
			dataNode = new DataNodeImage(dataNodeID, address, this);
		} catch (MalformedURLException | RemoteException e) {
			LOGGER.error("DataNode "+dataNodeID+" failed to connect", e);
			return;
		} catch (NotBoundException e) {
			LOGGER.error("DataNode "+dataNodeID+" failed to connect, " +
					"the remote DataNode was not bound in the rmiregistry");
			return;
		}
		
		connectedDataNodes.put(dataNode.id, dataNode);
		dataNode.connected();
	}
	
	public void dataNodeDisconnected(int dataNodeID) {
		DataNodeImage dataNode = connectedDataNodes.remove(dataNodeID);
		if (dataNode != null)
			dataNode.disconnected();
	}
	
	@Override
	public void blockReceived(int dataNodeID, BlockInfo blockInfo) {
		DataNodeImage dataNode = connectedDataNodes.get(dataNodeID);
		if (dataNode == null)
			LOGGER.error("Non-connected DataNode "+dataNodeID+" reports having received a block");
		else {
			dataNode.blockReceived(blockInfo);
		}
	}
	
	public BlockImage getBlock(long blockID) {
		return allBlocks.get(blockID);
	}
	
	
	
	
	
//	public boolean doneStarting() {
//		return true;
//	}
	
	public BlockImage addNewBlock(int replicationFactor, int blockSize) {
		BlockImage block = new BlockImage(blockIDCounter++, replicationFactor, blockSize, this);
		allBlocks.put(block.blockID, block);
		return block;
	}
	
	public HashMap<Integer, DataNodeImage> getConnectedDataNodes() {
		return connectedDataNodes;
	}
	
	
	
	
	
	public static void main(String[] args) {
		int nameNodePort = Constants.DEFAULT_NAME_NODE_PORT;
		
		RMIHelper.maybeStartSecurityManager();
		RMIHelper.makeSureRegistryIsStarted(nameNodePort);
		NameNode nameNode = new NameNode(Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);
		try {
			RemoteNameNode stub = (RemoteNameNode) RMIHelper.getStub(nameNode);
			RMIHelper.rebindAndHookUnbind("NameNode", stub);
		} catch (RemoteException | MalformedURLException e) {
			String errorMessage = "error setting up server";
			LOGGER.error(errorMessage, e);
			throw new RuntimeException(errorMessage, e);
		}
		
		nameNode.start();
	}
}
