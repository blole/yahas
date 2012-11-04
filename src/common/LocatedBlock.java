package common;

import java.io.Serializable;
import java.util.List;

import common.exceptions.AllDataNodesAreDeadException;
import common.protocols.RemoteDataNode;

public class LocatedBlock implements Serializable {
	private static final long serialVersionUID = 875453156327859620L;
	private final long blockID;
	private List<RemoteDataNode> remoteDataNodes;
	
	public LocatedBlock(long blockID, List<RemoteDataNode> dataNodes) {
		this.blockID = blockID;
		this.remoteDataNodes = dataNodes;
	}
	
	public void write(String data) throws AllDataNodesAreDeadException {
		if (!ReplicationHelper.write(data, blockID, remoteDataNodes))
			throw new AllDataNodesAreDeadException();
	}
	
	public int getBytesLeft() {
		return 65536;
	}
}
