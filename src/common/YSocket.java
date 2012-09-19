package common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import common.exceptions.InvalidPacketType;
import common.exceptions.UnexpectedPacketType;

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
	
	
	
	
	
	public String receiveString() throws IOException, InvalidPacketType, UnexpectedPacketType {
		Packet packet = receivePacket(PacketType.TEXT);
		return new String(packet.message);
	}
	
	public Packet receivePacket(PacketType expectedType) throws IOException, InvalidPacketType, UnexpectedPacketType {
		Packet packet = receivePacket();
		if (packet.type != expectedType)
			throw new UnexpectedPacketType("Unexpected "+PacketType.class+
					" received, expected "+expectedType+", got "+packet.type);
		else
			return packet;
	}
	
	public Packet receivePacket() throws IOException, InvalidPacketType {
		PacketType type = PacketType.decode(socket.getInputStream().read());
		int len = socket.getInputStream().read();
		byte[] message = new byte[len];
		for (int off=0; off<message.length; ) {	
			off += socket.getInputStream().read(message, off, len);
			len -= off;
		}
		
		return new Packet(type, message);
	}
	
	
	
	
	
	public void send(String message) throws IOException {
		send(new Packet(PacketType.TEXT, message.getBytes()));
	}

	/**
	 * TODO: Does not work on packets longer than 255 bytes...
	 * @param packet
	 * @throws IOException 
	 */
	public void send(Packet packet) throws IOException {
		socket.getOutputStream().write(packet.type.encode());
		socket.getOutputStream().write(packet.message.length);
		socket.getOutputStream().write(packet.message);
	}

	
	
	
	
	public void close() throws IOException {
		socket.close();
	}
	
	public void closeWithoutException() {
		try {
			socket.close();
		} catch (IOException e) {
			System.err.printf("[%s] Error closing socket: %s\n",
					getRemoteSocketAddress(), e.getLocalizedMessage());
		}
	}
	
	public SocketAddress getRemoteSocketAddress() {
		return socket.getRemoteSocketAddress();
	}
}
