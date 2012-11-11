package client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.RemoteException;
import java.util.List;

import namenode.NameNodeFile;

import common.LocatedBlock;
import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.BlockNotFoundException;
import common.exceptions.FileAlreadyOpenException;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.protocols.RemoteDataNode;
import common.protocols.RemoteFile;


public class YAHASFile implements Serializable {
	private static final long serialVersionUID = -1422394544577820093L;
	private RemoteFile remoteFile;
	private boolean iOpenedIt = false;
	
	public YAHASFile(NameNodeFile file) throws RemoteException {
		this(file.getStub());
	}
	public YAHASFile(RemoteFile remoteFile) {
		this.remoteFile = remoteFile;
	}
	
	
	
	
	
	public byte[] read() throws RemoteException, FileAlreadyOpenException {
		if (!iOpenedIt)
			throw new FileAlreadyOpenException(); //TODO better exception
		
		List<LocatedBlock> allBlocks = remoteFile.getBlocks();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		for (LocatedBlock block : allBlocks) {
			boolean successful = false;
			for (RemoteDataNode dataNode : block.getRemoteDataNodes()) {
				try {
					byte[] data = dataNode.getBlock(block.getID()).read();
					bytes.write(data);
					successful = true;
					break; //block done
				} catch (RemoteException | BlockNotFoundException e) {
					//this may happen if a DataNode is dead..
				} catch (IOException e) {
					e.printStackTrace(); //this should never happen however
				}
			}
			
			if (!successful)
				throw new RemoteException("Unable to read block "+block);
		}
		return bytes.toByteArray();
	}
	
	public void write(String data) throws RemoteException,
					AllDataNodesAreDeadException, FileAlreadyOpenException {
		if (!iOpenedIt)
			throw new FileAlreadyOpenException();
		
		while (data.length() > 0) {
			LocatedBlock block = remoteFile.getWritingBlock();
			int bytesLeft = block.getBytesLeft();
			
			int split = Math.min(bytesLeft, data.length());
			block.write(data.substring(0, split));
			data = data.substring(split);
		}
	}
	
	public void open() throws RemoteException, FileAlreadyOpenException {
		remoteFile.open();
		iOpenedIt = true;
	}
	
	public void renewLease() throws RemoteException, FileAlreadyOpenException {
		if (iOpenedIt)
			remoteFile.renewLease();
		else if (!remoteFile.isOpen())
			open();
		else
			throw new FileAlreadyOpenException();
	}
	
	public void close() throws RemoteException, FileAlreadyOpenException {
		if (!iOpenedIt)
			throw new FileAlreadyOpenException();
		else {
			iOpenedIt = false;
			remoteFile.close();
		}
	}
	
	public void tryToClose() {
		try {
			close();
		} catch (RemoteException | FileAlreadyOpenException e) {}
	}
	
	public void move(String to) throws NotDirectoryException, FileAlreadyExistsException, RemoteException, NoSuchFileOrDirectoryException {
		remoteFile.move(to);
	}
	
	public void delete() throws RemoteException, FileAlreadyOpenException {
		if (iOpenedIt || !remoteFile.isOpen())
			remoteFile.delete();
		else
			throw new FileAlreadyOpenException();
	}
	
	public String getName() {
		try {
			return remoteFile.getName();
		} catch (RemoteException e) {
			throw new RuntimeException("Error getting file name", e);
		}
	}
	
	public String getPath() {
		try {
			return remoteFile.getPath();
		} catch (RemoteException e) {
			throw new RuntimeException("Error getting file path", e);
		}
	}

	@Override
	public String toString() {
		return String.format("[%s %s]", this.getClass().getCanonicalName(), getName());
	}
}
