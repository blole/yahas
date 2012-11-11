package common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class BlockReport implements Serializable {
	private static final long serialVersionUID = 4940592183358404760L;
	public Set<Long> blockIDs;
	
	public BlockReport(Set<Long> blockIDs) {
		this.blockIDs = new HashSet<Long>(blockIDs);
	}
}
