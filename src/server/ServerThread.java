package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

import messenger.ServerMessenger;
import model.LamportClock;

/** ServerThread
 * Services a server request.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public class ServerThread extends Thread {
	
	private Server server;
	private Socket socket;
	private ServerMessenger messenger;
	private static ReentrantLock requestLock = new ReentrantLock(true);
	
	private DatagramPacket packet;
	private ConnectionMode mode;
	
	private enum ConnectionMode {
		TCP, UDP
	};
	
	// KeepAliveThread pinger
	private KeepAliveThread pinger;
	
	/** ServerThread <br>
	 * Constructs a new ServerThread Object. <br>
	 * Services a TCP Socket.
	 */
	public ServerThread(Server server, Socket socket) {
		this.server = server;
		this.socket = socket;
		this.messenger = server.getMessenger();
		this.mode = ConnectionMode.TCP;
	}
	
	/** ServerThread <br>
	 * Constructs a new ServerThread Object. <br>
	 * Services a UDP packet.
	 */
	public ServerThread(Server server, DatagramPacket packet) {
		this.server = server;
		this.packet = packet;
		this.messenger = server.getMessenger();
		this.mode = ConnectionMode.UDP;
	}
	
	// service TCP Socket
	public void run() {
		if (mode.equals(ConnectionMode.TCP)) {
			this.serviceTCP(); // service TCP Socket
		} else if (mode.equals(ConnectionMode.UDP)) {
			this.serviceUDP(); // service UDP Socket
		}
	}
	
	/** serviceTCP()
	 * 
	 * Connection-based tcp socket. <br>
	 * Writes to socket output.
	 */
	public void serviceTCP() {
		try (InputStreamReader istream = new InputStreamReader(socket.getInputStream());
			PrintWriter ostream = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader reader = new BufferedReader(istream);) {
			
			// continually service tcp connection.
			LamportClock timestamp = null;
			String command = "", response = "";
			while ((command = reader.readLine()) != null) {
				pinger = new KeepAliveThread(ostream); // keep alive thread
				ostream.println("ping"); // 100ms acknowledgement.
				System.out.println("TCP Service: " + command);
				
				// finished session.
				if (command.equals("exit")) {
					socket.close();
					break; // finished socket execution.
				}
				
				// DEBUG: rec ping.. ignore message.
				else if (command.equals("ping")) {
					System.out.println("Receieved a ping...");
				}
				
				// service intraserver request.
				else if (command.startsWith("request")) {
					timestamp = LamportClock.parseClock(command.split(" ", 2)[1]);
					messenger.receiveRequest(timestamp);
				}
				
				// service intraserver release.
				else if (command.startsWith("release")) {
					// execute the command before removing from queue.
					timestamp = LamportClock.parseClock(command.split(" ", 2)[1]);
					command = reader.readLine();
					execute(command);
					messenger.receiveRelease(timestamp);
				}
				
				// service intraserver acknowledgement.
				else if (command.startsWith("acknowledge")) {
					timestamp = LamportClock.parseClock(command.split(" ", 2)[1]);
					messenger.receiveAcknowledgement(timestamp);
				}
				
				// commands that require acknowledgement.
				else if (command.startsWith("purchase") || command.startsWith("cancel")) {
					pinger.start();
					requestLock.lock(); // request critical section
					messenger.request();
					response = execute(command);
					messenger.incrementClock();
					messenger.release(command);
					requestLock.unlock(); // release critical section
					pinger.kill();
					ostream.println(response);
					ostream.println("EOT");
				}
				
				// thread-safe commands.
				else { // execute server command. (list or search)
					response = execute(command);
					ostream.println(response);
					ostream.println("EOT");
					messenger.incrementClock();
				}
			}
		} catch (IOException err) {
			System.out.println("Error servicing TCP Client request. exiting...");
			err.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/** serviceUDP()
	  * 
	  * Connectionless UDP packets. <br>
	  * Sends a response packet.
	  * TODO: Update this function. It's OUT OF DATE!!!
	  */
	public void serviceUDP() {
		try (DatagramSocket socket = new DatagramSocket()) {
			String command = new String(packet.getData());
			System.out.println("UDP Service: " + command);
			
			// execute server command
			String response = execute(command);
			byte[] data = response.getBytes();
			int length = data.length;
			
			// send return packet with command response.
			DatagramPacket returnPacket = new DatagramPacket(data, length);
			returnPacket.setAddress(packet.getAddress());
			returnPacket.setPort(packet.getPort());
			socket.send(returnPacket);
			socket.close();
		} catch (IOException err) {
			System.err.println("Error servicing UDP request. exiting...");
			err.printStackTrace();
		}
	}
	
	/** execute()
	* 
	* Executes valid server command. <br>
	*/
	public String execute(String command) {
		String response = "";
		String[] tokens = command.trim().split("\\s+");
		try { // parse and execute
			String opcode = tokens[0].toLowerCase();
			if (opcode.equals("list")) {
				response = server.list();
			} else if (opcode.equals("purchase")) {
				String user = tokens[1];
				String product = tokens[2];
				Integer quantity = Integer.parseInt(tokens[3]);
				response = server.purchase(user, product, quantity);
			} else if (opcode.equals("cancel")) {
				Integer orderId = Integer.parseInt(tokens[1]);
				response = server.cancel(orderId);
			} else if (opcode.equals("search")) {
				String username = tokens[1];
				response = server.search(username);
			} else {
				response = "server command not supported: " + opcode;
			}
		} catch (Exception err) {
			System.err.println("invalid server command: " + command);
			response = "invalid server command: " + command;
			// err.printStackTrace();
		}
		
		// client response
		return response;
	}
}
