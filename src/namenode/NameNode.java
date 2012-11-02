package namenode;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import client.GreatFile;

import common.Constants;
import common.RMIHelper;
import common.exceptions.RemoteDirNotFoundException;
import common.exceptions.RemoteFileNotFoundException;
import common.protocols.ClientNameNodeProtocol;
import common.protocols.NameNodeDataNodeProtocol;
import common.protocols.RemoteDataNode;
import common.protocols.RemoteDir;
import common.protocols.RemoteNameNode;

public class NameNode extends RemoteServer implements RemoteNameNode {
	private static final long serialVersionUID = -8076847401609606850L;
	private Random randomIDgenerator = new Random();
	private HashSet<RemoteDataNode> connectedDataNodes = new HashSet<>();
	private HeartBeatReceiver heartBeatReceiver;
	private NameNodeDir root;

	public NameNode(int heartBeatPort) {
		heartBeatReceiver = new HeartBeatReceiver(this, heartBeatPort);
		root = new NameNodeDir();
	}
	
	private void start() {
		new Thread(heartBeatReceiver).start();
		System.out.printf("Listening for HeartBeats on port %d\n", heartBeatReceiver.getPort());
		System.out.println("Server ready");
		System.out.println();
	}
	
	
	
	@Override
	public GreatFile getFile(String path) throws RemoteException, RemoteFileNotFoundException {
		NameNodeFile file = root.getFile(path);
		if (file == null)
			throw new RemoteFileNotFoundException();
		else
			return new GreatFile(file.getStub());
	}

	@Override
	public GreatFile createFile(String path, byte replicationFactor) throws RemoteException, RemoteDirNotFoundException {
		int lastSlashIndex = path.lastIndexOf('/');
		if (lastSlashIndex < 0)
			lastSlashIndex = 0;
		
		String dirs = path.substring(0, lastSlashIndex);
		String name = path.substring(lastSlashIndex);
		NameNodeFile file = new NameNodeFile(this, name, replicationFactor);
		root.getDir(dirs).addFile(file);
		return new GreatFile(file.getStub());
	}

	@Override
	public RemoteDir createDir(String path, boolean createParentsAsNeeded) throws RemoteException {
		root.addDir(path, createParentsAsNeeded);
		return null;
	}

	@Override
	public RemoteDir getDir(String path) throws RemoteException, RemoteDirNotFoundException {
		return root.getDir(path).getStub();
	}

	@Override
	public int register(NameNodeDataNodeProtocol dataNode) throws RemoteException {
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

	@Override
	public List<RemoteDataNode> getDataNodes() throws RemoteException {
		return new ArrayList<>(connectedDataNodes);
	}

	public List<RemoteDataNode> getAppropriateDataNodes(byte replicationFactor) {
		return new ArrayList<>(connectedDataNodes);
	}

	public long getNewBlockID() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	
	
	public void dataNodeConnected(DataNodeImage dataNodeImage) {
		try {
			connectedDataNodes.add(dataNodeImage.getStub());
			System.out.printf("%s connected\n", dataNodeImage);
		} catch (MalformedURLException | RemoteException e) {
			e.printStackTrace();
			System.err.printf("%s failed to connect: %s\n", dataNodeImage, e.getLocalizedMessage());
		} catch (NotBoundException e) {
			System.err.printf("%s failed to connect: the remote DataNode was not bound.\n", dataNodeImage);
		}
				
		//TODO: spawn this as a new thread.
//		Set<BlockImage> blocks;
//		try {
//			blocks = dataNodeImage.getBlocks();
//		} catch (IOException | InvalidPacketType | UnexpectedPacketType e) {
//			e.printStackTrace();
//		}
	}

	public void dataNodeDisconnected(DataNodeImage dataNodeImage) {
		if (connectedDataNodes.remove(dataNodeImage.getStubOrNull()))
			System.out.printf("%s disconnected\n", dataNodeImage);
	}
	
	
	
	
	
	public static void main(String[] args) {
		int nameNodePort = Constants.DEFAULT_NAME_NODE_PORT;
		
		RMIHelper.maybeStartSecurityManager();
		RMIHelper.makeSureRegistryIsStarted(nameNodePort);
		
		NameNode nameNode = new NameNode(Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);
		try {
			ClientNameNodeProtocol stub = (ClientNameNodeProtocol) UnicastRemoteObject.exportObject(nameNode, 0);
			Naming.rebind("NameNode", stub);
		} catch (RemoteException | MalformedURLException e) {
		    System.err.println("Server exception: " + e.getLocalizedMessage().split(";")[0]);
		    e.printStackTrace();
			System.exit(1);
		}
		
		nameNode.start();
	}
}
