package common.exceptions;

public class BadFileName extends Exception {
	private static final long serialVersionUID = 2595300025555806041L;

	public BadFileName(String name) {
		super(name);
	}
}
