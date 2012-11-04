package namenode;

import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import common.BlockReport;

public class BlockReportReceiver implements Runnable {
	private static final Logger LOGGER = Logger.getLogger(BlockReportReceiver.class.getCanonicalName());

	private NameNode nameNode;
	long timerInterval;
	
	public BlockReportReceiver(	NameNode nameNode, long timerInterval) {
		this.nameNode = nameNode;
		this.timerInterval = timerInterval;
	}
	
	@Override
	public void run() {
		while(true){
			for (DataNodeImage dataNode : nameNode.getConnectedDataNodes())
				getBlockReportFrom(dataNode);
			
			try {
				Thread.sleep(timerInterval);
			} catch (InterruptedException e) {}
		}
	}
	
	public void getBlockReportFrom(DataNodeImage dataNode) {
		try {
			BlockReport blockReport = dataNode.stub.getBlockReport();
			nameNode.receiveBlockReport(dataNode, blockReport);
			LOGGER.debug(String.format("Got BlockReport from %s with %d blocks",
					dataNode, blockReport.blockIDs.size()));
		}
		catch (RemoteException e) {
			LOGGER.error("Error while getting BlockReport from "+dataNode, e);
		}
	}
}
