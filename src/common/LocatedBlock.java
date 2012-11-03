package common;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.RemoteBlockAlreadyOpenException;
import common.protocols.RemoteBlock;
import common.protocols.RemoteDataNode;

public class LocatedBlock implements Serializable {
	private static final long serialVersionUID = 875453156327859620L;
	private final long blockID;
	private boolean initialized = false;
	private List<RemoteDataNode> remoteDataNodes;
	private RemoteBlock firstBlock;
	
	public LocatedBlock(long blockID, List<RemoteDataNode> dataNodes) {
		this.blockID = blockID;
		this.remoteDataNodes = dataNodes;
	}
	
	private void makeSureInitialized() throws AllDataNodesAreDeadException {
		if (!initialized) {
			initialized = true;
			moveToNextFirstBlock();
			while (remoteDataNodes.size() > 0) {
				try {
					firstBlock.replicateTo(remoteDataNodes, 0);
					break;
				} catch (RemoteException e) {
					moveToNextFirstBlock();
				}
			}
		}
	}
	
	public void write(String data) throws AllDataNodesAreDeadException {
		makeSureInitialized();
		
		while (remoteDataNodes.size() > 0) {
			try {
				firstBlock.write(data);
				break;
			} catch (IOException e) {
				moveToNextFirstBlock();
			}
		}
	}
	
	private void moveToNextFirstBlock() throws AllDataNodesAreDeadException {
		while (remoteDataNodes.size() > 0) {
			try {
				firstBlock = remoteDataNodes.get(0).openOrCreateBlock(blockID);
				break;
			} catch (RemoteException e) {
				System.err.println("DataNode unreachable");
			} catch (RemoteBlockAlreadyOpenException e) {
				System.err.println("block already open on remote DataNode, removed from pipeline");
			}
			remoteDataNodes.remove(0);
		}
		
		throw new AllDataNodesAreDeadException();
	}

	public void close() {
		while (remoteDataNodes.size() > 0) {
			try {
				firstBlock.close();
				return;
			} catch (RemoteException e) {
				try {
					moveToNextFirstBlock();
				} catch (AllDataNodesAreDeadException e1) {}
			}
		}
	}
	
	public int getBytesLeft() {
		return 65536;
	}
}
