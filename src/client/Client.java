package client;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import com.google.common.base.Charsets;
import java.nio.file.NotDirectoryException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.InputMismatchException;
import java.util.Scanner;

import org.apache.log4j.Logger;


import com.google.common.io.Files;
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
	
	static RemoteDir pwd=null;
	static RemoteDir rootDir=null;
	
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

	
	public static void displayBanner(){
		try {
			String content = Files.toString(new File("/home/govind/PhD/Lecture/SWEng/Project/Code/YAHAS3/src/Banner"), Charsets.UTF_8);
			System.out.println(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void operations(Client client){
		
		
		try {
			rootDir = client.nameNode.getDir("");
			client.createFile("/world", 2, "file data at ROOT".getBytes());
			client.createDir("/dir1/");
			client.createFile("/dir1/world", 2, "file data inside dir1".getBytes());
			client.createDir("/dir2/");
			client.createDir("/dir3/");
//			client.printDirContent("dir1");
			client.debugPrintNamespace("", rootDir);
		} catch (RemoteException | NotDirectoryException | NoSuchFileOrDirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	
	public static String readLineFromConsole() throws InputMismatchException
	{
		Scanner in = new Scanner(System.in);  
		String str;
		System.out.print(">");
		str = in.nextLine();
		return str;
	}
	public static void main(String args[]) {
		
		displayBanner();
		Console console = System.console();
		
		String host = "localhost";
		String nameNodeAddress = "//" + host + "/NameNode";
		RMIHelper.maybeStartSecurityManager();
		Remote nameNode = RMIHelper.lookup(nameNodeAddress);
		Client client = new Client((ClientNameNodeProtocol) nameNode);

		while(true){
//			System.out.println(">");
			String command = readLineFromConsole();
			
			if(command.equals("ls")){
				System.out.println("LS");
			}
			else if (command.equals("pwd")){
				System.out.println("Printing PWD");
			}
			else if (command.split("-")[0].equals(("mkdir"))){
				System.out.println("Making directory !!");
			}
			else if (command.split("-")[0].equals(("mkfile"))){
				System.out.println("make File!!");
			}
			else if (command.split("-")[0].equals(("cd"))){
				System.out.println("Changing Directory!!");
			}
			else if (command.split("-")[0].equals(("mkfile"))){
				System.out.println("make File!!");
			}
			else if(command.equals("quit")){
				System.out.println("Bye !!");
				return;
			}
			else {
				System.out.println("Command Not Found");
			}
			System.out.println("> " + command );
			
			
		}
		
		
				
		
		
	}
}