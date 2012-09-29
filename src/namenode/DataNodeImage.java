package namenode;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import common.protocols.ClientDataNodeProtocol;

public class DataNodeImage {
	public final InetSocketAddress receivedHearbeatSocketAddress;
	public final int id;
	public int availableSpace;
	private ClientDataNodeProtocol stub;

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
	
	@Override
	public int hashCode() {
		return 997 * id ^ 991 * receivedHearbeatSocketAddress.hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("[DataNodeImage %s]", getRMIAddress());
	}
	
	public String getRMIAddress() {
		return 	"//"+receivedHearbeatSocketAddress.getHostString()+"/DataNode"+id;
	}

	public ClientDataNodeProtocol getStub() throws MalformedURLException, RemoteException, NotBoundException {
		if (stub == null)
			stub = (ClientDataNodeProtocol) Naming.lookup(getRMIAddress()); 
		return stub;
	}

	public ClientDataNodeProtocol getStubOrNull() {
		try {
			return getStub();
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			return null;
		}
	}
}
