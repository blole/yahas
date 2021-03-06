package common.protocols;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * 
 * Represents a Block present in the DataNode. A client program gets a handle to
 * this remote block and manipulates the same using RMI
 * 
 * @author Bjorn, Govind, Jerry, Karan
 */
public interface RemoteBlock extends Remote {
	
//	/**
//	 * Gets the Id of the RemoteBlock
//	 * 
//	 * @return
//	 * @throws RemoteException
//	 */
//
//	long getID() throws RemoteException;
	
	byte[] read() throws RemoteException;

	/**
	 * Write content specified by s into the block
	 * 
	 * @param data
	 * @throws RemoteException
	 * @throws IOException
	 */
	void write(byte[] data) throws RemoteException, IOException;

	/**
	 * Set this Block to replicate every write to it the specified DataNodes
	 * until it is closed.
	 * 
	 * @param dataNodes A List of Data Nodes to which the blocks needs to be replicated.
	 * @throws RemoteException
	 */
	void writePipeline(byte[] data, List<DataNodeDataNodeProtocol> dataNodes)
			throws RemoteException;

	/**
	 * Delete the Block
	 * 
	 * @throws RemoteException
	 */
	void delete() throws RemoteException;

	void copyTo(List<? extends DataNodeDataNodeProtocol> dataNodes) throws RemoteException;
}
