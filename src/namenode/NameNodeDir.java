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

import client.ClientFile;

import common.RMIHelper;
import common.exceptions.FileOrDirectoryAlreadyExistsException;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.exceptions.NotFileException;
import common.protocols.RemoteDir;
import common.protocols.RemoteFileOrDir;

public class NameNodeDir extends NameNodeFileOrDir implements RemoteDir {
	private static final Logger LOGGER = Logger.getLogger(
			NameNodeDir.class.getCanonicalName());
	
	private final HashMap<String, NameNodeFileOrDir> contents = new HashMap<>();
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
	
	
	
	
	@Override
	public RemoteFileOrDir getRemote(String path, boolean createParentsAsNeeded) throws NotDirectoryException, NoSuchFileOrDirectoryException {
		return get(path, createParentsAsNeeded).getStub();
	}
	
	public NameNodeFileOrDir get(String path, boolean createParentsAsNeeded) throws NoSuchFileOrDirectoryException, NotDirectoryException {
		NameNodeFileOrDir dir;
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
					throw new NotDirectoryException(dir.getPath());
				NameNodeFileOrDir child = ((NameNodeDir)dir).contents.get(dirName);
				if (child == null) {
					if (createParentsAsNeeded) {
						try {
							child = ((NameNodeDir)dir).createDirHere(dirName);
						} catch (FileOrDirectoryAlreadyExistsException e) {
							LOGGER.error("This should never happen. Concurrency issues?", e);
						}
					}
					else
						throw new NoSuchFileOrDirectoryException(((NameNodeDir)dir).getPath()+dirName);
				}
				dir = child;
				break;
			}
		}
		return dir;
	}
	
	@Override
	public ClientFile getRemoteFile(String fullPath) throws NotDirectoryException, NoSuchFileOrDirectoryException, NotFileException {
		return getFile(fullPath).getYAHASFile();
	}
	
	public NameNodeFile getFile(String fullPath) throws NotDirectoryException, NoSuchFileOrDirectoryException, NotFileException {
		NameNodeFileOrDir file = get(fullPath, false);
		if (file.getType() != Type.File) {
			LOGGER.warn("Tried to serve "+file+" as a file");
			throw new NotFileException(file.getPath());
		}
		else {
			LOGGER.debug(file+ " served");
			return (NameNodeFile) file;
		}
	}
	
	public NameNodeDir getLocalDir(String fullPath, boolean createParentsAsNeeded) throws NotDirectoryException, NoSuchFileOrDirectoryException {
		NameNodeFileOrDir dir = get(fullPath, createParentsAsNeeded);
		if (dir.getType() != Type.Directory)
			throw new NotDirectoryException(dir.getPath());
		else
			return (NameNodeDir) dir;
	}
	
	@Override
	public RemoteDir getDir(String fullPath) throws NotDirectoryException, NoSuchFileOrDirectoryException {
		return getLocalDir(fullPath, false).stub;
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
		path = path.replaceAll("/+$", ""); //remove trailing slashes
		int lastSlashIndex = path.lastIndexOf('/');
		
		NameNodeDir safeDir;
		String existingDirNameOrName;
		if (lastSlashIndex < 0) {
			existingDirNameOrName = path;
			safeDir = this;
		}
		else {
			String basePath = path.substring(0, lastSlashIndex);
			existingDirNameOrName = path.substring(lastSlashIndex+1);
			safeDir = getLocalDir(basePath, createParentsAsNeeded);
		}
		
		try {
			NameNodeFileOrDir maybeDir = safeDir.get(existingDirNameOrName, false);
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
			throws NotDirectoryException, FileOrDirectoryAlreadyExistsException, NoSuchFileOrDirectoryException {
		Pair<NameNodeDir, String> pair = getLastDir(path, createParentsAsNeeded);
		
		if (pair.getValue1() == null)
			return pair.getValue0();
		else
			return pair.getValue0().createDirHere(pair.getValue1());
	}
	
	private NameNodeDir createDirHere(String subDirName) throws FileOrDirectoryAlreadyExistsException {
		NameNodeDir subDir = new NameNodeDir(subDirName);
		moveHere(subDir, subDirName);
		return subDir;
	}
	
	public void moveHere(NameNodeFileOrDir node, String newName) throws FileOrDirectoryAlreadyExistsException {
		NameNodeFileOrDir alreadyExisting = contents.get(newName);
		if (alreadyExisting != null)
			throw new FileOrDirectoryAlreadyExistsException(alreadyExisting.getPath());
		if (node.parent != null)
			node.parent.contents.remove(node.getName());
		node.parent = this;
		node.name = newName;
		contents.put(newName, node);
	}
	
	
	
	
	@Override
	public void delete(boolean force) throws DirectoryNotEmptyException {
		if (force || contents.size() == 0)
			super.delete();
		else
			throw new DirectoryNotEmptyException(getPath());
	}
	
	@Override
	public List<ClientFile> getFiles() {
		List<ClientFile> files = new ArrayList<>();
		for (NameNodeFileOrDir file : contents.values()) {
			if (file.getType() == Type.File)
				files.add(((NameNodeFile)file).getYAHASFile());
		}
		LOGGER.debug(toString()+" listed files");
		return files;
	}

	@Override
	public List<RemoteDir> getSubDirs() {
		List<RemoteDir> subDirs = new ArrayList<>();
		for (NameNodeFileOrDir dir : contents.values()) {
			if (dir.getType() == Type.Directory)
				subDirs.add(((NameNodeDir)dir).getStub());
		}
		LOGGER.debug(toString()+" listed sub-directories");
		return subDirs;
	}
	
	
	
	
	
	void remove(NameNodeFileOrDir member) {
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

	@Override
	public RemoteDir getStub() {
		return stub;
	}
	
	@Override
	public String toString() {
		return "[Dir "+getPath()+"]";
	}
}
