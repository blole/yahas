package common;

import java.util.List;

import javax.naming.InitialContext;

import common.protocols.RemoteBlock;
import common.protocols.RemoteDataNode;

public class LocatedBlock {
	private final long id;
	private boolean initialized = false;
	private List<RemoteDataNode> remoteDataNodes;
	
	public LocatedBlock(long blockID, List<RemoteDataNode> dataNodes) {
		this.id = blockID;
	}
	
	private void makeSureInitialized() {
		if (!initialized) {
			initialized = true;
			//TODO: init
		}
	}
	
	public void write(String data) {
		makeSureInitialized();
	}
	
	public void close() {
		
	}
}
