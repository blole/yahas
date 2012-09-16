package namenode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Set;

import common.Constants;
import common.YSocket;

public class DataNodeImage {
	private YSocket backSocket = null;
	public final InetSocketAddress receivedHearbeatSocketAddress;
	public final int id;

	public DataNodeImage(int dataNodeID, InetSocketAddress socketAddress) {
		this.receivedHearbeatSocketAddress = socketAddress;
		this.id = dataNodeID;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		
		// object must be DataNodeImage at this point
		DataNodeImage otherDataNodeImage = (DataNodeImage)obj;
		return id == otherDataNodeImage.id &&
			(
				receivedHearbeatSocketAddress == otherDataNodeImage.receivedHearbeatSocketAddress ||
				(receivedHearbeatSocketAddress != null && receivedHearbeatSocketAddress.equals(otherDataNodeImage.receivedHearbeatSocketAddress))
			);
	}
	
	/**
	 * If called once more after failing to establish a connection, it tries again.
	 * 
	 * @return A socket connected to the DataNode, or null if an connection couldn't be established.
	 */
	public YSocket createOrGetBackSocket() {
		if (backSocket == null) {
			try {
				backSocket = new YSocket(receivedHearbeatSocketAddress.getAddress(), Constants.DEFAULT_DATA_NODE_PORT);
			} catch (IOException e) {
				System.err.printf("%s Unable to create back socket: %s\n", this, e.getLocalizedMessage());
			}
		}
		return backSocket;
	}
	
	@Override
	public int hashCode() {
		return 997 * id ^ 991 * receivedHearbeatSocketAddress.hashCode();
	}
	
	public String toString() {
		return String.format("[DataNodeImage id=%d addr=%s]", id, receivedHearbeatSocketAddress);
	}

	public Set<BlockImage> getBlocks() throws IOException {
		YSocket backSocket = createOrGetBackSocket();
		backSocket.send("hello, I'm the NameNode");
		return null;
	}
}
