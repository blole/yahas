package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import common.exceptions.RemoteDirNotEmptyException;
import common.exceptions.RemoteDirNotFoundException;

public interface RemoteDir extends Remote {

	/**
	 * Deletes the Directory
	 * 
	 * @param recursively
	 *            : Set to true to delete child directories
	 * @throws RemoteException
	 * @throws RemoteDirNotEmptyException 
	 */
	void delete(boolean recursively) throws RemoteException, RemoteDirNotEmptyException;

	/**
	 * Moves a directory to the specified location
	 * 
	 * @param to
	 * @throws RemoteException
	 * @throws RemoteDirNotFoundException 
	 */
	void move(String to) throws RemoteException, RemoteDirNotFoundException;

		

	/**
	 * To return the files under a particular directory
	 * 
	 * @return
	 * @throws RemoteException
	 */
	List<RemoteFile> getFiles() throws RemoteException;

	/**
	 * Get the Child Directories of a particular Directory
	 * 
	 * @return
	 * @throws RemoteException
	 */
	List<RemoteDir> getSubDirs() throws RemoteException;
}
