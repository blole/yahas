package datanode;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
	public byte[] read() throws RemoteException {
		byte[] byteArray = new byte[(int)file.length()];
		
		try {
			InputStream input = null;
			try {
				int totalBytesRead = 0;
				input = new BufferedInputStream(new FileInputStream(file));
				while (totalBytesRead < byteArray.length) {
					int bytesRemaining = byteArray.length - totalBytesRead;
					//input.read() returns -1, 0, or more :
					int bytesRead;
						bytesRead = input.read(byteArray, totalBytesRead, bytesRemaining);
					if (bytesRead > 0)
						totalBytesRead = totalBytesRead + bytesRead;
				}
			} finally {
				if (input != null)
					input.close();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			LOGGER.error("Error reading file "+file.getAbsolutePath(), e);
		}
		LOGGER.debug(toString()+" read");
		return byteArray;
		//TODO: maybe calculate hash while reading
	}
	
	@Override
	public void write(byte[] data) throws RemoteException {
		tryToWriteToFile(data);
	}
	
	@Override
	public void writePipeline(byte[] data,
			List<DataNodeDataNodeProtocol> dataNodes) throws RemoteException {
		tryToWriteToFile(data);
		LOGGER.debug(String.format("%s replicating to %d DataNodes", toString(), dataNodes.size()));
		ReplicationHelper.write(data, blockID, dataNodes);
	}

	private void tryToWriteToFile(byte[] data) throws RemoteException {
		try {
			OutputStream writer = new FileOutputStream(file);
			writer.write(data);
			writer.close();
			LOGGER.debug(toString()+" appended");

			String hashVal = DigestUtils.md5Hex(data);
			FileWriter hashWriter = new FileWriter(hashFile);
			hashWriter.write(""+hashVal);
			hashWriter.close();
			LOGGER.debug(toString()+" new MD5 hash: "+ hashVal);
		} catch (IOException e) {
			String errormessage = toString()+" error while appending";
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
