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
import common.exceptions.FileAlreadyOpenException;
import common.protocols.RemoteFile;

public class NameNodeFile extends FileOrDir implements RemoteFile {
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
	private final ArrayList<LocatedBlock> blocks = new ArrayList<>();
	private byte replicationFactor;
	private YAHASFile yahasFile;
	private boolean isOpen;




	
	public NameNodeFile(NameNode nameNode, String name, byte replicationFactor) {
		super(name);
		this.nameNode = nameNode;
		this.replicationFactor = replicationFactor;
		this.isOpen = false;
				
		try {
			this.yahasFile = new YAHASFile(this);
		} catch (RemoteException e) {
			LOGGER.error("Error creating file "+name, e);
		}
	}
	
	
	
	
	
	@Override
	public boolean isOpen() {
		return isOpen;
	}
	
	@Override
	public void open() throws FileAlreadyOpenException {
		if (isOpen())
			throw new FileAlreadyOpenException();
		else {
			LOGGER.debug(toString()+" opened");
			renewLease();
		}
	}

	@Override
	public void renewLease() {
		isOpen = true;
		leasedFiles.addOrRefresh(this);
	}

	@Override
	public void close() {
		if (leasedFiles.remove(this))
			closeForReal(false);
	}
	private void closeForReal(boolean timedOut) {
		isOpen = false;
		LOGGER.debug(toString()+" closed beacuse "+
		(timedOut ? "lease expired" : "of remote call"));
	}
	
	@Override
	public void delete() {
		super.delete();
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
		if (blocks.size() > 0 && getLastBlock().getBytesLeft() > 0)
			return getLastBlock();
		else
			return addBlock();
	}
	
	@Override
	public List<LocatedBlock> getBlocks() {
		return blocks;
	}
	
	@Override
	public Type getType() {
		return Type.File;
	}
	
	@Override
	public String toString() {
		return "[File "+getPath()+"]";
	}
	
	public YAHASFile getYAHASFile() {
		return yahasFile;
	}
	
	public RemoteFile getStub() throws RemoteException {
		return (RemoteFile) RMIHelper.getStub(this);
	}
}
