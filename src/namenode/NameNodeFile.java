package namenode;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import client.YAHASFile;

import common.Action;
import common.Constants;
import common.LocatedBlock;
import common.RMIHelper;
import common.TimeoutHashSet;
import common.exceptions.RemoteFileAlreadyOpenException;
import common.protocols.RemoteFile;

public class NameNodeFile implements RemoteFile {
	private static final Logger LOGGER = Logger.getLogger(
			NameNodeFile.class.getCanonicalName());
	
	private static final TimeoutHashSet<NameNodeFile> leasedFiles = 
			new TimeoutHashSet<>(Constants.DEFAULT_FILE_LEASE_TIME_MS, new Action<NameNodeFile>() {
				@Override
				public void execute(NameNodeFile file) {
					file.closeForReal(true);
				}
			});
	
	
	
	

	private NameNode nameNode;
	private String name;
	private final ArrayList<LocatedBlock> blocks = new ArrayList<>();
	private byte replicationFactor;
	private NameNodeDir parentDir = null;
	private YAHASFile yahasFile;




	
	public NameNodeFile(NameNode nameNode, String name, byte replicationFactor) {
		this.nameNode = nameNode;
		this.name = name;
		this.replicationFactor = replicationFactor;
		
		try {
			this.yahasFile = new YAHASFile(this);
		} catch (RemoteException e) {
			LOGGER.error("Error creating file "+name, e);
		}
	}
	
	public void setParentDir(NameNodeDir parentDir) {
		this.parentDir = parentDir;
	}
	
	
	
	
	
	@Override
	public boolean isOpen() {
		return leasedFiles.contains(this);
	}
	
	@Override
	public void open() throws RemoteFileAlreadyOpenException {
		if (isOpen())
			throw new RemoteFileAlreadyOpenException();
		else {
			LOGGER.debug(String.format("File '%s' opened.", name));
			renewLease();
		}
	}

	@Override
	public void renewLease() {
		leasedFiles.addOrRefresh(this);
	}

	@Override
	public void close() {
		if (leasedFiles.remove(this)) {
			closeForReal(false);
		}
	}
	private void closeForReal(boolean timedOut) {
		LOGGER.debug(String.format("File '%s' closed beacuse %s.",
				name, timedOut?"lease expired":"of remote call"));
	}
	
	
	
	
	
	@Override
	public LocatedBlock addBlock() {
		LocatedBlock block = new LocatedBlock(nameNode.getNewBlockID(),
				nameNode.getAppropriateDataNodes(replicationFactor));
		blocks.add(block);
		return block;
	}
	
	@Override
	public LocatedBlock getLastBlock() {
		return blocks.get(blocks.size()-1);
	}
	
	/**
	 * Will return the last block. If the current last block is full or
	 * if the file doesn't have any blocks yet, it adds a new one first.
	 */
	@Override
	public LocatedBlock getWritingBlock() {
		if (blocks.size() != 0 && getLastBlock().getBytesLeft() != 0)
			return getLastBlock();
		else
			return addBlock();
	}

	@Override
	public List<LocatedBlock> getBlocks() {
		return null;
	}
	
	
	
	
	
	@Override
	public void move(String filePathAndName) throws RemoteException {
		//TODO
	}
	
	@Override
	public void delete() throws RemoteException {
		if (parentDir != null)
			parentDir.removeFile(this);
		
		LOGGER.debug(String.format("File '%s' deleted.", name));
	}
	
	
	
	
	public YAHASFile getYAHASFile() {
		return yahasFile;
	}
	
	public RemoteFile getStub() throws RemoteException {
		return (RemoteFile) RMIHelper.getStub(this);
	}

	public String getName() {
		return name;
	}
}
