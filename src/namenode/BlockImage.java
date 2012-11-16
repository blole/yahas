package namenode;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import common.BlockInfo;
import common.RMIHelper;
import common.WriteInfo;
import common.exceptions.BlockNotFoundException;
import common.protocols.RemoteNameNodeBlock;

public class BlockImage implements RemoteNameNodeBlock {
	private static final Logger LOGGER = Logger.getLogger(
			BlockImage.class.getCanonicalName());
	
	public final long blockID;
	private int replicationFactor;
	public final int blockSize;
	private int size;
	public final RemoteNameNodeBlock stub;
	public final List<DataNodeImage> dataNodes = new LinkedList<>();
	
	private NameNode nameNode;

	
	public BlockImage(long id, int replicationFactor, int blockSize, NameNode nameNode) {
		this.blockID = id;
		this.replicationFactor = replicationFactor;
		this.blockSize = blockSize;
		this.size = 0;
		this.nameNode = nameNode;
		
		RemoteNameNodeBlock stub;
		try {
			stub = getStub();
		} catch (RemoteException e) {
			LOGGER.error(this+" error creating stub, stub=null", e);
			stub = null;
		}
		this.stub = stub;
	}

	@Override
	public WriteInfo initiateWrite(int amount) {
		amount = Math.min(amount, blockSize-size);
		scheduleIncreaseMaxSize(amount);
		System.out.println(amount);
		return new WriteInfo(amount, nameNode.getAppropriateDataNodes(replicationFactor));
	}
	
	private void scheduleIncreaseMaxSize(final int amount) {
		//TODO: this is not written nicely, and it also causes problems if consecutive
		//writes come too close together... in LinkedList.checkForComodification()
		if (amount >= 0) {
			new Thread() {
				@Override
				public void run() {
					try {
						Thread.sleep(10_000);
					} catch (InterruptedException e) {}
					
					size += amount;
					
					boolean bytesActuallyWritten = false;
					for (DataNodeImage dataNode : dataNodes) {
						BlockInfo oldBlockInfo = dataNode.getBlock(blockID);
						if (oldBlockInfo != null && oldBlockInfo.size() >= size) {
							bytesActuallyWritten = true;
							break;
						}
					}
					if (bytesActuallyWritten) {
						for (DataNodeImage dataNode : dataNodes) {
							BlockInfo oldBlockInfo = dataNode.getBlock(blockID);
							if (oldBlockInfo != null)
								dataNodeGotThisBlock(dataNode, oldBlockInfo);
						}
					}
				}
			}.start();
		}
	}
	
	public void dataNodeGotThisBlock(DataNodeImage dataNode, BlockInfo blockInfo) {
		if (/*nameNode.doneStarting() && */blockInfo.size() < size) {
			try {
				dataNode.getRemoteStub().getBlock(blockInfo.blockID).delete();
			} catch (RemoteException e) {
				LOGGER.error(dataNode+" error deleting block "+blockID, e);
			} catch (BlockNotFoundException e) {
				//that's okay, never mind
			}
			dataNodes.remove(dataNode);
		}
		else
			dataNodes.add(dataNode);
	}

	public void dataNodeLostThisBlock(DataNodeImage dataNode) {
		dataNodes.remove(dataNode);
	}
	
	private RemoteNameNodeBlock getStub() throws RemoteException {
		return (RemoteNameNodeBlock) RMIHelper.getStub(this);
	}
	
	
	
	
	
	@Override
	public String toString() {
		return "[BlockImage "+blockID+"]";
	}
	
	public boolean isFull() {
		return size >= blockSize;
	}
}
