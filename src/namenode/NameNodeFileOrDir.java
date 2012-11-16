package namenode;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.rmi.Remote;

import org.apache.log4j.Logger;
import org.javatuples.Pair;

import common.exceptions.NoSuchFileOrDirectoryException;

abstract class NameNodeFileOrDir implements Remote  {
	private static final Logger LOGGER = Logger.getLogger(
			NameNodeFileOrDir.class.getCanonicalName());
	
	public enum Type {
		File,
		Directory,
	};
	
	protected NameNodeDir parent;
	protected String name;
	
	public NameNodeFileOrDir(String name) {
		this.name = name;
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
			LOGGER.debug(toString()+" deleted");
		}
	}
	
	public void move(String path) throws NotDirectoryException,
					NoSuchFileOrDirectoryException, FileAlreadyExistsException {
		Pair<NameNodeDir, String> pair = parent.getLastDir(path, false);
		
		if (pair.getValue1() != null)
			pair.getValue0().moveHere(this, pair.getValue1());
		else
			pair.getValue0().moveHere(this, name);
	}
	
	
	
	
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return parent.getPath()+getName();
	}
	
	public abstract Type getType();
}
