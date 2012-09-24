package namenode;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;

import rmi.interfaces.NameNodeInterface;

import common.Constants;
import common.exceptions.InvalidPacketType;
import common.exceptions.UnexpectedPacketType;

public class NameNode extends RemoteServer implements NameNodeInterface {
	private static final long serialVersionUID = -8076847401609606850L;

	public NameNode(int heartBeatPort) {
		new Thread(new HeartBeatReceiver(this, heartBeatPort)).start();
		System.out.printf("Listening for HeartBeats on port %d\n", heartBeatPort);
	}

	public void dataNodeConnected(DataNodeImage dataNodeImage) {
		System.out.printf("DataNode connected: %s\n", dataNodeImage);
		//TODO: spawn this as a new thread.
		Set<BlockImage> blocks;
		try {
			blocks = dataNodeImage.getBlocks();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidPacketType e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnexpectedPacketType e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void dataNodeDisconnected(DataNodeImage dataNodeImage) {
		System.out.printf("DataNode disconnected: %s\n", dataNodeImage);
	}

	public static void main(String[] args) {
		int nameNodePort = 1099;//Constants.DEFAULT_NAME_NODE_PORT;
		
		if (args.length >= 2) {
			try {
				nameNodePort = Integer.valueOf(args[1]);
			}
			catch (NumberFormatException e) {
				System.out.println("malformated port specification, exiting");
				System.exit(1);
			}
		}
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
			System.out.println("Security manager installed.");
		} else {
			System.out.println("Security manager already exists.");
		}
		
		try {
		    NameNode nameNode = new NameNode(Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);
		    NameNodeInterface stub = (NameNodeInterface) UnicastRemoteObject.exportObject(nameNode, 0);

		    // Bind the remote object's stub in the registry
		    Registry registry = LocateRegistry.createRegistry(nameNodePort);
		    registry.bind("NameNode", stub);

		    System.out.println("Server ready");
		} catch (Exception e) {
		    System.err.println("Server exception: " + e.toString());
		    e.printStackTrace();
		    System.exit(1);
		}
		
//        Registry reg = null;
//        try {
//        	reg = LocateRegistry.createRegistry(nameNodePort);
//		} catch (RemoteException e) {
//			System.err.println("Java RMI registry already exists.");
//			System.exit(1);
//		}
//		
//		NameNode nameNode = new NameNode(nameNodePort, Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);
//		
//		try {
//			reg.rebind("//localhost/NameNode", nameNode);
//		} catch (RemoteException e) {
//			e.printStackTrace();
//			System.exit(1);
////		} catch (MalformedURLException e) {
////			e.printStackTrace();
////			System.exit(1);
//		}
	}

	@Override
	public void receiveMessage(String s) throws RemoteException {
		System.out.println(s);
	}
}
