package namenode;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import common.LocatedBlock;
import common.protocols.RemoteFile;

public class NameNodeFile implements RemoteFile {
	private NameNode nameNode;
	private String name;
	
	public NameNodeFile(NameNode nameNode, String name, byte replicationFactor) {
		this.nameNode = nameNode;
		this.name = name;
	}

	@Override
	public void delete() throws RemoteException {
		System.out.println("delete");
	}

	@Override
	public void close() throws RemoteException {
	}

	@Override
	public LocatedBlock addBlock() throws RemoteException {
		return new LocatedBlock(nameNode.getNewBlockID(), null);
	}

	@Override
	public LocatedBlock getLastBlock() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LocatedBlock> getBlocks() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void renewLease() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void move(String filePathAndName) throws RemoteException {
		// TODO Auto-generated method stub
		
	}
	
	
	
	
	
	public RemoteFile getStub() throws RemoteException {
		return (RemoteFile) UnicastRemoteObject.exportObject(this, 0);
	}

	public String getName() {
		return name;
	}
}
