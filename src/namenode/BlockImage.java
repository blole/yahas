package namenode;

public class BlockImage {
	public final long id;
	public final int replicationFactor;
	
	public BlockImage(long id, int replicationFactor) {
		this.id = id;
		this.replicationFactor = replicationFactor;
	}
}
