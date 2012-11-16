package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.BlockReport;
import common.exceptions.BlockNotFoundException;

/**
 * 
 * Represents a Protocol for Name Node Data Node Communication.
 * 
 * @author Bjorn, Govind, Jerry, Karan
 * 
 */
public interface NameNodeDataNodeProtocol extends Remote {
	/**
	 * Function to get a Block report from Data Node
	 * 
	 * @return
	 * @throws RemoteException
	 */
	BlockReport getBlockReport() throws RemoteException;
	
	/**
	 * Opens an Existing Block and throws an exception if it does not find it
	 * 
	 * @param blockID
	 * @return
	 * @throws RemoteException
	 * @throws RemoteBlockAlreadyOpenException
	 * @throws BlockNotFoundException
	 */
	RemoteBlock getBlock(long blockID) throws RemoteException,
			BlockNotFoundException;
}