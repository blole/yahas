package datanode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import common.RMIHelper;
import common.exceptions.RemoteBlockAlreadyExistsException;
import common.exceptions.RemoteBlockAlreadyOpenException;
import common.exceptions.RemoteBlockNotFoundException;
import common.protocols.DataNodeDataNodeProtocol;
import common.protocols.RemoteBlock;

public class Block implements RemoteBlock {

	private long blockID;
	private File file;
	private File hashFile;

	private List<? extends DataNodeDataNodeProtocol> replicationPipeline;
	private int myIndex;
	private RemoteBlock nextBlockInReplicationPipeline;
	private DataNode dataNode;
	
	

	private static final Logger LOGGER = Logger.getLogger(Block.class
			.getCanonicalName());

	private Block(long blockID, File file, DataNode dataNode) {
		this.dataNode = dataNode;
		this.blockID = blockID;
		this.file = file;
		file.getParentFile().mkdirs();

		if (!file.exists())
			try {
				file.createNewFile();
			} catch (IOException e) {
			}

		// Create the HashFile
		// hashFile = new File(file.get)

		LOGGER.debug("Hash File " + file.getAbsolutePath() + ".hash");
		hashFile = new File(file.getAbsoluteFile() + ".hash");
		if (hashFile.exists() == false) {
			try {
				hashFile.createNewFile();
			} catch (IOException e) {
				LOGGER.error(e);
			}

		}

		dataNode.openBlocks.put(blockID, this);
	}

	public static Block get(long blockID, DataNode dataNode)
			throws RemoteBlockAlreadyOpenException,
			RemoteBlockNotFoundException {
		if (dataNode.openBlocks.containsKey(blockID))
			throw new RemoteBlockAlreadyOpenException();
		else {
			File file = new File(dataNode.pathBlocks + blockID);
			if (!file.exists())
				throw new RemoteBlockNotFoundException();
			return new Block(blockID, file, dataNode);
		}
	}

	public static Block create(long blockID, DataNode dataNode)
			throws RemoteBlockAlreadyExistsException {
		File file = new File(dataNode.pathBlocks + blockID);

		if (file.exists())
			throw new RemoteBlockAlreadyExistsException();
		else
			return new Block(blockID, file, dataNode);

	}

	public static Block openOrCreate(long blockID, DataNode dataNode)
			throws RemoteBlockAlreadyOpenException {
		if (dataNode.openBlocks.containsKey(blockID))
			throw new RemoteBlockAlreadyOpenException();
		else
			return new Block(blockID, new File(dataNode.pathBlocks + blockID),
					dataNode);
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
			//Add to the DataNodes Block Report
			dataNode.setBlockReport(blockID, hashVal);
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

	@Override
	public void close() throws RemoteException {
		dataNode.openBlocks.remove(blockID);
		while (nextBlockInReplicationPipeline != null) {
			try {
				nextBlockInReplicationPipeline.close();
				break;
			} catch (IOException e) {
				nextBlockInReplicationPipeline = getNextBlockInPipeline();
			}
		}
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

	}

	public RemoteBlock getStub() throws RemoteException {
		return (RemoteBlock) RMIHelper.getStub(this);
	}

	@Override
	public void replicateTo(List<DataNodeDataNodeProtocol> dataNodes)
			throws RemoteException {
		// TODO Auto-generated method stub

	}
}
