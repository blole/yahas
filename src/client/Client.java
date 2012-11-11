package client;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import common.RMIHelper;
import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.BadFileName;
import common.exceptions.FileAlreadyOpenException;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.protocols.ClientNameNodeProtocol;
import common.protocols.RemoteDir;


public class Client {
	public Client(ClientNameNodeProtocol nameNode) {
		YAHASFile file = null;
		try {
			nameNode.createDir("a", false);
			file = nameNode.createFile("c", (byte)2);
			file.open();
			file.write("file data");
			file.move("h");
			
			debugPrintDir("", nameNode.getDir("/"));
			
			System.out.printf("\nread: '%s'\n", new String(file.read()));
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AllDataNodesAreDeadException e) {
			System.err.println("All DataNodes died.");
		} catch (FileAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotDirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFileOrDirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadFileName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileAlreadyOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (file != null)
				file.tryToClose();
		}
	}
	
	public void debugPrintDir(String prefix, RemoteDir dir) throws RemoteException {
		System.out.println(prefix + dir.getPath());
		for (YAHASFile file : dir.getFiles())
			System.out.println(prefix+" -"+file.getName());
		for (RemoteDir subDir : dir.getSubDirs())
			debugPrintDir(prefix+"    ", subDir);
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
