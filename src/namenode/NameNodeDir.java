package namenode;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import common.RMIHelper;
import common.exceptions.RemoteDirNotEmptyException;
import common.exceptions.RemoteDirNotFoundException;
import common.exceptions.RemoteFileNotFoundException;
import common.protocols.RemoteDir;
import common.protocols.RemoteFile;

public class NameNodeDir implements RemoteDir {
	private String name;
	private NameNodeDir parent;
	private final HashMap<String, NameNodeDir> subDirs = new HashMap<>();
	private final HashMap<String, NameNodeFile> files = new HashMap<>();
	
	/**
	 * Create a root dir.
	 */
	public NameNodeDir() {
		this.name = "";
		this.parent = this;
	}
	
	/**
	 * Create a regular dir.
	 * @param name
	 */
	public NameNodeDir(String name) {
		this.name = name;
	}
	
	
	
	
	
	public void addFile(NameNodeFile file) {
		files.put(file.getName(), file);
		file.setParentDir(this);
	}
	
	public NameNodeFile getFile(String path) throws RemoteFileNotFoundException {
		String[] s = path.split("/", 2);
		if (s.length == 1) {
			NameNodeFile file = files.get(path);
			if (file == null)
				throw new RemoteFileNotFoundException();
			return file;
		}
		else {
			NameNodeDir subDir = subDirs.get(s[0]);
			if (subDir == null)
				throw new RemoteFileNotFoundException();
			else
				return subDir.getFile(s[1]);
		}
	}
	
	public void addDir(String path, boolean createParentsAsNeeded) {
		String[] s = path.split("/", 2);
		if (s.length == 1)
			addSubDir(path);
		else {
			NameNodeDir subDir = subDirs.get(s[0]);
			if (subDir != null)
				subDir.addDir(s[1], createParentsAsNeeded);
			else if (createParentsAsNeeded) {
				subDir = this.addSubDir(s[0]);
				subDir.addDir(s[1], createParentsAsNeeded);
			}
		}
	}
	
	public NameNodeDir addSubDir(String subDirName) {
		NameNodeDir subDir = new NameNodeDir(subDirName);
		addSubDir(subDir);
		return subDir;
	}
	
	public void addSubDir(NameNodeDir subDir) {
		//if (subDirs.containsKey(subDir.getName()))
		//	throw new RemoteDirAlreadyContains....();
		subDirs.put(subDir.getName(), subDir);
		subDir.parent = this;
	}
	
	public NameNodeDir getDir(String path) throws RemoteDirNotFoundException {
		NameNodeDir dir = this;
		if (path.startsWith("/")) { //get root
			while (dir != dir.parent)
				dir = dir.parent;
		}
		
		String[] s = path.split("/");
		for (String movement : s) {
			switch (movement) {
			case "":
			case ".":
				break;
			case "..":
				dir = dir.parent;
				break;
			default:
				dir = dir.getSubDir(movement);
				break;
			}
		}
		return dir;
	}
	
	public NameNodeDir getSubDir(String subDirName) throws RemoteDirNotFoundException {
		NameNodeDir subDir = subDirs.get(subDirName);
		if (subDir == null)
			throw new RemoteDirNotFoundException();
		return subDir;
	}
	
	
	
	
	
	public String getName() {
		return name;
	}

	public RemoteDir getStub() throws RemoteException {
		return (RemoteDir) RMIHelper.getStub(this);
	}
	
	
	
	

	public void removeFile(NameNodeFile file) {
		files.remove(file);
	}
	
	@Override
	public void delete(boolean recursively) throws RemoteException, RemoteDirNotEmptyException {
		if (parent != null) { //this isn't the root dir
			if (recursively || (subDirs.size() == 0 && files.size() == 0))
				parent.subDirs.remove(this);
			else
				throw new RemoteDirNotEmptyException();
		}
	}
	
	@Override
	public void move(String pathTo) throws RemoteException, RemoteDirNotFoundException {
		getDir(pathTo).addSubDir(this);
	}
	
	@Override
	public List<RemoteFile> getFiles() throws RemoteException {
		List<RemoteFile> files = new ArrayList<>();
		for (NameNodeFile file : this.files.values())
			files.add(file.getStub());
		return files;
	}

	@Override
	public List<RemoteDir> getSubDirs() throws RemoteException {
		List<RemoteDir> subDirs = new ArrayList<>();
		for (NameNodeDir childDir : this.subDirs.values())
			subDirs.add(childDir.getStub());
		return subDirs;
	}

	
	

	
}
