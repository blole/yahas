package datanode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import common.RMIHelper;
import common.exceptions.RemoteBlockAlreadyOpenException;
import common.protocols.DataNodeDataNodeProtocol;
import common.protocols.RemoteBlock;

public class Block implements RemoteBlock {
	
	private static final Logger LOGGER = Logger.getLogger(
			Block.class.getCanonicalName());
	
	
	
	public final long blockID;
	private File file;
	private File hashFile;
	private boolean open;

	private List<? extends DataNodeDataNodeProtocol> replicationPipeline;
	private int myIndex;
	private RemoteBlock nextBlockInReplicationPipeline;

	private BlockManager manager;
	
	
	
	
	/**
	 * Creates a new block and creates the files for it too.
	 * If a block with that ID already existed in the directory, that one is deleted!
	 * 
	 * @param blockID
	 * @throws IOException 
	 */
	public Block(long blockID, BlockManager blockManager) throws RemoteException {
		this(blockID,
				new File(blockManager.blockDir, ""+blockID),
				new File(blockManager.blockDir, blockID+".hash"),
				blockManager);
		
		if (file.delete()) //not created successfully
			LOGGER.debug(String.format("Deleting block %d to make way for a new block with the same ID.", blockID));
		try {
			file.createNewFile();
			hashFile.createNewFile();
		} catch (IOException e) {
			String errormessage = "Error creating new block "+blockID;
			LOGGER.error(errormessage, e);
			throw new RemoteException(errormessage, e);
		}
	}
	
	public Block(long blockID, File file, File hashFile, BlockManager manager) {
		this.blockID = blockID;
		this.file = file;
		this.hashFile = hashFile;
		this.manager = manager;
	}
	
	
	
	
	
	@Override
	public void write(String data) throws RemoteException {
		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter writer = new FileWriter(file);

			writer.append(data);
			writer.close();


			if (hashFile.exists() == false) {
				hashFile.createNewFile();
			}
			FileWriter hashWriter = new FileWriter(hashFile);
			String hashVal = DigestUtils.md5Hex(data);
			hashWriter.append(hashVal);
			LOGGER.debug("Calculated MD5 Hash of " + data + "as " + hashVal);
			LOGGER.debug("Updating BlockReport...");
			hashWriter.close();
			

		} catch (IOException e) {
			LOGGER.error(e);
			throw new RemoteException("error while appending to block "
					+ blockID, e);

		}

		replicateToNext(data);
	}

	private void replicateToNext(String data) {
		while (nextBlockInReplicationPipeline != null) {
			try {
				nextBlockInReplicationPipeline.write(data);
				break;
			} catch (IOException e) {
				nextBlockInReplicationPipeline = getNextBlockInPipeline();
			}
		}
	}
	
	
	
	
	
	public boolean isOpen() {
		return open;
	}
	
	public void open() {
		open = true;
		LOGGER.debug(toString()+" opened.");
	}

	@Override
	public void close() throws RemoteException {
		forceClose();
		while (nextBlockInReplicationPipeline != null) {
			try {
				nextBlockInReplicationPipeline.close();
				break;
			} catch (IOException e) {
				nextBlockInReplicationPipeline = getNextBlockInPipeline();
			}
		}
	}
	
	public void forceClose() {
		open = false;
		LOGGER.debug(toString()+" closed.");
	}
	
	
	
	
	
	private RemoteBlock getNextBlockInPipeline() {
		while (myIndex + 2 < replicationPipeline.size()) {
			try {
				return replicationPipeline.get(myIndex + 1).openOrCreateBlock(
						blockID);
			} catch (RemoteException | RemoteBlockAlreadyOpenException e) {
				replicationPipeline.remove(myIndex + 1);
			}
		}
		return null;
	}

	@Override
	public long getID() throws RemoteException {
		return blockID;
	}

	@Override
	public int getPreferredBlockSize() throws RemoteException {
		// TODO Auto-generated method stub
		return 65536;
	}

	@Override
	public int getRemainingSize() throws RemoteException {
		return 100;
	}

	@Override
	public void replicateTo(List<? extends DataNodeDataNodeProtocol> dataNodes,
			int myIndex) throws RemoteException {
		this.replicationPipeline = dataNodes;
		this.myIndex = myIndex;
		nextBlockInReplicationPipeline = getNextBlockInPipeline();
		// TODO fix replication
	}

	@Override
	public void delete() throws RemoteException {
		manager.remove(this);
	}

	public RemoteBlock getStub() throws RemoteException {
		return (RemoteBlock) RMIHelper.getStub(this);
	}

	@Override
	public void replicateTo(List<DataNodeDataNodeProtocol> dataNodes)
			throws RemoteException {
		// TODO Auto-generated method stub

	}
	
	@Override
	public String toString() {
		return "[Block "+blockID+"]";
	}
}
