package namenode;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import client.YAHASFile;

import common.BlockReport;
import common.Constants;
import common.RMIHelper;
import common.exceptions.RemoteDirNotFoundException;
import common.exceptions.RemoteFileAlreadyOpenException;
import common.exceptions.RemoteFileNotFoundException;
import common.protocols.RemoteDataNode;
import common.protocols.RemoteDir;
import common.protocols.RemoteNameNode;

public class NameNode extends RemoteServer implements RemoteNameNode {
	private static final long serialVersionUID = -8076847401609606850L;
	private Random randomIDgenerator = new Random();
	private HashSet<DataNodeImage> allEverConnectedDataNodes = new HashSet<>();
	private HashSet<DataNodeImage> connectedDataNodes = new HashSet<>();
	private final HashMap<DataNodeImage, Set<Long>> dataNodeToBlockMap = new HashMap<>();
	private final HashMap<Long, Set<DataNodeImage>> blockToDataNodeMap = new HashMap<>();





	private HeartBeatReceiver heartBeatReceiver;
	private BlockReportReceiver blockReportReceiver;
	private NameNodeDir root;

	private final static Logger LOGGER = Logger.getLogger(NameNode.class
			.getCanonicalName());

	public NameNode(int heartBeatPort) {
		heartBeatReceiver = new HeartBeatReceiver(this, heartBeatPort);
		blockReportReceiver = new BlockReportReceiver(this, Constants.DEFAULT_BLOCKREPORT_TIME);
		root = new NameNodeDir();
	}
	
	private void start() {
		new Thread(heartBeatReceiver).start();
		new Thread(blockReportReceiver).start();
	
		LOGGER.info("Server Ready");
		LOGGER.info("Listening for HeartBeats on port "
				+ heartBeatReceiver.getPort());
	
	}
	
	@Override
	public YAHASFile getFile(String path) throws RemoteException, RemoteFileNotFoundException, RemoteFileAlreadyOpenException {
		NameNodeFile file = root.getFile(path);
		if (file.isOpen())
			throw new RemoteFileAlreadyOpenException();
		else
			return new YAHASFile(file.getStub());
	}

	@Override
	public YAHASFile createFile(String path, byte replicationFactor)
			throws RemoteException, RemoteDirNotFoundException {
		
		int lastSlashIndex = path.lastIndexOf('/');
		
		if (lastSlashIndex < 0)
			lastSlashIndex = 0;
		
		
		String dirs = path.substring(0, lastSlashIndex);
	
		String name = path.substring(lastSlashIndex);
		
		NameNodeFile file = new NameNodeFile(this, name, replicationFactor);
		
		root.getDir(dirs).addFile(file);
		
		return new YAHASFile(file.getStub());
	}

	@Override
	public RemoteDir createDir(String path, boolean createParentsAsNeeded)
			throws RemoteException {
		root.addDir(path, createParentsAsNeeded);
		return null;
	}

	@Override
	public RemoteDir getDir(String path) throws RemoteException,
			RemoteDirNotFoundException {
		return root.getDir(path).getStub();
	}

	@Override
	public int register() throws RemoteException {
		int i;
		do {
			i = randomIDgenerator.nextInt();
		} while (i<1);
		return i;
	}

	@Override
	public void blockReceived(long blockId) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	public HashSet<DataNodeImage> getConnectedDataNodes() {
		return connectedDataNodes;
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
		// TODO
		ArrayList<RemoteDataNode> list = new ArrayList<>();
		for (DataNodeImage dataNode : connectedDataNodes) {
			list.add(dataNode.stub);
		}
		return list;
	}

	public long getNewBlockID() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	
	
	public void dataNodeConnected(DataNodeImage dataNode) {
		connectedDataNodes.add(dataNode);
		if (allEverConnectedDataNodes.add(dataNode)) {
			LOGGER.info(dataNode + " connected for the first time since startup");
			try {
				dataNode.stub.closeAllBlocks();
			} catch (RemoteException e) {
				LOGGER.debug(e);
			}
		}
		else
			LOGGER.info(dataNode + " connected");
		
		blockReportReceiver.getBlockReportFrom(dataNode);
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

	public void dataNodeDisconnected(DataNodeImage dataNode) {
		if (connectedDataNodes.remove(dataNode))
			LOGGER.info(dataNode + " disconnected");
		
		removeReferencesToDataNodeFromBlockToDataNodeMap(dataNode);
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
