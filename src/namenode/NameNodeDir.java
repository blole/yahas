package namenode;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import client.YAHASFile;

import common.RMIHelper;
import common.exceptions.RemoteDirNotEmptyException;
import common.exceptions.RemoteDirNotFoundException;
import common.exceptions.RemoteFileNotFoundException;
import common.protocols.RemoteDir;

public class NameNodeDir implements RemoteDir {
	private static final Logger LOGGER = Logger.getLogger(
			NameNodeDir.class.getCanonicalName());
	
	private String name;
	protected NameNodeDir parent;
	private final HashMap<String, NameNodeDir> subDirs = new HashMap<>();
	private final HashMap<String, NameNodeFile> files = new HashMap<>();
	private RemoteDir stub;
	
	/**
	 * Create a regular dir.
	 * @param name
	 */
	public NameNodeDir(String name) {
		this.name = name;
		try {
			this.stub = (RemoteDir) RMIHelper.getStub(this);
		} catch (RemoteException e) {
			LOGGER.error("Error creating dir "+name, e);
		}
	}
	
	
	
	
	
	public void addFile(NameNodeFile file) {
		files.put(file.getName(), file);
		file.setParentDir(this);
	}
	
	@Override
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
	
	/**
	 * 
	 * @param path
	 * @param createParentsAsNeeded
	 * @return the newly created dir, or null if no dir was created because
	 * there were non-existing dirs in the path.
	 * Never returns null if {@code createParentsAsNeeded} is set to true.  
	 */
	public NameNodeDir addDir(String path, boolean createParentsAsNeeded) {
		String[] s = path.split("/", 2);
		if (s.length == 1)
			return addSubDir(path);
		else {
			NameNodeDir subDir = subDirs.get(s[0]);
			if (subDir != null)
				return subDir.addDir(s[1], createParentsAsNeeded);
			else if (createParentsAsNeeded) {
				return addSubDir(s[0]).addDir(s[1], createParentsAsNeeded);
			}
			else
				return null;
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
			while (!dir.isRoot())
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
	
	protected boolean isRoot() {
		return false;
	}





	public NameNodeDir getSubDir(String subDirName) throws RemoteDirNotFoundException {
		NameNodeDir subDir = subDirs.get(subDirName);
		if (subDir == null)
			throw new RemoteDirNotFoundException();
		return subDir;
	}
	
	
	
	

	public void removeFile(NameNodeFile file) {
		files.remove(file);
	}
	
	@Override
	public void move(String pathTo) throws RemoteDirNotFoundException {
		getDir(pathTo).addSubDir(this);
	}
	
	@Override
	public void delete(boolean recursively) throws RemoteDirNotEmptyException {
		if (recursively || (subDirs.size() == 0 && files.size() == 0))
			parent.subDirs.remove(this);
		else
			throw new RemoteDirNotEmptyException();
	}
	
	@Override
	public List<YAHASFile> getFiles() {
		List<YAHASFile> files = new ArrayList<>();
		for (NameNodeFile file : this.files.values())
			files.add(file.getYAHASFile());
		LOGGER.debug(String.format("Listed files in %s", this));
		return files;
	}

	@Override
	public List<RemoteDir> getSubDirs() {
		List<RemoteDir> subDirs = new ArrayList<>();
		for (NameNodeDir childDir : this.subDirs.values())
			subDirs.add(childDir.getStub());
		LOGGER.debug(String.format("Listed sub-directories in %s", this));
		return subDirs;
	}
	
	
	
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPath() {
		return parent.getPath()+getName()+"/";
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public RemoteDir getStub() {
		return stub;
	}
}
