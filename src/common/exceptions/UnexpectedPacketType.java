package common.exceptions;

public class UnexpectedPacketType extends Exception {
	private static final long serialVersionUID = -6966388614017978544L;

	public UnexpectedPacketType(String string) {
		super(string);
	}
}
