package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataNodeNameNodeProtocol extends Remote{
	int register() throws RemoteException;
	void blockReceived(long blockId) throws RemoteException;
}
