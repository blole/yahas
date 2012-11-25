package common.exceptions;

public class NotFileException extends Exception {
	private static final long serialVersionUID = 3492314342425382029L;

	public NotFileException(String path) {
		super(path);
	}
}
