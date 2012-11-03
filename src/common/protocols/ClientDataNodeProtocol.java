package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.exceptions.RemoteBlockAlreadyExistsException;
import common.exceptions.RemoteBlockAlreadyOpenException;
import common.exceptions.RemoteBlockNotFoundException;

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
	 * @throws RemoteBlockAlreadyExistsException
	 */
	RemoteBlock createBlock(long blockID) throws RemoteException,
			RemoteBlockAlreadyExistsException;

	/**
	 * Opens an Existing Block and throws an exception if it does not find it
	 * 
	 * @param blockID
	 * @return
	 * @throws RemoteException
	 * @throws RemoteBlockAlreadyOpenException
	 * @throws RemoteBlockNotFoundException
	 */
	RemoteBlock openBlock(long blockID) throws RemoteException,
			RemoteBlockAlreadyOpenException, RemoteBlockNotFoundException;

	/**
	 * Opens or creates a Block
	 * @param blockID
	 * @return
	 * @throws RemoteException
	 * @throws RemoteBlockAlreadyOpenException
	 */
	RemoteBlock openOrCreateBlock(long blockID) throws RemoteException,
			RemoteBlockAlreadyOpenException;
}
