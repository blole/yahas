package common;

public class Constants {
	public static final int DEFAULT_NAME_NODE_PORT = 1099;
	public static final int DEFAULT_NAME_NODE_HEARTBEAT_PORT = 1618;
	
	public static final int DEFAULT_DATA_NODE_PORT = 1099;
	
	public static final long DEFAULT_HEARTBEAT_INTERVAL_MS = 500;
	public static final long DEFAULT_HEARTBEAT_TIMEOUT_MS = 2000;
	public static final long DEFAULT_FILE_LEASE_TIME_MS = 5000;
	public static final long DEFAULT_BLOCKREPORT_TIME_MS=60_000;
	
	public static final long DEFAULT_BLOCK_OPEN_TIME_MS = 5000;
	public static final String HASH_FILE_ENDING = ".hash";
	public static final String ID_FILE_NAME = "id.txt";
}
