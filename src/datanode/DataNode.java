package datanode;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import common.Block;
import common.BlockInterface;
import common.Constants;
import common.RMIHelper;


public class DataNode implements DataNodeInterface {
	public final int id;
	private HeartBeatSender heartBeatSender;
	
	public DataNode(int id, int dataNodePort, int heartBeatPort, InetSocketAddress nameNodeHeartBeatSocketAddress) {
		this.id = id;
		
		try {
			heartBeatSender = new HeartBeatSender(nameNodeHeartBeatSocketAddress, Constants.DEFAULT_HEARTBEAT_INTERVAL_MS, id);
		} catch (SocketException e) {
			System.err.printf("Could not start sending HeartBeats: %s\n", e.getLocalizedMessage());
			System.exit(1);
		}
		
//		IncomingForker incomingForker = null;
//		try {
//			incomingForker = new IncomingForker(dataNodePort);
//		} catch (IOException e) {
//			System.err.printf("Could not start listening on port %d: %s\n", dataNodePort, e.getLocalizedMessage());
//			System.exit(1);
//		}
		
//		System.out.printf("Listening on port %d\n", dataNodePort);
//		new Thread(incomingForker).start();
	}
	
	public void start() {
		new Thread(heartBeatSender).start();
		System.out.printf("Sending HeartBeats every %d ms\n", heartBeatSender.getInterval());
	}

	@Override
	public void receiveMessage(String s) throws RemoteException {
		System.out.println(s);
	}

	@Override
	public BlockInterface createBlock(long id) throws RemoteException {
		return (BlockInterface) UnicastRemoteObject.exportObject(new Block(id), 0);
	}
	
	
	
	
	
	public static void main(String[] args) {
		int heartBeatPort = Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT;
		int dataNodePort = Constants.DEFAULT_DATA_NODE_PORT;
		int id = 0;
		InetSocketAddress nameNodeHeartBeatSocketAddress =
				new InetSocketAddress("localhost", Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);
		
		RMIHelper.maybeStartSecurityManager();
		RMIHelper.makeSureRegistryIsStarted(dataNodePort);
		
		DataNode dataNode = new DataNode(id, dataNodePort, heartBeatPort, nameNodeHeartBeatSocketAddress);
		
		try {
			DataNodeInterface stub = (DataNodeInterface) UnicastRemoteObject.exportObject(dataNode, 0);
			Naming.rebind("DataNode", stub);
		} catch (RemoteException | MalformedURLException e) {
		    System.err.println("Server exception: " + e);
			System.exit(1);
		}
		
		dataNode.start();
	}
}
