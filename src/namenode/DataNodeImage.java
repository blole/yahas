package namenode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import common.Constants;
import common.Convert;
import common.Packet;
import common.PacketType;
import common.YSocket;
import common.exceptions.InvalidPacketType;
import common.exceptions.UnexpectedPacketType;
import datanode.DataNodeInterface;

public class DataNodeImage {
	private YSocket backSocket = null;
	public final InetSocketAddress receivedHearbeatSocketAddress;
	public final int id;
	public int availableSpace;

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
	 * TODO: Blocks until the previous transaction is done.
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
	
	@Override
	public String toString() {
		return String.format("[DataNodeImage %s]", getRMIAddress());
	}

	public Set<BlockImage> getBlocks() throws IOException, InvalidPacketType, UnexpectedPacketType {
		YSocket backSocket = createOrGetBackSocket();
		backSocket.send(Packet.RequestBlockReport);
		Packet blockReportPacket = backSocket.receivePacket(PacketType.BLOCK_REPORT);
		
		if (blockReportPacket.message.length % 8 != 0)
			throw new IOException("Received BlockReportPacket's length was not a multiple of 8.");
		
		Set<BlockImage> set = new HashSet<>();
		for (int i=0; i<blockReportPacket.message.length; i+=8) {
			set.add(new BlockImage(Convert.byteArrayToLong(blockReportPacket.message, i)));
		}
		return set;
	}
	
	public String getRMIAddress() {
		return 	"//"+receivedHearbeatSocketAddress.getHostString()+"/DataNode";//+id;
	}

	public DataNodeInterface getStub() throws MalformedURLException, RemoteException, NotBoundException {
		return (DataNodeInterface) Naming.lookup(getRMIAddress());
	}
}
