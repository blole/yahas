package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import common.LocatedBlock;
import common.exceptions.RemoteFileAlreadyOpenException;

/**
 * Represents a Remote File in YAHAS system. 
 * The notion of file is only with NameNode
 * 
 * @author Bjorn, Govind, Jerry, Karan
 */
public interface RemoteFile extends Remote {
	/**
	 * Function to add a block to a file.
	 * 
	 * @return The Handle for the NewBlock. The LocatedBlock also contains a
	 *         list of DataNodes also where this block is replicated.
	 * @throws RemoteException
	 */
	LocatedBlock addBlock() throws RemoteException;

	/**
	 * Function to get the Last Block
	 * 
	 * @return
	 * @throws RemoteException
	 */
	LocatedBlock getLastBlock() throws RemoteException;

	/**
	 * Get The a Block With Writing
	 * @return
	 * @throws RemoteException
	 */
	LocatedBlock getWritingBlock() throws RemoteException;
	
	void open() throws RemoteException, RemoteFileAlreadyOpenException;

	/**
	 * Function to renew the lease on the file
	 * 
	 * @throws RemoteException
	 */
	void renewLease() throws RemoteException;

	/**
	 * Function called to close the File.
	 * 
	 * @throws RemoteException
	 */
	void close() throws RemoteException;

	/**
	 * Function to move the file to a new location
	 * 
	 * @param filePathAndName
	 *            : Destination of the file
	 * @throws RemoteException
	 */
	void move(String filePathAndName) throws RemoteException;

	/**
	 * Function to Delete a File
	 * 
	 * @throws RemoteException
	 */
	void delete() throws RemoteException;

	/**
	 * Gets the Blocks associated with this File.
	 * 
	 * @return A list of LocatedBlocks which contains a mapping between Blocks
	 *         and DataNodes which has these Nodes.
	 * @throws RemoteException
	 */
	List<LocatedBlock> getBlocks() throws RemoteException;
	
	boolean isOpen() throws RemoteException;
}
