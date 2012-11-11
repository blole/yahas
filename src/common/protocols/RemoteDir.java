package common.protocols;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NotDirectoryException;
import java.rmi.RemoteException;
import java.util.List;

import namenode.NameNodeFile;
import client.YAHASFile;

import common.exceptions.NoSuchFileOrDirectoryException;
import common.exceptions.NotFileException;

public interface RemoteDir extends RemoteFileOrDir {
	
	/**
	 * Deletes the Directory
	 * 
	 * @param force
	 *            : Set to true to delete child directories
	 * @throws RemoteException
	 * @throws RemoteDirNotEmptyException 
	 * @throws DirectoryNotEmptyException 
	 */
	void delete(boolean force) throws RemoteException, DirectoryNotEmptyException;
	
	NameNodeFile getFile(String path) throws RemoteException, NotDirectoryException, NoSuchFileOrDirectoryException, NotFileException;

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
}
