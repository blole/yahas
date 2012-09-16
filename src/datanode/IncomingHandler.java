package datanode;

import java.io.IOException;

import common.YSocket;

public class IncomingHandler implements Runnable {
	private YSocket backSocket;

	public IncomingHandler(YSocket backSocket) {
		this.backSocket = backSocket;
	}

	@Override
	public void run() {
		System.out.printf("[%s] Starting to serve client.\n", backSocket.getRemoteSocketAddress());
		
		try {
			backSocket.send("hello");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			System.out.println(backSocket.receiveString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		backSocket.closeWithoutException();
		System.out.printf("[%s] Finished serving client.\n", backSocket.getRemoteSocketAddress());
	}
}
