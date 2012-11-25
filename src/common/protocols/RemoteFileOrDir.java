package common.protocols;

import java.nio.file.NotDirectoryException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import common.exceptions.FileOrDirectoryAlreadyExistsException;
import common.exceptions.NoSuchFileOrDirectoryException;

public interface RemoteFileOrDir extends Remote {

	public enum Type {
		File,
		Directory,
	};

	/**
	 * Moves the file or directory to a specified location
	 * 
	 * @param to
	 * @throws RemoteException
	 * @throws RemoteDirNotFoundException 
	 */
	
	void move(String to) throws RemoteException, NotDirectoryException, FileOrDirectoryAlreadyExistsException, NoSuchFileOrDirectoryException;
	
	String getName() throws RemoteException;
	
	String getPath() throws RemoteException;
	
	/**
	 * Returns whether it is a file of a Directory
	 * @return
	 * @throws RemoteException
	 */
	Type getType() throws RemoteException;
	
	
}
