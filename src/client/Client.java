package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import namenode.NameNodeFile;

import common.RMIHelper;
import common.protocols.ClientDataNodeProtocol;
import common.protocols.ClientNameNodeProtocol;
import common.protocols.RemoteFile;


public class Client {
	private ClientNameNodeProtocol nameNode;
	
	public Client(ClientNameNodeProtocol nameNode) {
		this.nameNode = nameNode;
		
		try {
			File file = new File(nameNode.createFile("lol", (byte) 3));
			file.delete();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
//		try {
//			for (DataNodeInterface dataNode : nameNode.getDataNodes()) {
//				try {
//					dataNode.receiveMessage("lulz");
//				} catch (RemoteException e) {}
//			}
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}
	}
	
	
	
	
	
	public static void main(String args[]) {
		RMIHelper.maybeStartSecurityManager();
		
		ClientNameNodeProtocol nameNode = null;
		
		try {
			nameNode = (ClientNameNodeProtocol) Naming.lookup("//localhost/NameNode");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		new Client(nameNode);
	}
}
