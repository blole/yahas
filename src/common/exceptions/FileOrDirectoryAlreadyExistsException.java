package common.exceptions;

public class FileOrDirectoryAlreadyExistsException extends Exception {
	private static final long serialVersionUID = 7973896349798146388L;

	public FileOrDirectoryAlreadyExistsException(String path) {
		super(path);
	}
}
