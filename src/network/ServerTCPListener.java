package network;


import java.io.*;
import java.net.*;

import controller.Server;

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
		try ( ServerSocket socket = new ServerSocket(port); ) {
			while(true){ // listen for tcp clients
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
