package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.BlockReport;

/**
 * 
 * Represents a Protocol for Name Node Data Node Communication.
 * 
 * @author Bjorn, Govind, Jerry, Karan
 * 
 */
public interface NameNodeDataNodeProtocol extends Remote {
	/**
	 * Function to get a Block report from Data Node
	 * 
	 * @return
	 * @throws RemoteException
	 */
	BlockReport getBlockReport() throws RemoteException;
	
	/**
	 * Forcefully closes all opened blocks on the DataNode,
	 * used when the NameNode restarts. 
	 * 
	 * @throws RemoteException
	 */
	void closeAllBlocks() throws RemoteException;
}