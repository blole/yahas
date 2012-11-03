package client;

import java.io.Serializable;
import java.rmi.RemoteException;

import namenode.NameNodeFile;

import common.LocatedBlock;
import common.exceptions.AllDataNodesAreDeadException;
import common.protocols.RemoteFile;


public class YAHASFile implements Serializable {
	private static final long serialVersionUID = -1422394544577820093L;
	private RemoteFile remoteFile;
	
	public YAHASFile(NameNodeFile file) throws RemoteException {
		this(file.getStub());
	}
	
	public YAHASFile(RemoteFile remoteFile) {
		this.remoteFile = remoteFile;
	}
	
	
	
	
	
	public void write(String data) throws AllDataNodesAreDeadException {
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
	
	public void close() throws RemoteException {
		remoteFile.close();
	}
	
	public void delete() throws RemoteException {
		remoteFile.delete();
	}
}
