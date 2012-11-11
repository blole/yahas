package namenode;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

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
	
	
	
	
	
//	@Override
//	public NameNodeFile getFile(String fullPath) {
//		fullPath = fullPath.replaceAll("/+", "/");
//		int lastSlash = fullPath.lastIndexOf('/');
//		String path = fullPath.substring(0, lastSlash);
//		String fileName = fullPath.substring(lastSlash);
//		
//		NameNodeDir dir = getDir(path);
//		TreeMember file = getDir(path).contents.get(fileName);
//		if (file == null) {
//			if (dir.subDirs.get(fileName) != null)
//				throw new No
//		}
//		if (fileName)
//		
//		if (file == null)
//			throw new FileNotFoundException(fileName);
//		
//		return file;
//		
//		
//		
//		
//		String[] s = path.split("/", 2);
//		if (s.length == 1) {
//			NameNodeFile file = contents.get(path);
//			if (file == null)
//				throw new RemoteFileNotFoundException();
//			return file;
//		}
//		else {
//			NameNodeDir subDir = subDirs.get(s[0]);
//			if (subDir == null)
//				throw new RemoteFileNotFoundException();
//			else
//				return subDir.getFile(s[1]);
//		}
//	}
	
	public FileOrDir get(String path) throws NoSuchFileOrDirectoryException, NotDirectoryException {
		FileOrDir dir = this;
//		int i=0;
		//path = path.replaceAll("/+", "/");
		if (path.startsWith("/")) { //get root
			while (dir != dir.parent)
				dir = dir.parent;
//			i=1;
		}
		
		for (String dirName : path.split("/")) {
//		for (; i<s.length; i++) {
//			String dirName = s[i];
//			
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
				if (dir == null)
					throw new NoSuchFileOrDirectoryException();
				break;
			}
		}
		return dir;
	}
	
	@Override
	public NameNodeFile getFile(String fullPath) throws NotDirectoryException, NoSuchFileOrDirectoryException, NotFileException {
		FileOrDir file = get(fullPath);
		if (file.getType() != Type.File) {
			LOGGER.warn("Tried to serve "+file+" as a file");
			throw new NotFileException();
		}
		else {
			LOGGER.debug(file+ " serve");
			return (NameNodeFile) file;
		}
	}
	
	public NameNodeDir getDir(String fullPath) throws NotDirectoryException, NoSuchFileOrDirectoryException {
		FileOrDir dir = get(fullPath);
		if (dir.getType() != Type.Directory)
			throw new NotDirectoryException(dir.getName());
		else
			return (NameNodeDir) dir;
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
		String[] s = path.split("/+", 2);
		if (s[0].length() == 0)
			s = s[1].split("/+", 2);//if the first iteration was called with a string starting with a slash
		
		if (s.length == 1)
			return createDirHere(path);
		else {
			FileOrDir subDir = contents.get(s[0]);
			if (subDir != null) {
				if (subDir.getType() != Type.Directory)
					throw new NotDirectoryException(subDir.getName());
				else
					return ((NameNodeDir)subDir).createDir(s[1], createParentsAsNeeded);
			}
			else if (createParentsAsNeeded)
				return createDirHere(s[0]).createDir(s[1], createParentsAsNeeded);
			else
				throw new NoSuchFileOrDirectoryException();
		}
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
			while (!dir.isRoot())
	protected boolean isRoot() {
		return false;
	}





	}
	
	
	
	
	@Override
	public void delete(boolean force) throws DirectoryNotEmptyException {
		if (parent != null) {
			if (force || contents.size() == 0)
				super.delete();
		if (recursively || (subDirs.size() == 0 && files.size() == 0))
			parent.subDirs.remove(this);
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
		LOGGER.debug(String.format("Listed files in %s", this));
		return files;
	}

	@Override
	public List<RemoteDir> getSubDirs() {
		List<RemoteDir> subDirs = new ArrayList<>();
		for (FileOrDir dir : contents.values()) {
			if (dir.getType() == Type.Directory)
				subDirs.add(((NameNodeDir)dir).getStub());
		}
		LOGGER.debug(String.format("Listed sub-directories in %s", this));
		return subDirs;
	}
	
	
	
	
	
	void remove(FileOrDir member) {
		contents.remove(member.getName());
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
	public String toString() {
		return getName();
	}
	
	public RemoteDir getStub() {
		return stub;
	}
}
