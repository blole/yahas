package datanode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import common.Constants;


public class DataNode {
	public DataNode(int dataNodePort, int incomingPort) {
		InetSocketAddress nameNodeHeartBeatSocketAddress =
				new InetSocketAddress("localhost", Constants.DEFAULT_NAME_NODE_PORT);
		long interval_ms = Constants.DEFAULT_HEARTBEAT_INTERVAL_MS;
		
		IncomingForker incomingForker = null;
		HeartBeatSender heartBeatSender = null;
		
		try {
			incomingForker = new IncomingForker(incomingPort);
		} catch (IOException e) {
			System.err.printf("Could not start listening on port %d: %s\n", incomingPort, e.getLocalizedMessage());
			System.exit(1);
		}
		try {
			heartBeatSender = new HeartBeatSender(nameNodeHeartBeatSocketAddress, interval_ms); 
		} catch (SocketException e) {
			System.err.printf("Could not start sending HeartBeats: %s\n", e.getLocalizedMessage());
			System.exit(1);
		}
		
		System.out.printf("Listening on port %d\n", incomingPort);
		System.out.printf("Sending HeartBeats every %d ms\n", interval_ms);
		System.out.println();
		
		new Thread(incomingForker).start();
		new Thread(heartBeatSender).start();
	}

	public static void main(String[] args) {
		int port = Constants.DEFAULT_DATA_NODE_PORT;
		
		if (args.length >= 2) {
			try {
				port = Integer.valueOf(args[1]);
			}
			catch (NumberFormatException e) {
				System.out.println("malformated port specification, exiting");
				System.exit(1);
			}
		}
		
		new DataNode(port, port);
	}
}
