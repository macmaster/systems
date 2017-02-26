/** ServerTCPListener
 * By: Ronald Macmaster and Taylor Schmidt
 * UT-EID: rpm953   and    trs2277
 * Date: 2/25/17
 * 
 * TCP Listener thread for product server. <br>
 * Opens new TCP server socket and listens <br>
 */

import java.io.*;
import java.net.*;

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
