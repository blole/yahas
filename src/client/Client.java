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
	ClientNameNodeProtocol nameNode = null;

	static RemoteDir pwd = null;
	static RemoteDir rootDir = null;

	public Client(ClientNameNodeProtocol nameNode) {
		this.nameNode = nameNode;
	}

	public void createFile(String filePath, int repFactor, byte[] contents) {
		try (ClientFile file = nameNode.createFile(new File(filePath).getName(), (byte) repFactor,
				65536)) {
			file.open();
			file.write(contents);
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
			RemoteFileOrDir fileOrDir = pwd.getRemote(srcPath, false);

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
			RemoteFileOrDir fileOrDir = pwd.getRemote(pathName, false);
			if (fileOrDir.getType() == Type.File) {
				pwd.getRemoteFile(pathName).delete();
			} else if (fileOrDir.getType() == Type.Directory) {
				pwd.getDir(pathName).delete(mode);
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

	public void debugPrintNamespace(String prefix, RemoteDir dir)
			throws RemoteException {
		
		System.out.println(prefix + "*" + dir.getName());
		for (RemoteDir subDir : dir.getSubDirs())
			debugPrintNamespace(prefix + "  ", subDir);
		for (ClientFile file : dir.getFiles())
			System.out.println(prefix + "-" + file.getName());
	}

	public void listDirectory(String path) throws RemoteException {
		try {
			RemoteDir dir = nameNode.getDir(path);
			for (RemoteDir subDir : dir.getSubDirs()) {
				System.out.println("\033[1;34m" + subDir.getName()+"\033[0m");
			}
			for (ClientFile file : dir.getFiles()) {
				System.out.println(file.getName());
			}
		} catch (RemoteException | NotDirectoryException
				| NoSuchFileOrDirectoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void displayBanner() {
		try {
			String content = Files.toString(new File("../misc/banner"), Charsets.UTF_8);
			System.out.println(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void displayHelp() {
		try {
			String content = Files.toString(new File("../misc/help"), Charsets.UTF_8);
			System.out.println(content);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void putFile(String pathName, int repFactor) {
		pathName = pathName.trim();
		File file = new File(pathName);
		try {
			byte fileContents[] = Files.toByteArray(file);
			createFile(pathName, repFactor, fileContents);
		} catch (IOException e) {
			System.out.println("Local File Not Found");
			e.printStackTrace();
		}
	}

	public void operations(Client client) {

		try {
			pwd = rootDir = client.nameNode.getDir("");
		} catch (RemoteException | NotDirectoryException
				| NoSuchFileOrDirectoryException e) {
			e.printStackTrace();
		}

	}

	public static String readLineFromConsole() throws InputMismatchException {
		Scanner in = new Scanner(System.in);
		String str;
		System.out.print("> ");
		str = in.nextLine();
		// in.close();
		return str;
	}

	public static void main(String args[]) {
		String host = "localhost";
		String nameNodeAddress = "//" + host + "/NameNode";
		RMIHelper.maybeStartSecurityManager();
		Remote nameNode = RMIHelper.lookup(nameNodeAddress);
		Client client = new Client((ClientNameNodeProtocol) nameNode);
		client.displayBanner();
		client.operations(client);

		while (true) {

			String command = readLineFromConsole().trim();

			if (command.startsWith("ls")) {
				try {
					client.listDirectory(pwd.getPath());
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else if (command.startsWith("tree")) {
				try {
					System.out.println("Printing Name Space of YAHAS");
					client.debugPrintNamespace("", pwd);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (command.startsWith("pwd")) {
				try {
					if (pwd.getPath().equals("")) {
						System.out.println("/");
					} else {
						System.out.println(pwd.getPath());
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			} else if (command.startsWith(("mkdir"))) {
				List<String> parameters = Arrays.asList(command.split(" +"));
				client.createDir(parameters.get(parameters.size() - 1));
			} else if (command.startsWith(("cd"))) {

				List<String> parameters = Arrays.asList(command.split(" +"));
				if (parameters.size() <= 1) {
					System.out.println("Incorrect Command Format");
					continue;
				}
				try {
					if (parameters.get(parameters.size() - 1).startsWith("/")) {
						pwd = client.nameNode.getDir(parameters.get(parameters
								.size() - 1));
					} else {
						pwd = client.nameNode.getDir(pwd.getPath() + "/"
								+ parameters.get(parameters.size() - 1));
					}
				} catch (NotDirectoryException | RemoteException
						| NoSuchFileOrDirectoryException e) {

					e.printStackTrace();
				}
			} else if (command.startsWith("mv")) {
				List<String> parameters = Arrays.asList(command.split(" +"));
				if (parameters.size() != 3) {
					System.out.println("Incorrect Command Format !!");
					continue;
				}
				try {
					client.move(pwd.getPath()+"/"+parameters.get(1), parameters.get(2));
				} catch (RemoteException e) {
					System.err.println("error");
				}
				
			} else if (command.startsWith("rm")) {
				List<String> parameters = Arrays.asList(command.split(" +"));
				if (parameters.size() <= 1) {
					System.out.println("Incorrect Command Format !!");
					continue;
				}
				if (parameters.get(1).equals("-f") && parameters.size() == 3) {
					client.delete(parameters.get(parameters.size() - 1), true);
				} else {
					client.delete(parameters.get(parameters.size() - 1), false);
				}
			}

			else if (command.startsWith("put")) {
				List<String> parameters = Arrays.asList(command.split(" +"));
				System.out.println("Creating File "
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
			else if (command.startsWith("cat")) {
				List<String> parameters = Arrays.asList(command.split(" +"));
				System.out.println(client.readFile(parameters.get(parameters.size() - 1)));
			}
			else if (command.startsWith("help")) {
				client.displayHelp();
			} else if (command.startsWith("quit")) {
				System.out.println("Bye !!");
				return;
			} else if (command.equals("")) {
			} else {
				System.out.println("Command Not Found");
			}

		}

	}
}