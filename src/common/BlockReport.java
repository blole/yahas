package common;

import java.util.ArrayList;

public class BlockReport {
	
	ArrayList<Long> blocksIds = new ArrayList<Long>();
	
	
	public void addBlockIds(long blockId){
		blocksIds.add(blockId);
		
	}
	
	public ArrayList<Long> getBlockIds(){
		return blocksIds;
	}
	
	
}
