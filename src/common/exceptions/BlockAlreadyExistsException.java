package common.exceptions;


public class BlockAlreadyExistsException extends Exception {
	private static final long serialVersionUID = 3245235990347442482L;

	public BlockAlreadyExistsException(long blockID) {
		super(String.valueOf(blockID));
	}
}
