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
	private NameNode reportBackTo;
	private TimeoutHashSet<DataNodeImage> connectedDataNodes; //TODO: replace ArrayList with a HashSomething

	public HeartBeatReceiver(NameNode reportBackTo, int heartBeatPort) {
		try {
			incomingHeartBeatSocket = new DatagramSocket(heartBeatPort);
		} catch (SocketException e) {
			System.err.printf("Could not bind to HeartBeat port %d: %s\n", heartBeatPort, e.getLocalizedMessage());
			System.exit(1);
		}
		
		this.reportBackTo = reportBackTo;
	}

	@Override
	public void run() {
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		
		connectedDataNodes = new TimeoutHashSet<DataNodeImage>(
				Constants.DEFAULT_HEARTBEAT_TIMEOUT_MS,
				new Action<DataNodeImage>(){
					@Override
					public void execute(DataNodeImage dataNodeImage) {
						reportBackTo.dataNodeDisconnected(dataNodeImage);
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
				System.err.printf("Recieved a HeartBeat with the wrong length: %d\n", packet.getLength());
			else {
				byte[] b = packet.getData();
				
				int dataNodeID = Convert.byteArrayToInt(b);
				DataNodeImage dataNodeImage = new DataNodeImage(dataNodeID, (InetSocketAddress)packet.getSocketAddress());
				
				if (connectedDataNodes.addOrRefresh(dataNodeImage))
					reportBackTo.dataNodeConnected(dataNodeImage);
			}
		}
	}

	public int getPort() {
		return incomingHeartBeatSocket.getLocalPort();
	}
}
