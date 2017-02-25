import java.io.IOException;
import java.net.*;

/** ServerUDPListener
 * By: Ronald Macmaster and Taylor Schmidt
 * UT-EID: rpm953   and    trs2277
 * Date: 2/25/17
 * 
 * UDP Listener thread for product server. <br>
 * Opens new UDP server socket and listens <br>
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
		try ( DatagramSocket socket = new ServerSocket(port); ) {
			while(true){ // listen for tcp  clients
			    Datagram packet = 
			    ServerThread worker = new ServerThread(server, client);
			    worker.start();
			}
		} catch (IOException e) {
			System.out.println("Error listening on TCP Socket. exiting...");
			e.printStackTrace();
		}
	}
	
}
