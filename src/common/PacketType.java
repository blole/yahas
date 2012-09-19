package common;

import common.exceptions.InvalidPacketType;

public enum PacketType {
	TEXT,
	BLOCK_REPORT,
	REQUEST_BLOCK_REPORT,
	REPLICATION_INITIATION,
	REPLICATION_ORDER;
	
	private static PacketType[] values = PacketType.values(); 
	
	public static PacketType decode(int type) throws InvalidPacketType {
		try{
			return values[type];
		}
		catch (IndexOutOfBoundsException e) {
			throw new InvalidPacketType("Invalid "+PacketType.class+" integer");
		}
	}
	
	/**
	 * Alias of ordinal()
	 */
	public int encode() {
		return ordinal();
	}
}
