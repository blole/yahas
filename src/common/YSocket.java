package common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class YSocket {
	private Socket socket;

	public YSocket(Socket socket) {
		this.socket = socket;
	}
	
	public YSocket(InetSocketAddress socketAddress) throws IOException {
		this(socketAddress.getAddress(), socketAddress.getPort());
	}

	public YSocket(InetAddress address, int port) throws IOException {
		this.socket = new Socket(address, port);
	}

	public String receiveString() throws IOException {
		int len = socket.getInputStream().read();
		byte[] message = new byte[len];
		for (int off=0; off<message.length; ) {	
			off += socket.getInputStream().read(message, off, len);
			len -= off;
		}
		
		return new String(message);
	}
	
	public void send(String message) throws IOException {
		byte[] a = message.getBytes();
		socket.getOutputStream().write(a.length);
		socket.getOutputStream().write(a);
	}

	public SocketAddress getRemoteSocketAddress() {
		return socket.getRemoteSocketAddress();
	}
	
	public void closeWithoutException() {
		try {
			socket.close();
		} catch (IOException e) {
			System.err.printf("[%s] Error closing socket: %s\n",
					getRemoteSocketAddress(), e.getLocalizedMessage());
		}
	}
	
	public void close() throws IOException {
		socket.close();
	}
}
