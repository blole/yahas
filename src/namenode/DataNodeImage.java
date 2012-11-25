package namenode;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import common.BlockInfo;
import common.BlockReport;
import common.exceptions.BlockNotFoundException;
import common.protocols.RemoteDataNode;

public class DataNodeImage {
	private static final Logger LOGGER = Logger.getLogger(
			DataNodeImage.class.getCanonicalName());
	
	public final InetSocketAddress receivedHearbeatSocketAddress;
	public final int id;
	public int availableSpace;
	private final RemoteDataNode remoteStub;
	private final HashMap<Long, BlockInfo> blocks = new HashMap<>();

	private NameNode nameNode;

	public DataNodeImage(int dataNodeID, InetSocketAddress socketAddress, NameNode nameNode) throws MalformedURLException, RemoteException, NotBoundException {
		this.receivedHearbeatSocketAddress = socketAddress;
		this.id = dataNodeID;
		this.remoteStub = (RemoteDataNode) Naming.lookup(getRMIAddress());
		this.nameNode = nameNode;
	}
	
	
	
	
	
	public void connected() {
		LOGGER.info(this+" connected");
		retrieveBlockReport();
	}
	public void disconnected() {
		LOGGER.info(this+" disconnected");
		for (BlockInfo block : new ArrayList<>(blocks.values()))
			blockLost(nameNode.getBlock(block.blockID));
	}
	
	
	
	public void retrieveBlockReport() {
		BlockReport blockReport;
		try {
			blockReport = remoteStub.getBlockReport();
		}
		catch (RemoteException e) {
			LOGGER.error(this+" error retrieving BlockReport", e);
			return;
		}
		
		LOGGER.debug(this+" retrieved BlockReport with "+blockReport.blocks.length+" blocks");
		
		for (BlockInfo blockInfo : blockReport.blocks) {
			blockInfoReceived(blockInfo);
			blocks.remove(blockInfo.blockID);
		}
		for (BlockInfo lostBlock : blocks.values()) {
			BlockImage block = nameNode.getBlock(lostBlock.blockID);
			if (block != null)
				block.dataNodeLostThisBlock(this);
		}
		
		blocks.clear();
		for (BlockInfo blockInfo : blockReport.blocks)
			blocks.put(blockInfo.blockID, blockInfo);
	}

	public void blockReceived(BlockInfo blockInfo) {
		blocks.put(blockInfo.blockID, blockInfo);
		blockInfoReceived(blockInfo);
	}
	
	public void blockLost(BlockImage block) {
		if (block != null) {
			blocks.remove(block.blockID);
			block.dataNodeLostThisBlock(this);
		}
	}
	
	private void blockInfoReceived(BlockInfo blockInfo) {
		BlockImage block = nameNode.getBlock(blockInfo.blockID);
		if (block != null) {
			blocks.put(blockInfo.blockID, blockInfo);
			block.dataNodeGotThisBlock(this, blockInfo);
		}
		else {
			blocks.remove(blockInfo.blockID);
			LOGGER.error(this+" deleting block with unknown id "+blockInfo.blockID);
			try {
				remoteStub.getBlock(blockInfo.blockID).delete();
			} catch (RemoteException e) {
				LOGGER.error(this+" error while deleting block with id: "+blockInfo.blockID);
			} catch (BlockNotFoundException e) {
				// that's okay, never mind
			}
		}
	}
	
	
	
	
	
	public BlockInfo getBlock(long blockID) {
		return blocks.get(blockID);
	}
	
	public String getRMIAddress() {
		return 	"//"+receivedHearbeatSocketAddress.getHostString()+"/DataNode"+id;
	}
	
	public RemoteDataNode getRemoteStub() {
		return remoteStub;
	}
	
	@Override
	public String toString() {
		return String.format("[DataNode "+id+"]");
	}
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if((obj == null) || (obj.getClass() != this.getClass()))
//			return false;
//		
//		// object must be DataNodeImage at this point
//		return id == ((DataNodeImage)obj).id;
//	}
//	
//	@Override
//	public int hashCode() {
//		return id;
//		//return 997 * id ^ 991 * receivedHearbeatSocketAddress.hashCode();
//	}
}
