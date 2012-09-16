package common.packets;

import java.net.DatagramPacket;

public class ReplicationInitiationPacket {
	byte[] recieveData = new byte[1024];
	public ReplicationInitiationPacket() {
		DatagramPacket packet = new DatagramPacket(recieveData, recieveData.length);
	}
}
