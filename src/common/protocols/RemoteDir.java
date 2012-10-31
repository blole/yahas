package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteDir extends Remote {
	void delete(boolean recursively) throws RemoteException;
	void move(String to) throws RemoteException;
	
	List<RemoteFile> getFiles() throws RemoteException;
	List<RemoteDir> getSubDirs() throws RemoteException;
}
