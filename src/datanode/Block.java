package datanode;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import common.protocols.DataNodeDataNodeProtocol;
import common.protocols.RemoteBlock;

public class Block implements RemoteBlock {
	private long id;
	
	public Block(long id) {
		this.id = id;
	}
	
	@Override
	public void write(String s) throws RemoteException, IOException {
		
	}

	@Override
	public long getID() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getPreferredBlockSize() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getRemainingSize() throws RemoteException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void replicateTo(List<DataNodeDataNodeProtocol> dataNodes)
			throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete() throws RemoteException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws RemoteException {
		// TODO Auto-generated method stub
		
	}
}
