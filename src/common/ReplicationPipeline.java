package common;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import common.exceptions.RemoteBlockAlreadyOpenException;
import common.protocols.DataNodeDataNodeProtocol;
import common.protocols.RemoteBlock;

public class ReplicationPipeline implements Serializable {
	private List<DataNodeDataNodeProtocol> remoteDataNodes = new LinkedList<>();
	private long blockID;
	
	private RemoteBlock nextBlock;

	public ReplicationPipeline(long blockID) {
		this.blockID = blockID;
	}
	
	
	
	
	
	/**
	 * @return false if the pipeline is empty (which could be because none of them responded)
	 */
	public boolean open(List<? extends DataNodeDataNodeProtocol> dataNodes) {
		remoteDataNodes.clear();
		remoteDataNodes.addAll(dataNodes);
		moveToNextBlock();
		return 	runOnFirstPossibleDataNode(new ExceptionThrowingAction() {public void run() throws Exception {
				if (remoteDataNodes.size() > 1)
					getNextBlock().replicateTo(remoteDataNodes.subList(1, remoteDataNodes.size()));
			}});
	}
	
	/**
	 * @param data
	 * @return false if the pipeline is empty (which could be because none of them responded)
	 */
	public boolean write(final String data) {
		return runOnFirstPossibleDataNode(new ExceptionThrowingAction() {public void run() throws Exception {
			getNextBlock().write(data);
		}});
	}

	public void close() {
		if (nextBlock != null) { //it could be the case that we haven't opened the pipeline yet.
			runOnFirstPossibleDataNode(new ExceptionThrowingAction() {public void run() throws Exception {
					getNextBlock().close();
			}});
			nextBlock = null;
		}
	}
	
	
	
	
	
	private RemoteBlock getNextBlock() {
		return nextBlock;
	}

	/**
	 * @param action Dangerous operation to run on the next possible DataNode 
	 * @return true if successfully run on a DataNode,
	 * false if we exhausted our list of DataNodes to replicate to.
	 */
	public boolean runOnFirstPossibleDataNode(ExceptionThrowingAction action) {
		while (remoteDataNodes.size() > 0) {
			try {
				action.run();
				return true;
			} catch (Exception e) {
				moveToNextBlock();
			}
		}
		return false;
	}
	
	private void moveToNextBlock() {
		while (remoteDataNodes.size() > 0) {
			try {
				nextBlock = remoteDataNodes.get(0).openOrCreateBlock(blockID);
				return;
			} catch (RemoteException e) {
				e.printStackTrace();
				System.err.println("DataNode unreachable");
			} catch (RemoteBlockAlreadyOpenException e) {
				System.err.println("block already open on remote DataNode, removed from pipeline");
			}
			remoteDataNodes.remove(0);
		}
		
		nextBlock = null;
	}
}
