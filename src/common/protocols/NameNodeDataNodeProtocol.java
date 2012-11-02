package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.BlockReport;

public interface NameNodeDataNodeProtocol extends Remote {
	BlockReport getBlockReport() throws RemoteException;
}