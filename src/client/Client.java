package client;

import java.nio.file.NotDirectoryException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import common.RMIHelper;
import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.BadFileName;
import common.exceptions.FileAlreadyOpenException;
import common.exceptions.FileOrDirectoryAlreadyExistsException;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.exceptions.NotFileException;
import common.protocols.ClientNameNodeProtocol;
import common.protocols.RemoteDir;

public class Client {
	private static final Logger LOGGER = Logger.getLogger(Client.class
			.getCanonicalName());
	
	ClientNameNodeProtocol nameNode = null;
	
	public Client(ClientNameNodeProtocol nameNode) {
		this.nameNode = nameNode;
	}
	
	
	
	
	
	public void createFile(String fileName, int repFactor, byte[] contents) {
		try (ClientFile file = nameNode.createFile(fileName, (byte) repFactor, 65536)) {
			file.open();
			file.write(contents);
			LOGGER.debug("File " + fileName + " created Successfully");
			debugPrintNamespace("", nameNode.getDir("/"));
			
			System.out.printf("\nread: '%s'\n", new String(file.read()));
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FileOrDirectoryAlreadyExistsException e) {
			System.err.println("File already exists: "+e.getMessage());
		} catch (AllDataNodesAreDeadException e) {
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
		}
	}
	
	public String readFile (String fileName) {
		try (ClientFile file = nameNode.getFile(fileName)) {
			file.open();
			return new String(file.read());
		} catch (RemoteException | FileAlreadyOpenException |
				NotDirectoryException | NoSuchFileOrDirectoryException |
				NotFileException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	public void createDir(String pathName){
		try {
			nameNode.createDir(pathName, true);
		} catch (RemoteException | NotDirectoryException |
				FileOrDirectoryAlreadyExistsException | NoSuchFileOrDirectoryException e) {
			e.printStackTrace();
		}
	}
	
	public void printDirContent(String dirName){
		try {
			RemoteDir dir = nameNode.getDir(dirName);
			for (RemoteDir subDir : dir.getSubDirs()){
				LOGGER.debug("*" + subDir.getPath());
			}
			for(ClientFile file : dir.getFiles()){
				LOGGER.debug("-" + file.getName());
			}
		} catch (RemoteException | NotDirectoryException | NoSuchFileOrDirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	

	public void debugPrintNamespace(String prefix, RemoteDir dir)
			throws RemoteException {
		System.out.println(prefix + dir.getPath());
		for (RemoteDir subDir : dir.getSubDirs())
			debugPrintNamespace(prefix + "  ", subDir);
		for (ClientFile file : dir.getFiles())
			System.out.println(prefix + "-" + file.getName());
	}

	public static void main(String args[]) {
		String host = "localhost";
		String nameNodeAddress = "//" + host + "/NameNode";
		RMIHelper.maybeStartSecurityManager();
		Remote nameNode = RMIHelper.lookup(nameNodeAddress);
		Client client = new Client((ClientNameNodeProtocol) nameNode);
		client.createFile("/world", 2, "file data".getBytes());
		RemoteDir rootDir;
		try {
			rootDir = client.nameNode.getDir("");
			
			client.createDir("/dir1/");
			client.createDir("/dir2/");
			client.createDir("/dir3/");
//			client.printDirContent("dir1");
			client.debugPrintNamespace("", rootDir);
		} catch (RemoteException | NotDirectoryException | NoSuchFileOrDirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}