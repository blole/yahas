package datanode;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.BlockInterface;

public interface DataNodeInterface extends Remote {
	void receiveMessage(String s) throws RemoteException;
	BlockInterface createBlock(long id) throws RemoteException;
}
