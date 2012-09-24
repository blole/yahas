package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import rmi.interfaces.NameNodeInterface;

import common.YSocket;

public class Client {
	private static final int DEFAULT_PORT = 1618;
	//private static final DataNodeList
	private NameNodeInterface nameNode;
	
	public Client(NameNodeInterface nameNode) {
		this.nameNode = nameNode;
		
		try {
			nameNode.receiveMessage("wooooo");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		int serverPort = DEFAULT_PORT;
		
		if (args.length < 2)
			System.out.println("no port specified, assuming default port: "+DEFAULT_PORT);
		else {
			try {
				serverPort = Integer.valueOf(args[1]);
			}
			catch (NumberFormatException e) {
				System.out.println("malformated port specification, exiting");
				System.exit(1);
			}
		}
		
		NameNodeInterface nameNode = null;
		try {
			nameNode = (NameNodeInterface) Naming.lookup("//localhost/NameNode");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			System.exit(1);
		} catch (RemoteException e1) {
			e1.printStackTrace();
			System.exit(1);
		} catch (NotBoundException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		new Client(nameNode);
	}
}
