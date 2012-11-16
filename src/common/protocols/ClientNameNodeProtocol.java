package common.protocols;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import client.ClientFile;

import common.exceptions.BadFileName;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.exceptions.NotFileException;

/**
 * 
 * Interface for the protocol between Client and NameNode. The Protocol is used
 * to a get a file handle for RemoteFile. Once a file handle for remoteFile is
 * obtained all operation on file can be performed
 * 
 * @author Bjorn,Govind, Jerry, Karan
 */
public interface ClientNameNodeProtocol extends Remote {

	/**
	 * Creates a File
	 * @param name
	 * 	Name of the File
	 * @param replicationFactor
	 * 	Replication Factor for the file
	 * @return
	 * @throws RemoteException
	 * @throws FileAlreadyExistsException 
	 * @throws NoSuchFileOrDirectoryException 
	 * @throws NotDirectoryException 
	 * @throws BadFileName 
	 * @throws RemoteDirNotFoundException
	 */
	ClientFile createFile(String name, byte replicationFactor, int blockSize) throws RemoteException, FileAlreadyExistsException, NotDirectoryException, NoSuchFileOrDirectoryException, BadFileName;

	/**
	 * Get the FileHandle
	 * @param name
	 * @return
	 * @throws RemoteException
	 * @throws NotFileException 
	 * @throws NoSuchFileOrDirectoryException 
	 * @throws NotDirectoryException 
	 * @throws RemoteFileNotFoundException
	 */
	ClientFile getFile(String name) throws RemoteException, NotDirectoryException,
					NoSuchFileOrDirectoryException, NotFileException;


	/**
	 * Creates a directory and gets Handle to a RemoteDir
	 * @param name
	 * @param createParentsAsNeeded
	 * @return
	 * 	Returns a handleToRemoteDir, or null if one of the parents
	 *  in the path did not exist. Never returns null if
	 *  createParentsAsNeeded is set to true.
	 * @throws RemoteException
	 * @throws RemoteDirNotFoundException 
	 * @throws FileAlreadyExistsException 
	 * @throws NotDirectoryException 
	 * @throws NoSuchFileOrDirectoryException 
	 */
	RemoteDir createDir(String path, boolean createParentsAsNeeded)
			throws RemoteException, NotDirectoryException, FileAlreadyExistsException, NoSuchFileOrDirectoryException;
	
	/**
	 * Gets and Existing Dir.
	 * @param name
	 * @return
	 * @throws RemoteException
	 * @throws RemoteDirNotFoundException
	 * @throws NoSuchFileOrDirectoryException 
	 * @throws NotDirectoryException 
	 */
	RemoteDir getDir(String name) throws RemoteException, NotDirectoryException, NoSuchFileOrDirectoryException;
}
