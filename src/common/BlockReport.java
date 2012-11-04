package common;

import java.util.ArrayList;

import datanode.Block;

public class BlockReport {
	
	ArrayList<Long> blocksIds = new ArrayList<Long>();
	ArrayList<String> hashValues = new ArrayList<String>();
	
	public void addBlockReport(long blockId,String hashValue){
		blocksIds.add(blockId);
		hashValues.add(hashValue);
	}
	
	
}
