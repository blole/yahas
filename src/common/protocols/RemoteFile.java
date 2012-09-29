package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import common.LocatedBlock;

import client.Block;


public interface RemoteFile extends Remote {
	LocatedBlock addBlock() throws RemoteException;
	LocatedBlock getLastBlock() throws RemoteException;
	void renewLease() throws RemoteException;
	void close() throws RemoteException;
	
	void move(String filePathAndName) throws RemoteException;
	void delete() throws RemoteException;
	List<LocatedBlock> getBlocks() throws RemoteException;
}
