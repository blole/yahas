package namenode;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import common.BlockReport;

public class BlockReportReceiver implements Runnable {

	private NameNode reportBackToNameNode;
	private DataNodeImage dataNode;
	long timerInterval;
	public BlockReportReceiver(NameNode reportBackToNameNode,
			DataNodeImage dataNode,long timerInterval) {
		super();
		this.reportBackToNameNode = reportBackToNameNode;
		this.dataNode = dataNode;
		this.timerInterval = timerInterval;
	}
	
	
	
	private static final Logger LOGGER = Logger.getLogger(BlockReportReceiver.class.getCanonicalName());
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		while(true){
			try{
//				LOGGER.debug("Asking for Block Report");
				BlockReport blkRpt =dataNode.stub.getBlockReport();
				ArrayList<Long> blkIds = blkRpt.getBlockIds();
				LOGGER.debug("Size of Blk Ids" + blkIds.size());
				for(long i: blkRpt.getBlockIds()){
					LOGGER.debug( "\t" + dataNode.id + " having block "+ i);
				}
				Thread.sleep(timerInterval);
				
			}
			catch (RemoteException e){
				
			}
			catch(InterruptedException e){
				
			}
			
	
		}
			}

}
