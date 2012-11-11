package client;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.apache.log4j.Logger;

import common.RMIHelper;
import common.exceptions.AllDataNodesAreDeadException;
import common.exceptions.BadFileName;
import common.exceptions.FileAlreadyOpenException;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.exceptions.NotFileException;
import common.protocols.ClientNameNodeProtocol;
import common.protocols.RemoteDir;

public class Client {

	private static final Logger LOGGER = Logger.getLogger(Client.class
			.getCanonicalName());
	
	ClientNameNodeProtocol nameNode = null;
		

	public void createFile(String fileName, int repFactor, String contents) {

		YAHASFile file = null;

		try {
			file = nameNode.createFile(fileName, (byte) repFactor);
			file.open();
			file.write(contents);
			LOGGER.debug("File " + fileName + " created Successfully");
			debugPrintNamespace("", nameNode.getDir("/"));
			
			System.out.printf("\nread: '%s'\n", new String(file.read()));
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (AllDataNodesAreDeadException e) {
			e.printStackTrace();
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
				file.forceClose();
		}

	}
	
	public String readFile (String fileName) {
		String contents=null ;	
		try {
				YAHASFile file = nameNode.getFile(fileName);
				contents= new String(file.read());

			} catch (RemoteException | FileAlreadyOpenException |
					NotDirectoryException | NoSuchFileOrDirectoryException |
					NotFileException e) {
				e.printStackTrace();
			}
			return contents;
	}
	
	
	public void createDir(String pathName){
		
		try {
			nameNode.createDir(pathName, true);
		} catch (RemoteException | NotDirectoryException |
				FileAlreadyExistsException | NoSuchFileOrDirectoryException e) {
			e.printStackTrace();
		}
	}
	
	public void printDirContent(String dirName){
		try {
			RemoteDir dir = nameNode.getDir(dirName);
			for (RemoteDir subDir : dir.getSubDirs()){
				LOGGER.debug("*" + subDir.getPath());
			}
			for(YAHASFile file : dir.getFiles()){
				LOGGER.debug("-" + file.getName());
			}
		} catch (RemoteException | NotDirectoryException | NoSuchFileOrDirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	

	public Client(ClientNameNodeProtocol nameNode) {

		this.nameNode = nameNode;
		

		// try {
		// file = nameNode.createFile("world", (byte)2);
		// debugPrintDir("", nameNode.getDir(""));
		// file.open();
		// file.write("file data");
		// System.out.printf("read: '%s'\n", new String(file.read()));
		// } catch (RemoteException e) {
		// e.printStackTrace();
		// } catch (RemoteDirNotFoundException e) {
		// e.printStackTrace();
		// } catch (RemoteFileAlreadyOpenException e) {
		// System.err.println("File already open.");
		// } catch (AllDataNodesAreDeadException e) {
		// System.err.println("All DataNodes died.");
		// } finally {
		// if (file != null)
		// file.tryToClose();
		// }
	}

	public void debugPrintNamespace(String prefix, RemoteDir dir)
			throws RemoteException {
		System.out.println(prefix + dir.getPath());
		for (RemoteDir subDir : dir.getSubDirs())
			debugPrintNamespace(prefix + "  ", subDir);
		for (YAHASFile file : dir.getFiles())
			System.out.println(prefix + "-" + file.getName());
	}

	public static void main(String args[]) {
		String host = "localhost";
		String nameNodeAddress = "//" + host + "/NameNode";
		RMIHelper.maybeStartSecurityManager();
		Remote rNameNode = RMIHelper.lookup(nameNodeAddress);
		Client client =new Client((ClientNameNodeProtocol) rNameNode);
		client.createFile("world", 2, "file data");
		RemoteDir rootDir;
		try {
			rootDir = client.nameNode.getDir("");
			
			client.createDir("/dir1/");
			System.err.println("asd");
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