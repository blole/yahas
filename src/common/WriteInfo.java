package common;

import java.io.Serializable;
import java.util.LinkedList;

import common.protocols.RemoteDataNode;

public class WriteInfo implements Serializable {
	private static final long serialVersionUID = 4804085118105665496L;
	
	public final int reservedBytes;
	public final LinkedList<RemoteDataNode> remoteDataNodes;
	
	public WriteInfo (int reservedBytes, LinkedList<RemoteDataNode> remoteDataNodes) {
		this.reservedBytes = reservedBytes;
		this.remoteDataNodes = remoteDataNodes;
	}
}
