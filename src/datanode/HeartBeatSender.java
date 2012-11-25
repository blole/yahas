package datanode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;

import common.Convert;

public class HeartBeatSender implements Runnable {
	
	private DatagramSocket dataNodeHeartBeatSocket;
	private SocketAddress dataNodeHeartBeatSocketAddress;
	private long interval_ms;
	private int id;

	public HeartBeatSender(SocketAddress dataNodeHeartBeatSocketAddress, long interval_ms, int id) throws SocketException {
		dataNodeHeartBeatSocket = new DatagramSocket();
		
		this.dataNodeHeartBeatSocketAddress = dataNodeHeartBeatSocketAddress;
		this.interval_ms = interval_ms;
		this.id = id;
	}

	@Override
	public void run() {
		byte[] buf = Convert.toByteArray(id);
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

	public long getInterval() {
		return interval_ms;
	}

}
