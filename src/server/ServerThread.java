package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import messenger.ServerMessenger;
import model.LamportClock;
import model.ServerTag;

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
	private ServerMessenger messenger;
	private static ReentrantLock requestLock = new ReentrantLock(true);
	
	private Socket socket;
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
				
				// commands that require acknowledgement.
				else if (command.startsWith("purchase") || command.startsWith("cancel")) {
					pinger.start();
					requestLock.lock(); // request critical section
					while (!messenger.proposal(command)) {
						sleep(50* ThreadLocalRandom.current().nextInt(2, 5 + 1));
					}
					response = execute(command);
					messenger.incrementClock();
					messenger.receiveLearnedValue(command);
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
	  * Connection-less UDP packets. <br>
	  * Sends a response packet.
	  */
	public void serviceUDP() {
		String message = new String(packet.getData());
		message = messenger.parseMessage(message);
		Integer senderId = messenger.getSenderId();
		try (DatagramSocket socket = new DatagramSocket()) {
			System.out.println("UDP Service: " + message);
			
			// Parse ServerTag for reply.
			String returnAddress = packet.getAddress().getHostAddress();
			String tagString = String.format("%s:%d", returnAddress, packet.getPort());
			ServerTag tag = ServerTag.parse(tagString);
			
			// DEBUG: rec ping.. ignore message.
			if (message.equals("ping")) {
				System.out.println("Receieved a ping...");
			}
			
			// proposer message for prepare / accept
			if (message.startsWith("proposer")) {
				messenger.ping(tag);
				// System.out.format("recv proposer msg: [%s]%n", message);
				
				String command = null; // proposed command.
				LamportClock number = null; // proposal number
				if (message.startsWith("proposer prepare")) {
					String prepareRegex = "prepare (\\(\\d+, \\d+\\))";
					Pattern preparePattern = Pattern.compile(prepareRegex);
					Matcher matcher = preparePattern.matcher(message);
					matcher.find();
					number = LamportClock.parseClock(matcher.group(1));
					messenger.receiveProposerPrepare(senderId, number);
				} else if (message.contains("proposer accept")) {
					String acceptRegex = "accept \\[(.*?)\\] (\\(\\d+, \\d+\\))";
					Pattern acceptPattern = Pattern.compile(acceptRegex);
					Matcher matcher = acceptPattern.matcher(message);
					matcher.find();
					command = matcher.group(1);
					number = LamportClock.parseClock(matcher.group(2));
					messenger.receiveProposerAccept(senderId, number, command);
				}
			}
			
			// proposer message for prepare / accept
			if (message.startsWith("acceptor")) {
				messenger.ping(tag);				
				String command = null; // proposed command.
				LamportClock number = null; // proposal number
				if (message.startsWith("acceptor accept") || message.startsWith("acceptor choose")) {
					String acceptRegex = "\\[(.*?)\\] (\\(\\d+, \\d+\\)|null)";
					Pattern acceptPattern = Pattern.compile(acceptRegex);
					Matcher matcher = acceptPattern.matcher(message);
					matcher.find();
					command = matcher.group(1);
					command = command.equals("null") ? null : command;
					number = LamportClock.parseClock(matcher.group(2));
					if(message.startsWith("acceptor accept"))
						messenger.receiveAcceptorAccept(number, command);
					else if(message.startsWith("acceptor choose"))
						messenger.receiveAcceptorChoose(number, command);
				}  else if (message.startsWith("acceptor reject")) {
					messenger.receiveAcceptorReject();
				}
			}

			// message for learner
			if (message.startsWith("learn")) {
				messenger.ping(tag);
				String learnRegex = "learn \\[(.*?)\\]";
				Pattern learnPattern = Pattern.compile(learnRegex);
				Matcher matcher = learnPattern.matcher(message);
				matcher.find();
				String command = matcher.group(1);
				
				// fast-forward server.
				execute(command);
				messenger.receiveLearnedValue(command);
			}
			
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
