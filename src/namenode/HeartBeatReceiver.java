package namenode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import common.Action;
import common.Constants;
import common.Convert;
import common.TimeoutHashSet;

public class HeartBeatReceiver implements Runnable {
	private DatagramSocket incomingHeartBeatSocket;
	private NameNode reportBackTo;
	
	//Removes an element when a timeout happens
	private TimeoutHashSet<DataNodeImage> connectedDataNodes; 

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
				System.err.printf("Recieved a HeartBeat with the wrong length: %d\n" +
						"from %s", packet.getLength(), (InetSocketAddress)packet.getSocketAddress());
			else {
				byte[] b = packet.getData();
				
				int dataNodeID = Convert.byteArrayToInt(b);
				
				try {
					DataNodeImage dataNodeImage = new DataNodeImage(dataNodeID, (InetSocketAddress)packet.getSocketAddress());
					
					if (connectedDataNodes.addOrRefresh(dataNodeImage))
						reportBackTo.dataNodeConnected(dataNodeImage);
				} catch (MalformedURLException | RemoteException e) {
					e.printStackTrace();
					System.err.printf("DataNode '%d' failed to connect:\n%s\n", dataNodeID, e.getLocalizedMessage());
				} catch (NotBoundException e) {
					System.err.printf("DataNode '%d' failed to connect:\n" +
							"the remote DataNode was not bound in the rmiregistry.\n", dataNodeID);
				}
			}
		}
	}

	public int getPort() {
		return incomingHeartBeatSocket.getLocalPort();
	}
}
