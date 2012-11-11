package namenode;

import common.exceptions.RemoteDirNotEmptyException;
import common.exceptions.RemoteDirNotFoundException;

public class NameNodeRootDir extends NameNodeDir {

	public NameNodeRootDir() {
		super(null);
		this.parent = this;
	}
	
	@Override
	public void move(String pathTo) throws RemoteDirNotFoundException {
		throw new RemoteDirNotFoundException();
	}
	
	@Override
	public void delete(boolean recursively) throws RemoteDirNotEmptyException {
		throw new RemoteDirNotEmptyException();
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
