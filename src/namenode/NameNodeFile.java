package namenode;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import client.ClientBlock;
import client.ClientFile;

import common.Action;
import common.Constants;
import common.RMIHelper;
import common.TimeoutHashSet;
import common.exceptions.FileAlreadyOpenException;
import common.protocols.RemoteFile;

public class NameNodeFile extends NameNodeFileOrDir implements RemoteFile {
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
	private final LinkedList<BlockImage> blocks = new LinkedList<>();
	private byte replicationFactor;
	private ClientFile yahasFile;
	private int blockSize;
	private boolean isOpen;




	
	public NameNodeFile(NameNode nameNode, String name, byte replicationFactor, int blockSize) {
		super(name);
		this.nameNode = nameNode;
		this.replicationFactor = replicationFactor;
		this.blockSize = blockSize;
		this.isOpen = false;
				
		try {
			this.yahasFile = new ClientFile(this);
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
	
	
	
	
	
	private BlockImage addBlock() {
		BlockImage newBlock = nameNode.addNewBlock(replicationFactor, blockSize);
		blocks.add(newBlock);
		return newBlock;
	}
	
	/**
	 * Will return the last block. If the current last block is full or
	 * if the file doesn't have any blocks yet, it adds a new one first.
	 */
	@Override
	public ClientBlock getWritingBlock() {
		BlockImage writingBlock;
		if (blocks.size() > 0 && !blocks.getLast().isFull())
			writingBlock = blocks.getLast();
		else
			writingBlock = addBlock();
		return 	new ClientBlock(writingBlock);
	}
	
	@Override
	public List<ClientBlock> getBlocks() {
		List<ClientBlock> list = new LinkedList<>();
		for (BlockImage block : blocks)
			list.add(new ClientBlock(block));
		return list;
	}
	
	
	
	
	
	@Override
	public Type getType() {
		return Type.File;
	}
	
	@Override
	public String toString() {
		return "[File "+getPath()+"]";
	}
	
	public ClientFile getYAHASFile() {
		return yahasFile;
	}
	
	public RemoteFile getStub() throws RemoteException {
		return (RemoteFile) RMIHelper.getStub(this);
	}
}
