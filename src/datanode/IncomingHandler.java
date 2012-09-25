package datanode;

import java.io.IOException;
import common.Packet;
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
			Packet packet = backSocket.receivePacket();
			switch (packet.type) {
			case REQUEST_BLOCK_REPORT:
				backSocket.send(getBlockReportPacket());
				break;
			}
			System.out.println(backSocket.receiveString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		backSocket.closeWithoutException();
		System.out.printf("[%s] Finished serving client.\n", backSocket.getRemoteSocketAddress());
	}

	private Packet getBlockReportPacket() {
//		Set<BlockImage> blocks = getBlockReport();
//		new Packet(PacketType.BLOCK_REPORT, )
		return null;
	}

//	private Set<BlockImage> getBlockReport() {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
