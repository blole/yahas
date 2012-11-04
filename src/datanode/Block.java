package datanode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import common.RMIHelper;
import common.ReplicationHelper;
import common.protocols.DataNodeDataNodeProtocol;
import common.protocols.RemoteBlock;

public class Block implements RemoteBlock {
	
	private static final Logger LOGGER = Logger.getLogger(
			Block.class.getCanonicalName());
	
	
	
	public final long blockID;
	private File file;
	private File hashFile;
	private boolean open;

	private BlockManager manager;



	private RemoteBlock stub;
	
	
	
	
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
		
		if (file.delete()) //the file already existed
			LOGGER.debug(String.format("Deleting block %d to make way for " +
					"a new block with the same ID.", blockID));
		
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
		tryToWriteToFile(data);
	}
	
	@Override
	public void writePipeline(String data,
			List<DataNodeDataNodeProtocol> dataNodes) throws RemoteException {
		tryToWriteToFile(data);
		ReplicationHelper.write(data, blockID, dataNodes);
	}

	private void tryToWriteToFile(String data) throws RemoteException {
		try {
			FileWriter writer = new FileWriter(file);
			writer.append(data);
			writer.close();

			String hashVal = DigestUtils.md5Hex(data);
			FileWriter hashWriter = new FileWriter(hashFile);
			hashWriter.write(""+hashVal);
			hashWriter.close();
			LOGGER.debug("Calculated MD5 Hash of " + data + " as " + hashVal);
		} catch (IOException e) {
			String errormessage = "Error while appending to block "+blockID;
			LOGGER.error(errormessage, e);
			throw new RemoteException(errormessage, e);
		}
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
	public void delete() throws RemoteException {
		manager.remove(this);
	}

	public RemoteBlock getStub() throws RemoteException {
		if (stub == null)
			stub = (RemoteBlock) RMIHelper.getStub(this);
		return stub;
	}

	@Override
	public String toString() {
		return "[Block "+blockID+"]";
	}
}
