package common;

public class Packet {
	public static Packet RequestBlockReport = new Packet(PacketType.REQUEST_BLOCK_REPORT);
	
	public final PacketType type;
	public final byte[] message;
	
	public Packet(PacketType type) {
		this(type, new byte[0]);
	}
	
	public Packet(PacketType type, byte[] message) {
		this.type = type;
		this.message = message;
	}
}
