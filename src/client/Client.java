package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import common.YSocket;

import datanode.IncomingHandler;

public class Client {
	private static final int DEFAULT_PORT = 1618;
	//private static final DataNodeList
	
	public static void main(String args[]) {
		int serverPort = DEFAULT_PORT;
		
		if (args.length < 2)
			System.out.println("no port specified, assuming default port: "+DEFAULT_PORT);
		else {
			try {
				serverPort = Integer.valueOf(args[1]);
			}
			catch (NumberFormatException e) {
				System.out.println("malformated port specification, exiting");
				System.exit(1);
			}
		}
		
		InetSocketAddress dataNodeAddress = new InetSocketAddress("localhost", serverPort);
		YSocket outSocket = null;
		
		try {
			outSocket = new YSocket(dataNodeAddress);
		} catch (IOException e) {
			System.err.printf("Unable to connect to server: %s\n", e.getLocalizedMessage());
			System.exit(1);
		}
		
		
		try {
			outSocket.send("hello, this is the client.");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			outSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
