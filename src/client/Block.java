package client;

public class Block {
	private long id;
	
	public Block(long id) {
		this.id = id;
	}
	
	public void write(String s) {
		System.out.printf("%s wrote %s\n", this, s);
	}
	
	public String toString() {
		return "[Block "+id+"]";
	}
}
