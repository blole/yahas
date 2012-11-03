package common;

public class Convert {
	/**
	 * Equivalent to {@code toByteArray(i, new byte[4], 0);}
	 * 
	 * @param i
	 * @return The int-encoding of i as an array of four bytes
	 */
	public static byte[] toByteArray(int i) {
		return toByteArray(i, new byte[4], 0); 
	}
	
	/**
	 * Will insert the int-encoding (four bytes) of i into the given array and return that same array.
	 * Notice that the given array will really be modified, and not copied.
	 * 
	 * @param i int to insert
	 * @param array to insert i into
	 * @param offset to insert i at
	 * @return the same array as the given one, that is now modified, for convenience
	 */
	public static byte[] toByteArray(int i, byte[] array, int offset) {
		array[offset+0] = (byte) (i);
		array[offset+1] = (byte) (i>>8);
		array[offset+2] = (byte) (i>>16);
		array[offset+3] = (byte) (i>>24);
		return array;
	}
	
	/**
	 * Equivalent to {@code byteArrayToInt(b, 0);}
	 * 
	 * @param b
	 * @return The int-decoding of the first four bytes in b
	 */
	public static int byteArrayToInt(byte[] b) {
		return byteArrayToInt(b, 0);
	}
	
	/**
	 * 
	 * @param b
	 * @param offset
	 * @return The int-decoding of the four bytes in b, from offset to offset+3
	 */
	public static int byteArrayToInt(byte[] b, int offset) {
		return  unsigned(b[offset+0])      +
				(unsigned(b[offset+1])<<8)  +
				(unsigned(b[offset+2])<<16) +
				(unsigned(b[offset+3])<<24);
	}
	
	
	
	
	
	private static int unsigned(byte b) {
		return (b+256)%256;
	}

	/**
	 * Equivalent to {@code toByteArray(l, new byte[4], 0);}
	 * 
	 * @param l
	 * @return The long-encoding of l as an array of eight bytes
	 */
	public static byte[] toByteArray(long l) {
		return toByteArray(l, new byte[8], 0);
	}
	
	/**
	 * Will insert the long-encoding (eight bytes) of l into the given array and return that same array.
	 * Notice that the given array will really be modified, and not copied.
	 * 
	 * @param l long to insert
	 * @param array to insert l into
	 * @param offset to insert l at
	 * @return the same array as the given one, that is now modified, for convenience
	 */
	public static byte[] toByteArray(long l, byte[] array, int offset) {
		toByteArray((int) l, 		array, offset);
		toByteArray((int) (l>>32),	array, offset+4);
		return array;
	}
	
	/**
	 * Equivalent to {@code byteArrayToLong(b, 0);}
	 * 
	 * @param b
	 * @return The long-decoding of the first eight bytes in b
	 */
	public static long byteArrayToLong(byte[] b) {
		return byteArrayToLong(b, 0);
	}
	
	/**
	 * @param b
	 * @param offset
	 * @return The long-decoding of the eight bytes in b, from offset to offset+7
	 */
	public static long byteArrayToLong(byte[] b, int offset) {
		return  unsigned(b[offset+0])      +
				(unsigned(b[offset+1])<<8)  +
				(unsigned(b[offset+2])<<16) +
				(unsigned(b[offset+3])<<24) +
				(unsigned(b[offset+4])<<32) +
				(unsigned(b[offset+5])<<40) +
				(unsigned(b[offset+6])<<48) +
				(unsigned(b[offset+7])<<56);
	}
}
