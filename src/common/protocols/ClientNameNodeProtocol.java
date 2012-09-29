package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientNameNodeProtocol extends Remote {
	RemoteFile createFile(String name, byte replicationFactor) throws RemoteException;
	RemoteFile getFile(String name) throws RemoteException;
	
	RemoteDir createDir(String name, boolean createParentsAsNeeded) throws RemoteException;
	RemoteDir getDir(String name) throws RemoteException;
	
	List<ClientDataNodeProtocol> getDataNodes() throws RemoteException;
}
