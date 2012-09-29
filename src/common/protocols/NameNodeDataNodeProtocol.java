package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import common.BlockReport;

public interface NameNodeDataNodeProtocol extends Remote {
	BlockReport getBlockReport() throws RemoteException;
	
	RemoteBlock getBlock(long blockID) throws RemoteException;
}