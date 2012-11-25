package client;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NotDirectoryException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.google.common.base.Charsets;
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
import common.protocols.RemoteFileOrDir;
import common.protocols.RemoteFileOrDir.Type;

public class Client {
	private static final Logger LOGGER = Logger.getLogger(Client.class
			.getCanonicalName());

	ClientNameNodeProtocol nameNode = null;

	static RemoteDir pwd = null;
	static RemoteDir rootDir = null;

	public Client(ClientNameNodeProtocol nameNode) {
		this.nameNode = nameNode;
	}

	public void createFile(String fileName, int repFactor, byte[] contents) {
		try (ClientFile file = nameNode.createFile(fileName, (byte) repFactor,
				65536)) {
			file.open();
			file.write(contents);
			LOGGER.debug("File " + fileName + " created Successfully");
			debugPrintNamespace("", nameNode.getDir("/"));

			System.out.printf("\nread: '%s'\n", new String(file.read()));
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (FileOrDirectoryAlreadyExistsException e) {
			System.err.println("File already exists: " + e.getMessage());
		} catch (AllDataNodesAreDeadException e) {
			e.printStackTrace();
		} catch (NotDirectoryException e) {

			e.printStackTrace();
		} catch (NoSuchFileOrDirectoryException e) {

			e.printStackTrace();
		} catch (BadFileName e) {

			e.printStackTrace();
		} catch (FileAlreadyOpenException e) {

			e.printStackTrace();
		}
	}

	public String readFile(String fileName) {
		try (ClientFile file = nameNode.getFile(fileName)) {
			file.open();
			return new String(file.read());
		} catch (RemoteException | FileAlreadyOpenException
				| NotDirectoryException | NoSuchFileOrDirectoryException
				| NotFileException e) {
			throw new RuntimeException(e);
		}
	}

	public void createDir(String pathName) {
		try {
			nameNode.createDir(pathName, true);
		} catch (RemoteException | NotDirectoryException
				| FileOrDirectoryAlreadyExistsException
				| NoSuchFileOrDirectoryException e) {
			e.printStackTrace();
		}
	}

	public void move(String srcPath, String pathName) {

		try {
			RemoteFileOrDir fileOrDir = pwd.get(srcPath, false);

			if (pathName.startsWith("/")) {
				fileOrDir.move(pathName);
			} else {
				fileOrDir.move(pwd.getPath() + "/" + pathName);
			}

		} catch (NotDirectoryException | NoSuchFileOrDirectoryException
				| RemoteException | FileOrDirectoryAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Deletes a File/Dir
	 * 
	 * @param pathName
	 * @param mode
	 */
	public void delete(String pathName, boolean mode) {
		try {

			RemoteFileOrDir fileOrDir = pwd.get(pathName, false);
			if (fileOrDir.getType() == Type.File) {
				ClientFile fileToDelete = nameNode.getFile(pathName);
				fileToDelete.delete();
			} else if (fileOrDir.getType() == Type.Directory) {
				RemoteDir dir = nameNode.getDir(pathName);
				dir.delete(mode);
			}

		} catch (NotDirectoryException | RemoteException
				| NoSuchFileOrDirectoryException e) {
			e.printStackTrace();
		} catch (DirectoryNotEmptyException e) {
			System.out.println("Directory is Not Empty");
		} catch (NotFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileAlreadyOpenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void printDirContent(String dirName) {
		try {
			RemoteDir dir = nameNode.getDir(dirName);
			for (RemoteDir subDir : dir.getSubDirs()) {
				LOGGER.debug("*" + subDir.getPath());
			}
			for (ClientFile file : dir.getFiles()) {
				LOGGER.debug("-" + file.getName());
			}
		} catch (RemoteException | NotDirectoryException
				| NoSuchFileOrDirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void debugPrintNamespace(String prefix, RemoteDir dir)
			throws RemoteException {
		
		System.out.println(prefix + "*" + dir.getName());
		for (RemoteDir subDir : dir.getSubDirs())
			debugPrintNamespace(prefix + "  ", subDir);
		for (ClientFile file : dir.getFiles())
			System.out.println(prefix + "-" + file.getName());
	}

	public void listDirectory(String path) throws RemoteException {
		RemoteDir dir;
		try {
			dir = nameNode.getDir(path);
			// System.out.println(dir.getPath());
			for (RemoteDir subDir : dir.getSubDirs()) {
				LOGGER.debug("*" + subDir.getName());
			}
			for (ClientFile file : dir.getFiles()) {
				LOGGER.debug("-" + file.getName());
			}
		} catch (NotDirectoryException | NoSuchFileOrDirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void displayBanner() {
		try {
			String content = Files
					.toString(
							new File(
									"/home/govind/PhD/Lecture/SWEng/Project/Code/YAHAS3/src/Banner"),
							Charsets.UTF_8);
			System.out.println(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void displayHelp() {
		try {
			String content = Files
					.toString(
							new File(
									"/home/govind/PhD/Lecture/SWEng/Project/Code/YAHAS3/src/Help"),
							Charsets.UTF_8);
			System.out.println(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void putFile(String pathName, int repFactor) {
		try {
			String content = Files.toString(new File(pathName), Charsets.UTF_8);
			createFile(pathName, repFactor, content.getBytes());
		} catch (IOException e) {
			System.out.println("Local File Not Found");
			e.printStackTrace();
		}
	}

	public void operations(Client client) {

		try {
			rootDir = client.nameNode.getDir("");
			pwd = rootDir;
			client.createFile("/world", 2, "file data at ROOT".getBytes());
			client.createDir("/dir1/");
			client.createFile("/dir1/world", 2,
					"file data inside dir1".getBytes());
			client.createDir("/dir2/");
			client.createDir("/dir3/");
			// client.printDirContent("dir1");
			client.debugPrintNamespace("", rootDir);
		} catch (RemoteException | NotDirectoryException
				| NoSuchFileOrDirectoryException e) {

			e.printStackTrace();
		}

	}

	public static String readLineFromConsole() throws InputMismatchException {
		Scanner in = new Scanner(System.in);
		String str;
		System.out.print(">");
		str = in.nextLine();
		// in.close();
		return str;
	}

	public static void main(String args[]) {
		LOGGER.info("Connecting to NameNode......");
		String host = "localhost";
		String nameNodeAddress = "//" + host + "/NameNode";
		RMIHelper.maybeStartSecurityManager();
		Remote nameNode = RMIHelper.lookup(nameNodeAddress);
		Client client = new Client((ClientNameNodeProtocol) nameNode);
		client.displayBanner();
		client.operations(client);

		while (true) {

			String command = readLineFromConsole();

			if (command.contains("ls")) {
				try {
					client.listDirectory(pwd.getPath());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else if (command.contains("tree")) {
				try {
					System.out.println("Printing Name Space of YAHAS");
					client.debugPrintNamespace("", pwd);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (command.contains("pwd")) {
				try {
					if (pwd.getPath().equals("")) {
						System.out.println("/");
					} else {
						System.out.println(pwd.getPath());
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else if (command.contains(("mkdir"))) {
				if (command.contains("-")) {
					// String dirName = command.split("-")[1];
					List<String> parameters = Arrays.asList(command.split("-"));
					System.out.println(parameters);
					System.out.println("Creating Directory "
							+ parameters.get(parameters.size() - 1));
					// TODO::Check for Absolute or Relative Path
					client.createDir(parameters.get(parameters.size() - 1));
				} else {
					LOGGER.error("Incorrect Command Format");
				}
			} else if (command.contains(("cd"))) {

				List<String> parameters = Arrays.asList(command.split("-"));
				if (parameters.size() <= 1) {
					System.out.println("Incorrect Command Format");
					continue;
				}
				System.out.println("Changing directory to -->"
						+ parameters.get(parameters.size() - 1));
				try {
					if (parameters.get(parameters.size() - 1).startsWith("/")) {
						pwd = client.nameNode.getDir(parameters.get(parameters
								.size() - 1));
					} else {
						pwd = client.nameNode.getDir(pwd.getPath() + "/"
								+ parameters.get(parameters.size() - 1));
					}

					System.out.println("Changed path to " + pwd.getPath());
				} catch (NotDirectoryException | RemoteException
						| NoSuchFileOrDirectoryException e) {

					e.printStackTrace();
				}
			} else if (command.contains("mv")) {
				List<String> parameters = Arrays.asList(command.split("-"));
				if (parameters.size() <= 2) {
					System.out.println("Incorrect Command Format !!");
					continue;
				}
				LOGGER.debug("Moving Directory to "
						+ parameters.get(parameters.size() - 1));
				try {
					client.move(pwd.getPath(),
							parameters.get(parameters.size() - 1));

				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			else if (command.contains("rm")) {
				List<String> parameters = Arrays.asList(command.split("-"));
				if (parameters.size() <= 1) {
					System.out.println("Incorrect Command Format !!");
					continue;
				}
				LOGGER.debug("Removing directory "
						+ parameters.get(parameters.size() - 1));
				if (parameters.get(1).equals("f") && parameters.size() == 3) {
					client.delete(parameters.get(parameters.size() - 1), true);
				} else {
					client.delete(parameters.get(parameters.size() - 1), false);
				}
			}

			else if (command.contains("put")) {
				List<String> parameters = Arrays.asList(command.split("-"));
				LOGGER.debug("Creating File "
						+ parameters.get(parameters.size() - 2)
						+ " with rep factor of "
						+ parameters.get(parameters.size() - 1));
				if (parameters.size() <= 2) {
					System.out
							.println("Please enter the file and the replication factor!");
				} else {
					client.putFile(
							parameters.get(parameters.size() - 2),
							Integer.parseInt(parameters.get(parameters.size() - 1)));
				}

			}

			else if (command.contains("help")) {
				client.displayHelp();
			} else if (command.contains("quit")) {
				System.out.println("Bye !!");
				return;
			} else {
				System.out.println("Command Not Found");
			}

		}

	}
}