package common.protocols;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import common.exceptions.NoSuchFileOrDirectoryException;

public interface RemoteFileOrDir extends Remote {

	/**
	 * Moves the file or directory to a specified location
	 * 
	 * @param to
	 * @throws RemoteException
	 * @throws RemoteDirNotFoundException 
	 */
	void move(String to) throws RemoteException, NotDirectoryException, FileAlreadyExistsException, NoSuchFileOrDirectoryException;
	
	String getName() throws RemoteException;
	
	String getPath() throws RemoteException;
}
