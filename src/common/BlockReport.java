package common;

import java.util.ArrayList;

import datanode.Block;

public class BlockReport {
	
	ArrayList<Long> blocksIds = new ArrayList<Long>();
	
	
	public void addBlockReport(long blockId){
		blocksIds.add(blockId);
		
	}
	
	public ArrayList<Long> getBlockIds(){
		return blocksIds;
	}
	
	
}
