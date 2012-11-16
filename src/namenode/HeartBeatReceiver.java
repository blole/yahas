package namenode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import common.Action;
import common.Constants;
import common.Convert;
import common.TimeoutHashSet;

public class HeartBeatReceiver implements Runnable {
	private DatagramSocket incomingHeartBeatSocket;
	private NameNode nameNode;
	
	//Removes an element when a timeout happens
	private TimeoutHashSet<Integer> connectedDataNodes;
//	private static final Logger LOGGER = Logger.getLogger(HeartBeatReceiver.class.getCanonicalName());

	public HeartBeatReceiver(NameNode reportBackTo, int heartBeatPort) {
		try {
			incomingHeartBeatSocket = new DatagramSocket(heartBeatPort);
		} catch (SocketException e) {
			String errorMessage = String.format("Could not bind to HeartBeat port %d: %s\n",
					heartBeatPort, e.getLocalizedMessage());
			throw new RuntimeException(errorMessage);
		}
		
		this.nameNode = reportBackTo;
	}

	@Override
	public void run() {
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		connectedDataNodes = new TimeoutHashSet<>(
				Constants.DEFAULT_HEARTBEAT_TIMEOUT_MS,
				new Action<Integer>(){
					@Override
					public void execute(Integer dataNodeID) {
						nameNode.dataNodeDisconnected(dataNodeID);
					}
				}
			);
		
		while (true) {
			try {
				incomingHeartBeatSocket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
				continue;
			}
			
			if (packet.getLength() != 4)
				System.err.printf("Recieved a HeartBeat with the wrong length: %d\n" +
						"from %s", packet.getLength(), (InetSocketAddress)packet.getSocketAddress());
			else {
				byte[] b = packet.getData();
				
				int dataNodeID = Convert.byteArrayToInt(b);
				
				if (connectedDataNodes.addOrRefresh(dataNodeID))
					nameNode.dataNodeConnected(dataNodeID, (InetSocketAddress)packet.getSocketAddress());
			}
		}
	}

	public int getPort() {
		return incomingHeartBeatSocket.getLocalPort();
	}
}
