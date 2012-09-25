package rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashSet;

import datanode.DataNodeInterface;

public interface NameNodeInterface extends Remote {
	void receiveMessage(String s) throws RemoteException;
	HashSet<DataNodeInterface> getDataNodes() throws RemoteException;
}
