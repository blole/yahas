package client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

import namenode.NameNodeFile;

import common.LocatedBlock;
import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.RemoteBlockNotFoundException;
import common.exceptions.RemoteFileAlreadyOpenException;
import common.protocols.RemoteDataNode;
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
	
	
	
	
	
	public byte[] read() throws RemoteException, RemoteFileAlreadyOpenException {
		if (!iOpenedIt)
			throw new RemoteFileAlreadyOpenException(); //TODO better exception
		
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
				} catch (RemoteException | RemoteBlockNotFoundException e) {
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
					AllDataNodesAreDeadException, RemoteFileAlreadyOpenException {
		if (!iOpenedIt)
			throw new RemoteFileAlreadyOpenException();
		
		while (data.length() > 0) {
			LocatedBlock block = remoteFile.getWritingBlock();
			int bytesLeft = block.getBytesLeft();
			
			int split = Math.min(bytesLeft, data.length());
			block.write(data.substring(0, split));
			data = data.substring(split);
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

	@Override
	public String toString() {
		return String.format("[%s %s]", this.getClass().getCanonicalName(), name);
	}
}
