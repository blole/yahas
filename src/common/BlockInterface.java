package common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BlockInterface extends Remote {
	void write(String s) throws RemoteException;
}
