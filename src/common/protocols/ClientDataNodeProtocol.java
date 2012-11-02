package common.protocols;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.exceptions.RemoteBlockAlreadyExistsException;
import common.exceptions.RemoteBlockAlreadyOpenException;
import common.exceptions.RemoteBlockNotFoundException;

public interface ClientDataNodeProtocol extends Remote {
	RemoteBlock createBlock(long blockID) throws RemoteException,
			RemoteBlockAlreadyExistsException;
	RemoteBlock openBlock(long blockID) throws RemoteException,
			RemoteBlockAlreadyOpenException,
			RemoteBlockNotFoundException;
	RemoteBlock openOrCreateBlock(long blockID) throws RemoteException,
			RemoteBlockAlreadyOpenException;
}
