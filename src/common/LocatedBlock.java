package common;

import java.io.Serializable;
import java.util.List;

import common.exceptions.AllDataNodesAreDeadException;
import common.protocols.RemoteDataNode;

public class LocatedBlock implements Serializable {
	private static final long serialVersionUID = 875453156327859620L;
	private final long blockID;
	private List<RemoteDataNode> remoteDataNodes;
	
	private ReplicationPipeline replicationPipeline;
	
	public LocatedBlock(long blockID, List<RemoteDataNode> dataNodes) {
		this.blockID = blockID;
		this.remoteDataNodes = dataNodes;
		replicationPipeline = new ReplicationPipeline(blockID);
	}
	
	public void write(String data) throws AllDataNodesAreDeadException {
		boolean a = replicationPipeline.open(remoteDataNodes);
		boolean b = replicationPipeline.write(data);
		if (!a || !b)
			throw new AllDataNodesAreDeadException();
		replicationPipeline.close();
	}
	
	public void close() {
		replicationPipeline.close();
	}
	
	public int getBytesLeft() {
		return 65536;
	}
}
