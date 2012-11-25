package namenode;


public class BlockReportRetriever implements Runnable {
//	private static final Logger LOGGER = Logger.getLogger(
//			BlockReportRetriever.class.getCanonicalName());

	private NameNode nameNode;
	long timerInterval;
	
	public BlockReportRetriever(NameNode nameNode, long timerInterval) {
		this.nameNode = nameNode;
		this.timerInterval = timerInterval;
	}
	
	@Override
	public void run() {
		while(true){
			for (DataNodeImage dataNode : nameNode.getConnectedDataNodes().values())
				dataNode.retrieveBlockReport();
			
			try {
				Thread.sleep(timerInterval);
			} catch (InterruptedException e) {}
		}
	}
}
