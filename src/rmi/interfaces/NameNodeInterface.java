package rmi.interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NameNodeInterface extends Remote {
	void receiveMessage(String s) throws RemoteException;
}
