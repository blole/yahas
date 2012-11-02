package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import common.exceptions.RemoteDirNotEmptyException;
import common.exceptions.RemoteDirNotFoundException;

public interface RemoteDir extends Remote {
	void delete(boolean recursively) throws RemoteException, RemoteDirNotEmptyException;
	void move(String to) throws RemoteException, RemoteDirNotFoundException;
	
	List<RemoteFile> getFiles() throws RemoteException;
	List<RemoteDir> getSubDirs() throws RemoteException;
}
