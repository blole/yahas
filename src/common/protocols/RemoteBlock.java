package common.protocols;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteBlock extends Remote {
	long getID() throws RemoteException;
	int getPreferredBlockSize() throws RemoteException;
	int getRemainingSize() throws RemoteException;
	
	void write(String s) throws RemoteException, IOException;
	
	void replicateTo(List<DataNodeDataNodeProtocol> dataNodes) throws RemoteException;
	void delete() throws RemoteException;
	void close() throws RemoteException;
}
