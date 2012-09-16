package datanode;

import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

public class HeartBeatSender implements Runnable {
	
	private DatagramSocket dataNodeHeartBeatSocket;
	private SocketAddress dataNodeHeartBeatSocketAddress;
	private long interval_ms;
	private int id;

	public HeartBeatSender(SocketAddress dataNodeHeartBeatSocketAddress, long interval_ms) throws SocketException {
		dataNodeHeartBeatSocket = new DatagramSocket();
		
		this.dataNodeHeartBeatSocketAddress = dataNodeHeartBeatSocketAddress;
		this.interval_ms = interval_ms;
		this.id = 58;
	}

	@Override
	public void run() {
		byte[] buf = new byte[] {
				(byte) (id>>24),
				(byte) (id>>16),
				(byte) (id>>8),
				(byte) (id),
		};
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		packet.setSocketAddress(dataNodeHeartBeatSocketAddress);
		
		while (true) {
			try {
				dataNodeHeartBeatSocket.send(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				Thread.sleep(interval_ms);
			} catch (InterruptedException e) {}
		}
	}

}
