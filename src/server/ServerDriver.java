/** ServerClient.java
 * @author ronny
 * 
 * TODO: Document <br>
 */
package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import messenger.Messenger;
import messenger.ServerMessenger;

/** ServerClient <br>
 * @author ronny
 */
public class ServerDriver {
	
	public static void main(String[] args) throws SocketException {
		System.out.println("running server client...");
		DatagramSocket socket = new DatagramSocket();
		
		// parse the inventory file and start the server.
		System.out.println("Starting the inventory server...");
		Server server = new Server();
		server.init();
		server.start();
		
		byte[] data = null;
		ServerMessenger messenger = server.getMessenger();
		System.out.println("sclient $ ");
		try (InputStreamReader stream = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(stream);) {
			while (true) {
				String input = reader.readLine();
				String[] tokens = input.split(" ");
				
				// execute command
				if (input.startsWith("leader")) {
					System.out.println("[leader command]");
					messenger.startLeaderElection();
				} else if (input.startsWith("exit")) {
					System.out.println("[exit command]");
					System.exit(0);
				} else {
					System.out.println("unknown command...");
				}
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
	
}
