package namenode;

import java.nio.file.NotDirectoryException;

import org.apache.log4j.Logger;
import org.javatuples.Pair;

import common.exceptions.FileOrDirectoryAlreadyExistsException;
import common.exceptions.NoSuchFileOrDirectoryException;
import common.protocols.RemoteFileOrDir;

public abstract class NameNodeFileOrDir implements RemoteFileOrDir {
	private static final Logger LOGGER = Logger.getLogger(
			NameNodeFileOrDir.class.getCanonicalName());
	

	
	protected NameNodeDir parent;
	protected String name;
	
	public NameNodeFileOrDir(String name) {
		this.name = name;
	}
	
	
	
	
	
	protected void rename(String newName) throws FileOrDirectoryAlreadyExistsException {
		if (parent != null) {
			parent.moveHere(this, newName);
		}
		else
			this.name = newName;
	}
	
	public void delete() {
		if (parent != null) {
			LOGGER.debug(this+" deleted");
			parent.remove(this);
			parent = null;
		}
	}
	
	public void move(String path) throws NotDirectoryException,
					NoSuchFileOrDirectoryException, FileOrDirectoryAlreadyExistsException {
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
	
	abstract public Type getType();

	abstract public RemoteFileOrDir getStub();
}
