package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import namenode.NameNodeFile;
import client.YAHASFile;

import common.exceptions.RemoteDirNotEmptyException;
import common.exceptions.RemoteDirNotFoundException;
import common.exceptions.RemoteFileNotFoundException;

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

		

	NameNodeFile getFile(String path) throws RemoteException, RemoteFileNotFoundException;

	/**
	 * To return the files under a particular directory
	 * 
	 * @return
	 * @throws RemoteException
	 */
	List<YAHASFile> getFiles() throws RemoteException;

	/**
	 * Get the Child Directories of a particular Directory
	 * 
	 * @return
	 * @throws RemoteException
	 */
	List<RemoteDir> getSubDirs() throws RemoteException;

	String getName() throws RemoteException;

	String getPath() throws RemoteException;
}
