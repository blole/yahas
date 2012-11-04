package namenode;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import common.Action;
import common.Constants;
import common.LocatedBlock;
import common.RMIHelper;
import common.TimeoutHashSet;
import common.protocols.RemoteFile;

public class NameNodeFile implements RemoteFile {
	private static final Logger LOGGER = Logger.getLogger(
			NameNodeFile.class.getCanonicalName());
	
	private static TimeoutHashSet<NameNodeFile> leasedFiles = 
			new TimeoutHashSet<>(Constants.DEFAULT_FILE_LEASE_TIME, new Action<NameNodeFile>() {
				@Override
				public void execute(NameNodeFile file) {
					file.realClose(true);
				}
			});
	
	
	
	
	

	private NameNode nameNode;
	private String name;
	private final ArrayList<LocatedBlock> blocks = new ArrayList<>();
	private byte replicationFactor;
	private NameNodeDir parentDir = null;




	
	public NameNodeFile(NameNode nameNode, String name, byte replicationFactor) {
		this.nameNode = nameNode;
		this.name = name;
		this.replicationFactor = replicationFactor;
		this.accessed();
		
		LOGGER.debug(String.format("File '%s' opened.\n", name));
	}
	
	public void setParentDir(NameNodeDir parentDir) {
		this.parentDir = parentDir;
	}

	private void accessed() {
		leasedFiles.addOrRefresh(this);
	}

	@Override
	public void delete() throws RemoteException {
		if (parentDir != null)
			parentDir.removeFile(this);
		
		LOGGER.debug(String.format("File '%s' deleted.\n", name));
	}

	@Override
	public void close() throws RemoteException {
		leasedFiles.remove(this);
		realClose(false);
	}
	private void realClose(boolean timedOut) {
		for (LocatedBlock block : blocks)
			block.close();
		
		LOGGER.debug(String.format("File '%s' closed beacuse %s.\n",
				name, timedOut?"lease expired":"of remote call"));
	}

	@Override
	public LocatedBlock addBlock() throws RemoteException {
		accessed();
		LocatedBlock block = new LocatedBlock(nameNode.getNewBlockID(),
				nameNode.getAppropriateDataNodes(replicationFactor));
		blocks.add(block);
		return block;
	}

	@Override
	public LocatedBlock getLastBlock() throws RemoteException {
		accessed();
		return blocks.get(blocks.size()-1);
	}
	
	/**
	 * Will return the last block. If the current last block is full or
	 * if the file doesn't have any blocks yet, it adds a new one first.
	 */
	@Override
	public LocatedBlock getWritingBlock() throws RemoteException {
		accessed();
		if (blocks.size() != 0 && getLastBlock().getBytesLeft() != 0)
			return getLastBlock();
		else
			return addBlock();
	}

	@Override
	public List<LocatedBlock> getBlocks() throws RemoteException {
		accessed();
		return null;
	}

	@Override
	public void renewLease() throws RemoteException {
		accessed();
	}

	@Override
	public void move(String filePathAndName) throws RemoteException {
		accessed();
	}
	
	
	
	

	public boolean isOpen() {
		return leasedFiles.contains(this);
	}
	
	public RemoteFile getStub() throws RemoteException {
		return (RemoteFile) RMIHelper.getStub(this);
	}

	public String getName() {
		return name;
	}
}
