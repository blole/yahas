package common;

public class Packet {
	public enum PacketType {
		REPLICATION_INITIATION(20),
		REPLICATION_ORDER(100);
		
		private PacketType(int id) {
			
		}
	}
	
	public Packet (byte[] message) {
		
	}
}
