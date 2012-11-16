package common.protocols;

import java.io.Closeable;
import java.rmi.RemoteException;
import java.util.List;

import client.ClientBlock;

import common.exceptions.FileAlreadyOpenException;

/**
 * Represents a Remote File in YAHAS system. 
 * The notion of file is only with NameNode
 * 
 * @author Bjorn, Govind, Jerry, Karan
 */
public interface RemoteFile extends RemoteFileOrDir, Closeable {
	
	void open() throws RemoteException, FileAlreadyOpenException;
	
	boolean isOpen() throws RemoteException;

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
	 * Function to Delete a File
	 * 
	 * @throws RemoteException
	 */
	void delete() throws RemoteException;
	
	
	
	
	
//	/**
//	 * Function to add a block to a file.
//	 * 
//	 * @return The Handle for the NewBlock. The LocatedBlock also contains a
//	 *         list of DataNodes also where this block is replicated.
//	 * @throws RemoteException
//	 */
//	ClientBlock addBlock() throws RemoteException;
//
//	/**
//	 * Function to get the Last Block
//	 * 
//	 * @return
//	 * @throws RemoteException
//	 */
//	ClientBlock getLastBlock() throws RemoteException;

	/**
	 * Get The a Block With Writing
	 * @return
	 * @throws RemoteException
	 */
	ClientBlock getWritingBlock() throws RemoteException;
	
	/**
	 * Gets the Blocks associated with this File.
	 * 
	 * @return A list of LocatedBlocks which contains a mapping between Blocks
	 *         and DataNodes which has these Nodes.
	 * @throws RemoteException
	 */
	List<ClientBlock> getBlocks() throws RemoteException;
}
