package server;

import java.io.*;
import java.net.*;

/** ServerTCPListener
 * Listens for incoming TCP requests from e-commerce clients.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public class ServerTCPListener extends Thread {
	
	private int port;
	private Server server;
	
	/** ServerTCPListener <br>
	 * 
	 * Constructs a new ServerTCPListener Object. <br>
	 */
	public ServerTCPListener(Server server, int port) {
		this.port = port;
		this.server = server;
	}
	
	public void run() {
		try (ServerSocket socket = new ServerSocket(port);) {
			while (true) { // listen for tcp clients
				Socket client = socket.accept();
				ServerThread worker = new ServerThread(server, client);
				worker.start();
			}
		} catch (IOException e) {
			System.out.println("Error listening on TCP Socket. exiting...");
			e.printStackTrace();
		}
	}
	
}
