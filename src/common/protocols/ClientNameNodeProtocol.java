package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import client.GreatFile;

import common.exceptions.RemoteDirNotFoundException;
import common.exceptions.RemoteFileAlreadyOpenException;
import common.exceptions.RemoteFileNotFoundException;

public interface ClientNameNodeProtocol extends Remote {
	GreatFile createFile(String name, byte replicationFactor) throws RemoteException, RemoteDirNotFoundException;
	GreatFile getFile(String name) throws RemoteException,
			RemoteFileNotFoundException, RemoteFileAlreadyOpenException;
	
	RemoteDir createDir(String name, boolean createParentsAsNeeded) throws RemoteException;
	RemoteDir getDir(String name) throws RemoteException, RemoteDirNotFoundException;
	
	List<? extends ClientDataNodeProtocol> getDataNodes() throws RemoteException;
}
