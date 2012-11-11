package namenode;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NotDirectoryException;

public class NameNodeRootDir extends NameNodeDir {

	public NameNodeRootDir() {
		super(null);
		this.parent = this;
	}
	
	@Override
	public void move(String pathTo) throws NotDirectoryException {
		throw new NotDirectoryException("/");
	}
	
	@Override
	public void delete(boolean force) throws DirectoryNotEmptyException {
		throw new DirectoryNotEmptyException("/");
	}
	
	@Override
	protected boolean isRoot() {
		return true;
	}
	
	@Override
	public String getName() {
		return "/";
	}
	
	@Override
	public String getPath() {
		return "/";
	}
}
