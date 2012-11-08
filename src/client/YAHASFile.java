package client;

import java.io.Serializable;
import java.rmi.RemoteException;

import namenode.NameNodeFile;

import common.LocatedBlock;
import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.RemoteFileAlreadyOpenException;
import common.protocols.RemoteFile;


public class YAHASFile implements Serializable {
	private static final long serialVersionUID = -1422394544577820093L;
	private RemoteFile remoteFile;
	private String name;
	private boolean iOpenedIt = false;
	
	public YAHASFile(NameNodeFile file) throws RemoteException {
		this(file.getName(), file.getStub());
	}
	
	public YAHASFile(String name, RemoteFile remoteFile) {
		this.name = name;
		this.remoteFile = remoteFile;
	}
	
	
	
	
	
	public void write(String data) throws AllDataNodesAreDeadException, RemoteFileAlreadyOpenException {
		if (!iOpenedIt)
			throw new RemoteFileAlreadyOpenException();
		
		try {
			while (data.length() > 0) {
				LocatedBlock block = remoteFile.getWritingBlock();
				int bytesLeft = block.getBytesLeft();
				
				int split = Math.min(bytesLeft, data.length());
				block.write(data.substring(0, split));
				data = data.substring(split);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void open() throws RemoteException, RemoteFileAlreadyOpenException {
		remoteFile.open();
		iOpenedIt = true;
	}
	
	public void renewLease() throws RemoteException, RemoteFileAlreadyOpenException {
		if (iOpenedIt)
			remoteFile.renewLease();
		else if (!remoteFile.isOpen())
			open();
		else
			throw new RemoteFileAlreadyOpenException();
	}
	
	public void close() throws RemoteException, RemoteFileAlreadyOpenException {
		if (!iOpenedIt)
			throw new RemoteFileAlreadyOpenException();
		else {
			iOpenedIt = false;
			remoteFile.close();
		}
	}
	
	public void tryToClose() {
		try {
			close();
		} catch (RemoteException | RemoteFileAlreadyOpenException e) {}
	}
	
	public void delete() throws RemoteException, RemoteFileAlreadyOpenException {
		if (iOpenedIt || !remoteFile.isOpen())
			remoteFile.delete();
		else
			throw new RemoteFileAlreadyOpenException();
	}
	
	public String getName() {
		return name;
	}
}
