package namenode;

import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.javatuples.Pair;

import client.YAHASFile;

import common.BlockReport;
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
	private HashSet<DataNodeImage> connectedDataNodes = new HashSet<>();
	private final HashMap<DataNodeImage, Set<Long>> dataNodeToBlockMap = new HashMap<>();
	private final HashMap<Long, Set<DataNodeImage>> blockToDataNodeMap = new HashMap<>();





	private HeartBeatReceiver heartBeatReceiver;
	private BlockReportReceiver blockReportReceiver;
	private final NameNodeRootDir root;
	private int dataNodeIdCounter;
	private int blockIdCounter;

	private final static Logger LOGGER = Logger.getLogger(NameNode.class
			.getCanonicalName());

	public NameNode(int heartBeatPort) {
		heartBeatReceiver = new HeartBeatReceiver(this, heartBeatPort);
		blockReportReceiver = new BlockReportReceiver(this, Constants.DEFAULT_BLOCKREPORT_TIME_MS);
		root = new NameNodeRootDir();
		dataNodeIdCounter = 0;
		blockIdCounter = 0;
	}
	
	private void start() {
		new Thread(heartBeatReceiver).start();
		new Thread(blockReportReceiver).start();
	
		LOGGER.info("Listening for HeartBeats on port "
				+ heartBeatReceiver.getPort());
		LOGGER.info("Server Ready\n");
	
	}
	
	@Override
	public YAHASFile getFile(String path) throws RemoteException, NotDirectoryException, NoSuchFileOrDirectoryException, NotFileException {
		return root.getFile(path).getYAHASFile();
	}

	@Override
	public YAHASFile createFile(String path, byte replicationFactor) throws FileAlreadyExistsException, NotDirectoryException, NoSuchFileOrDirectoryException, BadFileName {
		Pair<NameNodeDir, String> pair = FileOrDir.getLastDir(root, path);
		
		if (pair.getValue0() == null)
			throw new BadFileName();
		else {
			NameNodeDir dir = pair.getValue0();
			String fileName = pair.getValue1();
			NameNodeFile file = new NameNodeFile(this, fileName, replicationFactor);
			dir.moveHere(file, fileName);
			LOGGER.debug(String.format("Created file '%s'", path));
			return file.getYAHASFile();
		}
	}

	@Override
	public RemoteDir createDir(String path, boolean createParentsAsNeeded) throws NotDirectoryException, FileAlreadyExistsException, NoSuchFileOrDirectoryException {
		NameNodeDir dir = root.createDir(path, createParentsAsNeeded);
		LOGGER.debug(String.format("Created dir '%s'", path));
		return dir.getStub();
	}

	@Override
	public RemoteDir getDir(String path) throws NotDirectoryException, NoSuchFileOrDirectoryException {
		NameNodeDir dir = root.getDir(path);
		LOGGER.debug(String.format("Served dir '%s'", path));
		return dir.getStub();
	}
	
	
	
	
	
	@Override
	public int register() throws RemoteException {
		LOGGER.debug("Issued new DataNodeID: "+dataNodeIdCounter);
		return dataNodeIdCounter++;
	}

	@Override
	public void blockReceived(long blockId) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<RemoteDataNode> getDataNodes() throws RemoteException {
		ArrayList<RemoteDataNode> list = new ArrayList<>();
		for (DataNodeImage dataNode : connectedDataNodes) {
			list.add(dataNode.stub);
		}
		return list;
	}

	public List<RemoteDataNode> getAppropriateDataNodes(byte replicationFactor) {
		// TODO return APPROPRIATE DataNodes, not all
		ArrayList<RemoteDataNode> list = new ArrayList<>();
		for (DataNodeImage dataNode : connectedDataNodes) {
			list.add(dataNode.stub);
		}
		return list;
	}
	
	
	
	
	
	public HashSet<DataNodeImage> getConnectedDataNodes() {
		return connectedDataNodes;
	}
	
	public long getNewBlockID() {
		return blockIdCounter++;
	}
	
	
	
	
	public void dataNodeConnected(DataNodeImage dataNode) {
		connectedDataNodes.add(dataNode);
		LOGGER.info(dataNode + " connected");
		
		blockReportReceiver.getBlockReportFrom(dataNode);
	}

	public void dataNodeDisconnected(DataNodeImage dataNode) {
		if (connectedDataNodes.remove(dataNode))
			LOGGER.info(dataNode + " disconnected");
		
		removeReferencesToDataNodeFromBlockToDataNodeMap(dataNode);
	}
	
	public void receiveBlockReport(DataNodeImage from, BlockReport blockReport) {
		removeReferencesToDataNodeFromBlockToDataNodeMap(from);
		
		for (long blockID : blockReport.blockIDs) {
			Set<DataNodeImage> dataNodes = blockToDataNodeMap.get(blockID);
			if (dataNodes == null)
				dataNodes = new HashSet<>();
			dataNodes.add(from);
		}
		
		dataNodeToBlockMap.put(from, blockReport.blockIDs);
	}

	private void removeReferencesToDataNodeFromBlockToDataNodeMap(DataNodeImage dataNode) {
		Set<Long> oldBlocks = dataNodeToBlockMap.get(dataNode);
		if (oldBlocks != null) {
			for (long oldBlock : oldBlocks) {
				Set<DataNodeImage> dataNodes = blockToDataNodeMap.get(oldBlock);
				if (dataNodes != null) {
					dataNodes.remove(dataNode);
					if (dataNodes.isEmpty())
						blockToDataNodeMap.remove(dataNodes);
				}
			}
		}
	}
	
	
	
	private RemoteNameNode getStub() throws RemoteException {
		return (RemoteNameNode) RMIHelper.getStub(this);
	}
	
	
	
	
	
	public static void main(String[] args) {
		int nameNodePort = Constants.DEFAULT_NAME_NODE_PORT;
		
		RMIHelper.maybeStartSecurityManager();
		RMIHelper.makeSureRegistryIsStarted(nameNodePort);
		NameNode nameNode = new NameNode(Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);
		try {
			RemoteNameNode stub = nameNode.getStub();
			RMIHelper.rebindAndHookUnbind("NameNode", stub);
		} catch (RemoteException | MalformedURLException e) {
			LOGGER.error("Server exception: "
					+ e.getLocalizedMessage().split(";")[0]);
			e.printStackTrace();
			System.exit(1);
		}
		
		nameNode.start();
	}
}
