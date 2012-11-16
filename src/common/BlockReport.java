package common;

import java.io.Serializable;
import java.util.Collection;

import datanode.Block;

public class BlockReport implements Serializable {
	private static final long serialVersionUID = 4940592183358404760L;
	public 	BlockInfo[] blocks;
	
	public BlockReport(Collection<Block> blocks) {
		this.blocks = new BlockInfo[blocks.size()];
		int i=0;
		for (Block block : blocks)
			this.blocks[i++] = block.getInfo();
	}
}
