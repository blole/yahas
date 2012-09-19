package common.exceptions;

public class InvalidPacketType extends Exception {
	private static final long serialVersionUID = -2040224824498769887L;

	public InvalidPacketType(String string) {
		super(string);
	}
}
