package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import common.RMIHelper;
import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.RemoteDirNotFoundException;
import common.protocols.ClientNameNodeProtocol;


public class Client {
	public Client(ClientNameNodeProtocol nameNode) {
		try {
			GreatFile file = nameNode.createFile("/lol", (byte) 3);
			file.write("data here");
			file.close();
			file.delete();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AllDataNodesAreDeadException e) {
			System.err.println("all DataNodes in replication pipeline died");
		} catch (RemoteDirNotFoundException e) {
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
		String host = "localhost";
		String nameNodeAddress = "//"+host+"/NameNode";
		
		try {
			nameNode = (ClientNameNodeProtocol) Naming.lookup(nameNodeAddress);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (RemoteException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		} catch (NotBoundException e) {
			System.err.printf("rmiregistry is likely running on the NameNode '%s', " +
					"but '%s' is not bound.\n", host, e.getMessage());
			System.exit(1);
		}
		
		new Client(nameNode);
	}
}
