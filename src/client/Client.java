package client;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import common.RMIHelper;

import rmi.interfaces.NameNodeInterface;

public class Client {
	private NameNodeInterface nameNode;
	
	public Client(NameNodeInterface nameNode) {
		this.nameNode = nameNode;
		
		try {
			nameNode.receiveMessage("wooooo");
			System.out.println(nameNode.getDataNodes());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	public static void main(String args[]) {
		RMIHelper.maybeStartSecurityManager();
		
		NameNodeInterface nameNode = null;
		
		try {
			nameNode = (NameNodeInterface) Naming.lookup("//localhost/NameNode");
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
		
		new Client(nameNode);
	}
}
