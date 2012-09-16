package namenode;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;

import common.Constants;
import common.YSocket;

public class NameNode {
	public NameNode(int incomingPort, int heartBeatPort) {
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
		}
	}

	public void dataNodeDisconnected(DataNodeImage dataNodeImage) {
		System.out.printf("DataNode disconnected: %s\n", dataNodeImage);
	}

	public static void main(String[] args) {
		int nameNodePort = Constants.DEFAULT_NAME_NODE_PORT;
		
		if (args.length >= 2) {
			try {
				nameNodePort = Integer.valueOf(args[1]);
			}
			catch (NumberFormatException e) {
				System.out.println("malformated port specification, exiting");
				System.exit(1);
			}
		}
		
		new NameNode(nameNodePort, Constants.DEFAULT_NAME_NODE_HEARTBEAT_PORT);
	}
}
