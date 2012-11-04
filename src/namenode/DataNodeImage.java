package namenode;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import common.protocols.RemoteDataNode;

public class DataNodeImage {
	public final InetSocketAddress receivedHearbeatSocketAddress;
	public final int id;
	public int availableSpace;
	public final RemoteDataNode stub;

	public DataNodeImage(int dataNodeID, InetSocketAddress socketAddress) throws MalformedURLException, RemoteException, NotBoundException {
		this.receivedHearbeatSocketAddress = socketAddress;
		this.id = dataNodeID;
		this.stub = (RemoteDataNode) Naming.lookup(getRMIAddress());
	}
	
	public String getRMIAddress() {
		return 	"//"+receivedHearbeatSocketAddress.getHostString()+"/DataNode"+id;
	}
	
	
	
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		
		// object must be DataNodeImage at this point
		DataNodeImage otherDataNodeImage = (DataNodeImage)obj;
		return id == otherDataNodeImage.id;
	}
	
	@Override
	public int hashCode() {
		return id;
		//return 997 * id ^ 991 * receivedHearbeatSocketAddress.hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("[DataNode "+id+"]");
	}
}
