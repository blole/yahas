package namenode;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashSet;

import rmi.interfaces.NameNodeInterface;

import common.Constants;
import common.RMIHelper;
import datanode.DataNodeInterface;

public class NameNode extends RemoteServer implements NameNodeInterface {
	private static final long serialVersionUID = -8076847401609606850L;
	private HashSet<DataNodeInterface> connectedDataNodes = new HashSet<>();
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
	public void receiveMessage(String s) throws RemoteException {
		System.out.println(s);
	}
	
	@Override
	public HashSet<DataNodeInterface> getDataNodes() throws RemoteException {
		return connectedDataNodes;
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
		System.out.printf("%s disconnected   NOT YET IMPLEMENTED\n", dataNodeImage);
//		if (connectedDataNodes.remove(dataNodeImage))
//			System.out.printf("DataNode disconnected: %s\n", dataNodeImage);
	}
	
	
	
	
	
	public static void main(String[] args) {
		int nameNodePort = Constants.DEFAULT_NAME_NODE_PORT;
		
		RMIHelper.maybeStartSecurityManager();
		RMIHelper.makeSureRegistryIsStarted(nameNodePort);
		
		NameNode nameNode = new NameNode(Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);
		try {
			NameNodeInterface stub = (NameNodeInterface) UnicastRemoteObject.exportObject(nameNode, 0);
			Naming.rebind("NameNode", stub);
		} catch (RemoteException | MalformedURLException e) {
		    System.err.println("Server exception: " + e.getLocalizedMessage().split(";")[0]);
		    e.printStackTrace();
			System.exit(1);
		}
		
		nameNode.start();
	}
}
