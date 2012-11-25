package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.WriteInfo;

public interface RemoteNameNodeBlock extends Remote {
	WriteInfo initiateWrite(int amount) throws RemoteException;
}
