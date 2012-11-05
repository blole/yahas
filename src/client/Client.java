package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.RMIHelper;
import common.exceptions.RemoteDirNotFoundException;
import common.protocols.ClientNameNodeProtocol;


public class Client {
	public Client(ClientNameNodeProtocol nameNode) {
//		YAHASFile file = null;
//		try {
//			file = nameNode.createFile("/lol2", (byte) 3);
//			file.write("data here Goivnd 123");
//			//file.delete();
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		} catch (AllDataNodesAreDeadException e) {
//			System.err.println("All DataNodes in replication pipeline died");
//		} catch (RemoteDirNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//			if (file != null)
//				file.tryToClose();
//		}
		
		try {
			nameNode.createFile("hello", (byte)2);
			nameNode.createFile("world", (byte)2);
			for (YAHASFile f : nameNode.getDir("/").getFiles())
				System.out.println(f.getName());
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (RemoteDirNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	public static void main(String args[]) {
		String host = "localhost";
		String nameNodeAddress = "//"+host+"/NameNode";
		
		RMIHelper.maybeStartSecurityManager();
		Remote nameNode = RMIHelper.lookup(nameNodeAddress);
		if (nameNode != null)
			new Client((ClientNameNodeProtocol) nameNode);
	}
}
