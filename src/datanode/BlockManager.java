package datanode;

import java.io.File;
import java.rmi.RemoteException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import common.BlockReport;
import common.Constants;

public class BlockManager extends HashMap<Long, Block> {
	private static final long serialVersionUID = -215302376131662533L;
	private static final Logger LOGGER = Logger.getLogger(
			BlockManager.class.getCanonicalName());
	
	public final File blockDir;
	
	public BlockManager(String blockDirPath) {
		this.blockDir = new File(blockDirPath);
		if (!blockDir.exists())
			blockDir.mkdirs();
		else if (!blockDir.isDirectory()) {
			LOGGER.error(String.format("'%s' is not a directory.", blockDir.getAbsolutePath()));
			System.exit(1);
		}
	}
	
	public Block newBlock(long blockID) throws RemoteException {
		Block block = new Block(blockID, this);
		put(block.blockID, block);
		return block;
	}
	
	public BlockReport getBlockReport() {
		//TODO in the future, continuously scan through all the blocks,
		//calculating checksums and removing the bad ones.
		return new BlockReport(this.keySet());
	}
	
	public void readFromDisk() {
		if (size() != 0)
			throw new RuntimeException("TODO: we cannot read blocks from disk twice yet.."); //TODO
		
		File files[] = blockDir.listFiles();
		for (File file : files) {
			if (file.isFile() && !file.getName().endsWith(Constants.HASH_FILE_ENDING)) {
				long id;
				try {
					id = Integer.parseInt(file.getName());
				} catch (NumberFormatException e) {
					LOGGER.error(String.format("File '%s' does not belong " +
							"in the block directory.", file.getName()));
					continue;
				}
				File hashFile = new File(file.getAbsolutePath()+Constants.HASH_FILE_ENDING);
				if (!hashFile.exists()) {
					LOGGER.error(String.format("Missing hashfile '%s' for block.",
							hashFile.getName()));
					continue;
				}
				
				put(id, new Block(id, file, hashFile, this));
			}
		}
	}
}
