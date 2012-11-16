package datanode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import common.BlockInfo;
import common.RMIHelper;
import common.ReplicationHelper;
import common.protocols.DataNodeDataNodeProtocol;
import common.protocols.RemoteBlock;

public class Block implements RemoteBlock {
	private static final Logger LOGGER = Logger.getLogger(
			Block.class.getCanonicalName());
	
	private final long blockID;
	private File file;
	private File hashFile;
	
	private DataNode dataNode;
	private RemoteBlock stub;

	
	
	
	
	
	/**
	 * Creates a new block and creates the files for it too.
	 * If a block with that ID already existed in the directory, that one is deleted!
	 * 
	 * @param blockID
	 * @param nameNode 
	 * @throws IOException 
	 */
	public Block(long blockID, DataNode dataNode) throws RemoteException {
		this(blockID,
				new File(dataNode.blocks.blockDir, ""+blockID),
				new File(dataNode.blocks.blockDir, blockID+".hash"),
				dataNode);
		
		if (file.delete()) //the file already existed
			LOGGER.debug(String.format("Deleting block %d to make way for " +
					"a new block with the same ID.", blockID));
		
		try {
			file.createNewFile();
			hashFile.createNewFile();
		} catch (IOException e) {
			String errormessage = "Error creating files for new block "+blockID;
			LOGGER.error(errormessage, e);
			throw new RemoteException(errormessage, e);
		}
	}
	
	/**
	 * Only use if you already have checked that both file and hashFile already
	 * exist.
	 * 
	 * @param blockID
	 * @param file
	 * @param hashFile
	 * @param manager
	 * @param nameNode 
	 */
	public Block(long blockID, File file, File hashFile, DataNode dataNode) {
		this.blockID = blockID;
		this.file = file;
		this.hashFile = hashFile;
		this.dataNode = dataNode;
	}
	
	
	
	

	@Override
	public byte[] read() throws RemoteException {
		byte[] byteArray = new byte[(int)file.length()];
		
		try (InputStream input = new BufferedInputStream(new FileInputStream(file))) {
			input.read(byteArray);
			//TODO: maybe calculate hash while reading
			LOGGER.debug(this+" read");
			return byteArray;
		} catch (IOException e) {
			String errorMessage = this+" error while reading '"+file.getAbsolutePath()+"'";
			LOGGER.error(errorMessage, e);
			throw new RemoteException(errorMessage, e);
		}
	}
	
	@Override
	public void write(byte[] data) throws RemoteException {
		writeToFile(data);
	}
	
	@Override
	public void writePipeline(final byte[] data,
			final List<DataNodeDataNodeProtocol> dataNodes) throws RemoteException {
		writeToFile(data);
		
		new Thread() {
			@Override
			public void run() {
				LOGGER.debug(this+" replicating to "+dataNodes.size()+" DataNodes");
				ReplicationHelper.write(data, blockID, dataNodes);
			};
		}.start();
	}

	private void writeToFile(byte[] data) throws RemoteException {
		try (OutputStream writer = new FileOutputStream(file, true)) {
			writer.write(data);
			LOGGER.debug(toString()+" appended");
			writeHash(DigestUtils.md5Hex(data).getBytes());
		} catch (IOException e) {
			String errormessage = this+" error while appending";
			LOGGER.error(errormessage, e);
			throw new RemoteException(errormessage, e);
		}
		dataNode.nameNode.blockReceived(dataNode.id, getInfo());
	}
	
	
	
	
	
	private void writeHash(byte[] hash) {
		try (OutputStream writer = new FileOutputStream(hashFile)){
			writer.write(hash);
			LOGGER.debug(this+" saved new hash");
		} catch (IOException e) {
			LOGGER.error(this+" error writing new hash", e);
		}
	}
	
	private byte[] readHash() {
		byte[] byteArray = new byte[(int) hashFile.length()];
		try (InputStream reader = new BufferedInputStream(new FileInputStream(hashFile))) {
			reader.read(byteArray);
			LOGGER.debug(this+" read hash");
		} catch (IOException e) {
			LOGGER.error(this+" error reading hash", e);
		}
		return byteArray;
	}
	
	
	
	
	
	public long getID() {
		return blockID;
	}

	@Override
	public void delete() throws RemoteException {
		dataNode.blocks.remove(blockID);
		file.delete();
		hashFile.delete();
		LOGGER.debug(this+" removed");
	}

	public RemoteBlock getStub() throws RemoteException {
		if (stub == null)
			stub = (RemoteBlock) RMIHelper.getStub(this);
		return stub;
	}

	@Override
	public String toString() {
		return "[Block "+getID()+"]";
	}

	public BlockInfo getInfo() {
		return new BlockInfo(blockID, (int)file.length());
	}
}
