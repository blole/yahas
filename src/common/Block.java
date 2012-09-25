package common;

import java.rmi.RemoteException;


public class Block implements BlockInterface {
	private long id;
	
	public Block(long id) {
		this.id = id;
	}
	
	@Override
	public void write(String s) throws RemoteException {
		System.out.printf("%s wrote %s\n", this, s);
	}
	
	public String toString() {
		return "[Block "+id+"]";
	}
}
