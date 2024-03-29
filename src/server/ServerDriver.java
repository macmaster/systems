/** ServerClient.java
 * @author ronny
 * 
 * TODO: Document <br>
 */
package server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketException;

import messenger.ServerMessenger;
import model.LamportClock;

/** ServerClient <br>
 * @author ronny
 */
public class ServerDriver {
	
	public static void main(String[] args) throws SocketException {
		
		// parse the inventory file and start the server.
		System.out.println("Starting the inventory server...");
		Server server = new Server();
		server.init();
		server.start();
		
		ServerMessenger messenger = server.getMessenger();
		try (InputStreamReader stream = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(stream);) {
			while (true) { // execute command
				System.out.print("sclient $ ");
				String input = reader.readLine();
				if (input.startsWith("prepare")) {
					System.out.println("[prepare command]");
					messenger.proposePrepare(new LamportClock(777, 777));
				} else if (input.startsWith("exit")) {
					System.out.println("[exit command]");
					System.exit(0);
				} else if (input.startsWith("proposal")) {
					System.out.println("[proposal command]");
					messenger.proposal(input.split(" ", 2)[1]);
				} else {
					System.out.println("unknown command...");
				}
			}
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
	
}
