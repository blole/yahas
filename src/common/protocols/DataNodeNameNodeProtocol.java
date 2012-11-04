package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Representation of the protocol used between Data Node and Name Node
 * 
 * @author Bjorn, Govind, Jerry, Karan
 * 
 */
public interface DataNodeNameNodeProtocol extends Remote {
	/**
	 * Registers a DataNode
	 * 
	 * @param dataNode
	 *            A DataNode to register
	 * @return a new DataNode Id if successfull.
	 *         failure.
	 * @throws RemoteException
	 */
	int register() throws RemoteException;
	
	/**
	 * Function to indicate a Block recieved. Based on this the NameNode can
	 * create an internal representation of the Blocks in the YAHAS Cluster
	 * 
	 * @param blockId ID of the Block
	 * @throws RemoteException
	 */

	void blockReceived(long blockId) throws RemoteException;
	
	
	
}
