package common;

import java.io.Serializable;


public class BlockInfo implements Serializable {
	private static final long serialVersionUID = 733444104553649197L;
	
	public final long blockID;
	private int size;
	
	public BlockInfo(long blockID, int size) {
		this.blockID = blockID;
		this.size = size;
	}
	
	public int size() {
		return size;
	}
}
