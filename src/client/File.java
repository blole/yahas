package client;

import java.rmi.RemoteException;

import common.protocols.RemoteFile;


public class File {
	private RemoteFile remoteFile;

	public File(RemoteFile remoteFile) {
		this.remoteFile = remoteFile;
	}

	public void write(String data) {
		
	}
	
	public void delete() throws RemoteException {
		remoteFile.delete();
	}
	
	public void close() throws RemoteException {
		remoteFile.close();
	}
}
