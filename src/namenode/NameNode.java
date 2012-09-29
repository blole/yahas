package namenode;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.Lock;

import client.File;


import common.Constants;
import common.RMIHelper;
import common.protocols.ClientDataNodeProtocol;
import common.protocols.ClientNameNodeProtocol;
import common.protocols.NameNodeDataNodeProtocol;
import common.protocols.RemoteDataNode;
import common.protocols.RemoteDir;
import common.protocols.RemoteFile;
import common.protocols.RemoteNameNode;

public class NameNode extends RemoteServer implements RemoteNameNode {
	private static final long serialVersionUID = -8076847401609606850L;
	private HashSet<ClientDataNodeProtocol> connectedDataNodes = new HashSet<>();
	private HeartBeatReceiver heartBeatReceiver;

	public NameNode(int heartBeatPort) {
		heartBeatReceiver = new HeartBeatReceiver(this, heartBeatPort);
	}
	
	private void start() {
		new Thread(heartBeatReceiver).start();
		System.out.printf("Listening for HeartBeats on port %d\n", heartBeatReceiver.getPort());
		System.out.println("Server ready");
		System.out.println();
	}
	
	
	
	
	
	@Override
	public RemoteFile getFile(String name) throws RemoteException {
		return createFile(name, (byte) 3);
	}

	@Override
	public RemoteFile createFile(String name, byte replicationFactor) throws RemoteException {
		return (RemoteFile) UnicastRemoteObject.exportObject(new NameNodeFile(this, name, replicationFactor), 0);
	}

	@Override
	public RemoteDir createDir(String name, boolean createParentsAsNeeded) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RemoteDir getDir(String name) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int register(NameNodeDataNodeProtocol dataNode) throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void blockReceived(long blockId) throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<ClientDataNodeProtocol> getDataNodes() throws RemoteException {
		return null;
	}

	
	
	
	public void dataNodeConnected(DataNodeImage dataNodeImage) {
		try {
			connectedDataNodes.add(dataNodeImage.getStub());
			System.out.printf("%s connected\n", dataNodeImage);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.err.printf("%s failed connecting: %s\n", dataNodeImage, e.getLocalizedMessage());
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

	public long getNewBlockID() {
		// TODO Auto-generated method stub
		return 0;
	}
}
