package server;

import java.io.IOException;
import java.net.*;

/** ServerTCPListener
 * Listens for incoming UDP requests from e-commerce clients.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/21/2017
 */
public class ServerUDPListener extends Thread {
	
	private int port;
	private Server server;
	
	/** ServerUDPListener <br>
	 * 
	 * Constructs a new ServerUDPListener Object. <br>
	 */
	public ServerUDPListener(Server server, int port) {
		this.port = port;
		this.server = server;
	}
	
	public void run() {
		try (DatagramSocket socket = new DatagramSocket(port);) {
			final int length = 1024;
			while (true) { // listen for tcp clients
				byte[] data = new byte[length];
				DatagramPacket packet = new DatagramPacket(data, length);
				socket.receive(packet);
				ServerThread worker = new ServerThread(server, packet);
				worker.start();
			}
		} catch (IOException e) {
			System.out.println("Error listening on UDP Socket. exiting...");
			e.printStackTrace();
		}
	}
	
}
