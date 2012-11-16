package client;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import namenode.BlockImage;
import namenode.DataNodeImage;

import common.ReplicationHelper;
import common.WriteInfo;
import common.exceptions.AllDataNodesAreDeadException;
import common.protocols.RemoteDataNode;
import common.protocols.RemoteNameNodeBlock;

public class ClientBlock implements Serializable {
	private static final long serialVersionUID = 875453156327859620L;
	private final long blockID;
	private final List<RemoteDataNode> remoteDataNodes = new LinkedList<>();
	private RemoteNameNodeBlock remoteNameNodeBlock;
	
	public ClientBlock(BlockImage blockImage) {
		this.blockID = blockImage.blockID;
		this.remoteNameNodeBlock = blockImage.stub;
		for (DataNodeImage dataNode : blockImage.dataNodes)
			this.remoteDataNodes.add(dataNode.getRemoteStub());
	}

	/**
	 * 
	 * @param data
	 * @param startIndex
	 * @return the number of bytes of {@code data} actually written
	 * @throws AllDataNodesAreDeadException
	 * @throws RemoteException
	 */
	public int write(byte[] data, int from) throws RemoteException, AllDataNodesAreDeadException {
		return write(data, from, data.length);
	}
	
	public int write(byte[] data, int from, int to) throws RemoteException, AllDataNodesAreDeadException {
		WriteInfo writeInfo = remoteNameNodeBlock.initiateWrite(to-from);
		byte[] dataPortion = Arrays.copyOfRange(data, from, writeInfo.reservedBytes);
		if (!ReplicationHelper.write(dataPortion, blockID, writeInfo.remoteDataNodes))
			throw new AllDataNodesAreDeadException();
		else
			return writeInfo.reservedBytes;
	}
	
	
	
	public long getID() {
		return blockID;
	}
	
	public List<RemoteDataNode> getRemoteDataNodes() {
		return remoteDataNodes;
	}
}
