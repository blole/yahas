package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ClientDataNodeProtocol extends Remote {
	RemoteBlock createBlock(long blockID) throws RemoteException;
	RemoteBlock getBlock(long blockID) throws RemoteException;
}
