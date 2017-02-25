import java.io.IOException;
import java.net.ServerSocket;

/** ServerTCPListener
 * By: Ronald Macmaster and Taylor Schmidt
 * UT-EID: rpm953   and    trs2277
 * Date: 2/25/17
 * 
 * TCP Listener thread for product server. <br>
 * Opens new TCP server socket and listens <br>
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
		try ( ServerSocket socket = new ServerSocket(port); ) {
			socket.accept();
			
			
		} catch (IOException e) {
			System.out.println("Error listening on TCP Socket. exiting...");
			e.printStackTrace();
		}
	}
	
}
