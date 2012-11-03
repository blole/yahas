package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import client.YAHASFile;

import common.exceptions.RemoteDirNotFoundException;
import common.exceptions.RemoteFileAlreadyOpenException;
import common.exceptions.RemoteFileNotFoundException;

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
 * @throws RemoteDirNotFoundException
 */
	YAHASFile createFile(String name, byte replicationFactor)
			throws RemoteException, RemoteDirNotFoundException;

/**
 * Get the FileHandle
 * @param name
 * @return
 * @throws RemoteException
 * @throws RemoteFileNotFoundException
 */
	YAHASFile getFile(String name) throws RemoteException,
			RemoteFileNotFoundException, RemoteFileAlreadyOpenException;


	/**
	 * Creates a directory and gets Handle to a RemoteDir
	 * @param name
	 * @param createParentsAsNeeded
	 * @return
	 * 	Returns a handleToRemoteDir
	 * @throws RemoteException
	 */
	RemoteDir createDir(String name, boolean createParentsAsNeeded)
			throws RemoteException;

	/**
	 * Gets and Existing Dir.
	 * @param name
	 * @return
	 * @throws RemoteException
	 * @throws RemoteDirNotFoundException
	 */
	RemoteDir getDir(String name) throws RemoteException,
			RemoteDirNotFoundException;

	/**
	 * Gets DataNodes
	 * @return
	 * @throws RemoteException
	 */
	List<? extends ClientDataNodeProtocol> getDataNodes()
			throws RemoteException;
}
