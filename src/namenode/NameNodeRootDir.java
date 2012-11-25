package namenode;

import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.NotDirectoryException;

public class NameNodeRootDir extends NameNodeDir {
	public NameNodeRootDir() {
		super("/");
		this.parent = this;
	}
	
	@Override
	public void move(String pathTo) throws NotDirectoryException {
		throw new NotDirectoryException(getPath());
	}
	
	@Override
	public void delete(boolean force) throws DirectoryNotEmptyException {
		throw new DirectoryNotEmptyException(getPath());
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
