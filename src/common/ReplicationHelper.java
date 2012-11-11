package common;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import common.protocols.DataNodeDataNodeProtocol;
import common.protocols.RemoteBlock;

public class ReplicationHelper {
	/**
	 * 
	 * @param data
	 * @param blockID
	 * @param dataNodes
	 * @return false if the pipeline is empty (which could be because no one responded)
	 */
	public static boolean write(final byte[] data,
			long blockID, List<? extends DataNodeDataNodeProtocol> dataNodes) {
		List<DataNodeDataNodeProtocol> remoteDataNodes = new LinkedList<>(dataNodes);
		RemoteBlock nextBlock = moveToNextBlock(remoteDataNodes, blockID);
		
		while (nextBlock != null) {
			try {
				if (remoteDataNodes.size() > 1)
					nextBlock.writePipeline(data, new LinkedList<DataNodeDataNodeProtocol>(
							remoteDataNodes.subList(1, remoteDataNodes.size())));
				else
					nextBlock.write(data);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				remoteDataNodes.remove(0);
				nextBlock = moveToNextBlock(remoteDataNodes, blockID);
			}
		}
		return false;
	}
	
	private static RemoteBlock moveToNextBlock(
			List<DataNodeDataNodeProtocol> remoteDataNodes, long blockID) {
		while (remoteDataNodes.size() > 0) {
			try {
				return remoteDataNodes.get(0).getOrCreateBlock(blockID);
			} catch (RemoteException e) {
				System.err.println("DataNode unreachable");
			}
			remoteDataNodes.remove(0);
		}
		return null;
	}
}
