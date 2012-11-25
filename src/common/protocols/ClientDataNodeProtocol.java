package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.exceptions.BlockAlreadyExistsException;
import common.exceptions.BlockNotFoundException;

/**
 * Interface for the ClientDataNodeProtcol representing the protocol used
 * between the client and the data node
 * 
 * @author Bjorn, Govind, Jerry, Karan
 * 
 */
public interface ClientDataNodeProtocol extends Remote {

	/**
	 * Function to create a block
	 * 
	 * @param blockID
	 *            BlockId of the new Block
	 * @return returns the newly created Block
	 * @throws RemoteException
	 * @throws BlockAlreadyExistsException
	 */
	RemoteBlock createBlock(long blockID) throws RemoteException,
			BlockAlreadyExistsException;

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

	/**
	 * Opens or creates a Block
	 * @param blockID
	 * @return
	 * @throws RemoteException
	 * @throws RemoteBlockAlreadyOpenException
	 */
	RemoteBlock getOrCreateBlock(long blockID) throws RemoteException;
}
