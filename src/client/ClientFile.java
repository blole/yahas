package client;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.NotDirectoryException;
import java.rmi.RemoteException;
import java.util.List;

import namenode.NameNodeFile;

import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.BlockNotFoundException;
import common.exceptions.FileAlreadyOpenException;
import common.exceptions.FileOrDirectoryAlreadyExistsException;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.protocols.RemoteDataNode;
import common.protocols.RemoteFile;


public class ClientFile implements Serializable, Closeable {
	private static final long serialVersionUID = -1422394544577820093L;
	private RemoteFile remoteFile;
	private boolean iOpenedIt = false;
	
	public ClientFile(NameNodeFile file) throws RemoteException {
		this(file.getStub());
	}
	public ClientFile(RemoteFile remoteFile) {
		this.remoteFile = remoteFile;
	}
	
	
	
	
	
	public byte[] read() throws RemoteException, FileAlreadyOpenException {
		if (!iOpenedIt)
			throw new FileAlreadyOpenException(getPath());
		
		List<ClientBlock> allBlocks = remoteFile.getBlocks();
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		for (ClientBlock block : allBlocks) {
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
	
	public void write(byte[] data) throws RemoteException,
					AllDataNodesAreDeadException, FileAlreadyOpenException {
		if (!iOpenedIt)
			throw new FileAlreadyOpenException(getPath());
		
		for (int alreadyWrittenBytes=0; alreadyWrittenBytes<data.length; ) {
			ClientBlock block = remoteFile.getWritingBlock();
			alreadyWrittenBytes += block.write(data, alreadyWrittenBytes);
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
			throw new FileAlreadyOpenException(getPath());
	}
	
	@Override
	public void close() throws RemoteException {
		if (iOpenedIt) {
			iOpenedIt = false;
			remoteFile.close();
		}
	}
	
	public void move(String to) throws NotDirectoryException, FileOrDirectoryAlreadyExistsException, RemoteException, NoSuchFileOrDirectoryException {
		remoteFile.move(to);
	}
	
	public void delete() throws RemoteException, FileAlreadyOpenException {
		if (iOpenedIt || !remoteFile.isOpen())
			remoteFile.delete();
		else
			throw new FileAlreadyOpenException(getPath());
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
