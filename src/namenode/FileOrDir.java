package namenode;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.Remote;

import org.apache.log4j.Logger;
import org.javatuples.Pair;

import common.exceptions.NoSuchFileOrDirectoryException;

abstract class FileOrDir implements Remote  {
	private static final Logger LOGGER = Logger.getLogger(
			FileOrDir.class.getCanonicalName());
	
	public enum Type {
		File,
		Directory,
	};
	
	protected NameNodeDir parent;
	protected String name;
	
	public FileOrDir(String name) {
		
	}
	
	
	
	
	
	protected void rename(String newName) throws FileAlreadyExistsException {
		if (parent != null) {
			parent.moveHere(this, newName);
		}
		else
			this.name = newName;
	}
	
	public void delete() {
		if (parent != null) {
			parent.remove(this);
			parent = null;
			LOGGER.debug(String.format("%s '%s' deleted.", getType().name(), name));
		}
	}
	
	public void move(String path) throws NotDirectoryException,
					NoSuchFileOrDirectoryException, FileAlreadyExistsException {
		Pair<NameNodeDir, String> pair = getLastDir(parent, path, false);
		
		if (pair.getValue1() != null)
			pair.getValue0().moveHere(this, pair.getValue1());
		else
			pair.getValue0().moveHere(this, name);
	}
	
	public static Pair<NameNodeDir, String> getLastDir(NameNodeDir startingDir, String path,
			boolean createParentsAsNeeded) throws NotDirectoryException, NoSuchFileOrDirectoryException {
		path.replaceAll("/+$", "");
		int lastSlashIndex = path.lastIndexOf('/');
		
		if (lastSlashIndex < 0)
			lastSlashIndex = 0;
		String basePath = path.substring(0, lastSlashIndex);
		String existingDirNameOrName = path.substring(lastSlashIndex);
		
		NameNodeDir safeDir = startingDir.getDir(basePath, createParentsAsNeeded);
		try {
			FileOrDir maybeDir = safeDir.get(existingDirNameOrName, createParentsAsNeeded);
			if (maybeDir.getType() == Type.Directory)
				return new Pair<>((NameNodeDir)maybeDir, null);
		} catch (NoSuchFileOrDirectoryException e) {}
		
		return new Pair<>((NameNodeDir)safeDir, existingDirNameOrName);
	}
	
	
	
	
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return parent.getPath()+getName();
	}
	
	public abstract Type getType();
}
