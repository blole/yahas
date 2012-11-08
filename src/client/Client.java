package client;

import java.rmi.Remote;
import java.rmi.RemoteException;

import common.RMIHelper;
import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.RemoteDirNotFoundException;
import common.exceptions.RemoteFileAlreadyOpenException;
import common.protocols.ClientNameNodeProtocol;
import common.protocols.RemoteDir;


public class Client {
	public Client(ClientNameNodeProtocol nameNode) {
		YAHASFile file = null;
		try {
			file = nameNode.createFile("world", (byte)2);
			debugPrintDir("", nameNode.getDir(""));
			
			file.open();
			file.write("file data");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (RemoteDirNotFoundException e) {
			e.printStackTrace();
		} catch (RemoteFileAlreadyOpenException e) {
			System.err.println("File already open.");
		} catch (AllDataNodesAreDeadException e) {
			System.err.println("All DataNodes died.");
		} finally {
			if (file != null)
				file.tryToClose();
		}
	}
	
	public void debugPrintDir(String prefix, RemoteDir dir) throws RemoteException {
		System.out.println(prefix + dir.getPath());
		for (RemoteDir subDir : dir.getSubDirs())
			debugPrintDir(prefix+"  ", subDir);
		for (YAHASFile file : dir.getFiles())
			System.out.println(prefix+"-"+file.getName());
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
