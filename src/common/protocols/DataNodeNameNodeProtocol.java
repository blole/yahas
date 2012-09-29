package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DataNodeNameNodeProtocol extends Remote{
	int register(NameNodeDataNodeProtocol dataNode) throws RemoteException;
	void blockReceived(long blockId) throws RemoteException;
}
