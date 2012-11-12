package namenode;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.javatuples.Pair;

import client.YAHASFile;

import common.RMIHelper;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.exceptions.NotFileException;
import common.protocols.RemoteDir;

public class NameNodeDir extends FileOrDir implements RemoteDir {
	private static final Logger LOGGER = Logger.getLogger(
			NameNodeDir.class.getCanonicalName());
	
	private final HashMap<String, FileOrDir> contents = new HashMap<>();
	private RemoteDir stub;
	
	/**
	 * Create a regular dir.
	 * @param name
	 */
	public NameNodeDir(String name) {
		super(name);
		try {
			this.stub = (RemoteDir) RMIHelper.getStub(this);
		} catch (RemoteException e) {
			LOGGER.error("Error creating dir "+name, e);
		}
	}
	
	
	
	
	
	public FileOrDir get(String path, boolean createParentsAsNeeded) throws NoSuchFileOrDirectoryException, NotDirectoryException {
		FileOrDir dir;
		if (path.startsWith("/"))
			dir = getRoot();
		else
			dir = this;
		
		for (String dirName : path.split("/")) {
			switch (dirName) {
			case "":
			case ".":
				break;
			case "..":
				dir = dir.parent;
				break;
			default:
				if (dir.getType() != Type.Directory)
					throw new NotDirectoryException(dirName);
				dir = ((NameNodeDir)dir).contents.get(dirName);
				if (dir == null) {
					if (createParentsAsNeeded) {
						try {
							dir = createDirHere(dirName);
						} catch (FileAlreadyExistsException e) {
							LOGGER.error("This should never happen. Concurrency issues?", e);
						}
					}
					else
						throw new NoSuchFileOrDirectoryException();
				}
				break;
			}
		}
		return dir;
	}
	
	@Override
	public NameNodeFile getFile(String fullPath) throws NotDirectoryException, NoSuchFileOrDirectoryException, NotFileException {
		FileOrDir file = get(fullPath, false);
		if (file.getType() != Type.File) {
			LOGGER.warn("Tried to serve "+file+" as a file");
			throw new NotFileException();
		}
		else {
			LOGGER.debug(file+ " serve");
			return (NameNodeFile) file;
		}
	}
	
	public NameNodeDir getDir(String fullPath, boolean createParentsAsNeeded) throws NotDirectoryException, NoSuchFileOrDirectoryException {
		FileOrDir dir = get(fullPath, createParentsAsNeeded);
		if (dir.getType() != Type.Directory)
			throw new NotDirectoryException(dir.getName());
		else
			return (NameNodeDir) dir;
	}
	
	/**
	 * If {@code createParentsAsNeeded} is true, the function will try to create
	 * try to create directories up to the next last level, but never for
	 * the last level, since it doesn't know if you were looking for a
	 * file or a directory.
	 * 
	 * @param startingDir
	 * @param path
	 * @param createParentsAsNeeded
	 * @return
	 * @throws NotDirectoryException
	 * @throws NoSuchFileOrDirectoryException
	 */
	public Pair<NameNodeDir, String> getLastDir(String path, boolean createParentsAsNeeded)
					throws NotDirectoryException, NoSuchFileOrDirectoryException {
		path.replaceAll("/+$", ""); //remove trailing slashes
		int lastSlashIndex = path.lastIndexOf('/');
		
		if (lastSlashIndex < 0)
			lastSlashIndex = 0;
		String basePath = path.substring(0, lastSlashIndex);
		String existingDirNameOrName = path.substring(lastSlashIndex);
		
		NameNodeDir safeDir = getDir(basePath, createParentsAsNeeded);
		try {
			FileOrDir maybeDir = safeDir.get(existingDirNameOrName, false);
			if (maybeDir.getType() == Type.Directory)
				return new Pair<>((NameNodeDir)maybeDir, null);
		} catch (NoSuchFileOrDirectoryException e) {}
		
		return new Pair<>((NameNodeDir)safeDir, existingDirNameOrName);
	}
	
	
	
	
	
	/**
	 * 
	 * @param path
	 * @param createParentsAsNeeded
	 * @return the newly created dir, or null if no dir was created because
	 * there were non-existing dirs in the path.
	 * Never returns null if {@code createParentsAsNeeded} is set to true.  
	 * @throws NotDirectoryException 
	 * @throws FileAlreadyExistsException 
	 * @throws NoSuchFileOrDirectoryException 
	 */
	public NameNodeDir createDir(String path, boolean createParentsAsNeeded)
			throws NotDirectoryException, FileAlreadyExistsException, NoSuchFileOrDirectoryException {
		Pair<NameNodeDir, String> pair = getLastDir(path, createParentsAsNeeded);
		
		if (pair.getValue1() == null)
			return pair.getValue0();
		else
			return pair.getValue0().createDirHere(pair.getValue1());
	}
	
	private NameNodeDir createDirHere(String subDirName) throws FileAlreadyExistsException {
		NameNodeDir subDir = new NameNodeDir(subDirName);
		moveHere(subDir, subDirName);
		return subDir;
	}
	
	public void moveHere(FileOrDir node, String newName) throws FileAlreadyExistsException {
		if (contents.containsKey(newName))
			throw new FileAlreadyExistsException(newName);
		if (node.parent != null)
			node.parent.contents.remove(node.getName());
		node.parent = this;
		node.name = newName;
		contents.put(newName, node);
	}
	
	
	
	
	@Override
	public void delete(boolean force) throws DirectoryNotEmptyException {
		if (parent != null) {
			if (force || contents.size() == 0)
				super.delete();
		}
		else
			throw new DirectoryNotEmptyException(getPath());
	}
	
	@Override
	public List<YAHASFile> getFiles() {
		List<YAHASFile> files = new ArrayList<>();
		for (FileOrDir file : contents.values()) {
			if (file.getType() == Type.File)
				files.add(((NameNodeFile)file).getYAHASFile());
		}
		LOGGER.debug(toString()+" listed files");
		return files;
	}

	@Override
	public List<RemoteDir> getSubDirs() {
		List<RemoteDir> subDirs = new ArrayList<>();
		for (FileOrDir dir : contents.values()) {
			if (dir.getType() == Type.Directory)
				subDirs.add(((NameNodeDir)dir).getStub());
		}
		LOGGER.debug(toString()+" listed sub-directories");
		return subDirs;
	}
	
	
	
	
	
	void remove(FileOrDir member) {
		contents.remove(member.getName());
	}
	
	protected boolean isRoot() {
		return false;
	}
	
	private NameNodeDir getRoot() {
		NameNodeDir dir = this;
		while (!dir.isRoot())
			dir = dir.parent;
		return dir;
	}
	
	@Override
	public Type getType() {
		return Type.Directory;
	}
	
	@Override
	public String getPath() {
		return super.getPath()+"/";
	}

	public RemoteDir getStub() {
		return stub;
	}
	
	@Override
	public String toString() {
		return "[Dir "+getPath()+"]";
	}
}
