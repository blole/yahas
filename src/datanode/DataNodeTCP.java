package datanode;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.HashMap;

public class DataNodeTCP {
	private static final int DEFAULT_PORT = 1618;
	
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		
		if (args.length >= 2) {
			try {
				port = Integer.valueOf(args[1]);
			}
			catch (NumberFormatException e) {
				System.out.println("malformated port specification, exiting");
				System.exit(1);
			}
		}
		
		
		DatagramSocket listeningSocket = null;
		
		try {
			listeningSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			System.err.printf("Could not bind to port %d: %s\n", port, e.getLocalizedMessage());
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.printf("Server started listening on port %d\n", port);
		
		while (true) {
			try {
				byte[] recieveData = new byte[1024];
				DatagramPacket initiatePacket = new DatagramPacket(recieveData, recieveData.length);
				
				listeningSocket.receive(initiatePacket);
				//TODO: check if the packet is valid and so that there aren't already too many connections from that IP. 
				
				DatagramSocket backSocket = new DatagramSocket();
				backSocket.connect(initiatePacket.getSocketAddress());
				System.out.printf("Starting to serve client %s\n", backSocket.getRemoteSocketAddress());
				(new Thread(new IncomingConnectionHandler(initiatePacket, backSocket))).run();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
