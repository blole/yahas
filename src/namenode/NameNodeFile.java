package namenode;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import common.LocatedBlock;
import common.RMIHelper;
import common.protocols.RemoteFile;

public class NameNodeFile implements RemoteFile {
	
	private NameNode nameNode;
	private String name;
	private final ArrayList<LocatedBlock> blocks = new ArrayList<>();
	private byte replicationFactor;
	
	public NameNodeFile(NameNode nameNode, String name, byte replicationFactor) {
		this.nameNode = nameNode;
		this.name = name;
		this.replicationFactor = replicationFactor;
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
		LocatedBlock block = new LocatedBlock(nameNode.getNewBlockID(),
				nameNode.getAppropriateDataNodes(replicationFactor));
		blocks.add(block);
		return block;
	}

	@Override
	public LocatedBlock getLastBlock() throws RemoteException {
		return blocks.get(blocks.size()-1);
	}
	
	/**
	 * Will return the last block. If the current last block is full or
	 * if the file doesn't have any blocks yet, it adds a new one first.
	 */
	@Override
	public LocatedBlock getWritingBlock() throws RemoteException {
		if (blocks.size() != 0 && getLastBlock().getBytesLeft() != 0)
			return getLastBlock();
		else
			return addBlock();
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
		return (RemoteFile) RMIHelper.getStub(this);
	}

	public String getName() {
		return name;
	}
}
