package datanode;

import java.io.IOException;
import java.net.ServerSocket;

import common.YSocket;

public class IncomingForker implements Runnable {
	private ServerSocket incomingConnectionsSocket;

	public IncomingForker(int incomingPort) throws IOException {
		incomingConnectionsSocket = new ServerSocket(incomingPort);
	}

	@Override
	public void run() {
		while (true) {
			YSocket incomingSocket;
			try {
				incomingSocket = new YSocket(incomingConnectionsSocket.accept());
			} catch (IOException e) {
				System.err.println("Error starting to serve client.");
				e.printStackTrace();
				continue;
			}
			
			//TODO: check if the packet is valid and so that there aren't already too many connections from that IP. 
			(new Thread(new IncomingHandler(incomingSocket))).run();
		}
	}
}
