package messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import model.AcceptorMessage;
import model.LamportClock;
import model.ProposalMessage;
import model.ServerTag;
import server.Server;
import server.ServerTCPListener;
import server.ServerUDPListener;

/** ServerMessenger
 * Contains communication methods for the server.
 * Manages the server-side Lookup Table.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public class ServerMessenger extends Messenger {
	
	// specific server handle.
	private Server server;
	private Integer serverId;
	private ServerTag serverTag;
	
	// server-server communication
	private DatagramSocket socket; // outgoing port
	
	// leader election
	private Integer leader;
	
	// Paxos Algorithm
	private Integer senderId = -1; // set as a return handle when msgs are parsed.
	
	// proposer
	private Integer numAccepts = 0, numRejects = 0; // number of acks a proposer tracks while it waits.
	private LamportClock proposedNumber = null; // last accepted proposal number.
	private String proposedCommand = null; // last accepted command tied to the last accepted proposal number.
	
	// acceptor
	private LamportClock promisedNumber = new LamportClock(0, 0); // acceptor promises to reject below to this proposal.
	private LamportClock acceptedNumber = null; // last accepted proposal number.
	private String acceptedCommand = null; // last accepted command tied to the last accepted proposal number.
	
	// Lamport's Algorithm
	private Integer numAcks = 0;
	private LamportClock timestamp;
	private PriorityQueue<LamportClock> queue;
	
	/** ServerMessenger
	 * 
	 * Constructs a new ServerMessenger object. <br>
	 */
	public ServerMessenger(Server server) {
		this.server = server;
		this.queue = new PriorityQueue<LamportClock>();
	}
	
	/**
	 * start()
	 * 
	 * start the server listener <br>
	 */
	public void start() {
		try { // start server port listeners
			this.timestamp = new LamportClock(serverId);
			this.serverTag = tags.get(serverId); // set my server tag.
			this.socket = new DatagramSocket(); // personal backchannel socket.
			new ServerTCPListener(server, serverTag.getPort()).start();
			new ServerUDPListener(server, serverTag.getUDPPort()).start();
			System.out.format("Server %d: now listening on (tcp, udp) ports (%d, %d)%n", serverId, serverTag.getPort(), serverTag.getUDPPort());
		} catch (SocketException e) {
			System.err.println("Could not start the server messenger. Exiting...");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	// parse server metadata.
	@Override // <serverId> <numServers> <filename>
	protected boolean parseMetadata(String metadata) {
		try {
			String[] tokens = metadata.split("\\s+");
			this.serverId = Integer.parseInt(tokens[0]);
			this.numServers = Integer.parseInt(tokens[1]);
			server.filename = tokens[2]; // inventory path
			if ((numServers <= 0) || (serverId < 1) || (serverId > numServers)) {
				System.err.println("Bad metadata values. make sure numServers > 0.");
				return false;
			} else {
				return true;
			}
		} catch (Exception err) {
			System.err.println("Could not parse server metadata.");
			System.err.println("usage: " + getMetadataFormat());
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see Messenger#getMetadataFormat()
	 */
	@Override
	protected String getMetadataFormat() {
		return "<serverId> <numServers> <inventory_path>";
	}
	
	/** incrementClock()
	 * 
	 * signals that an event has occurred. <br>
	 * updates the lamport clock. <br>
	 */
	public synchronized void incrementClock() {
		this.timestamp.increment();
	}
	
	/** link to this specific server */
	public Integer getServerId() {
		return serverId;
	}
	
	/** return link to the last server to send a msg */
	public Integer getSenderId() {
		return senderId;
	}
	
	/**
	 * UDP pings a server. acts as an acknowledgment.
	 */
	public void ping(ServerTag tag) throws IOException {
		String msg = String.format("%s : %s", this.timestamp, "ping"); // ping message.
		DatagramPacket sendPacket = new DatagramPacket(msg.getBytes(), msg.length());
		sendPacket.setAddress(tag.getAddress());
		sendPacket.setPort(tag.getPort());
		socket.send(sendPacket);
		incrementClock();
	}
	
	/**
	 * Send a UDP message to a server.
	 * messages are tagged with "(pi, ti) : message"
	 */
	private void sendMessage(Integer serverId, String message) throws IOException {
		ServerTag serverTag = getServerTag(serverId);
		socket.setSoTimeout(100); // send a datagram
		String string = String.format("%s : %s", this.timestamp, message);
		DatagramPacket sendPacket = new DatagramPacket(string.getBytes(), string.length());
		sendPacket.setAddress(serverTag.getAddress());
		sendPacket.setPort(serverTag.getUDPPort());
		System.out.format("Sending %s to %s : %d%n", message, serverTag.getAddress().getHostAddress(), serverTag.getUDPPort()); // debug
		socket.send(sendPacket);
		incrementClock();
	}
	
	/**
	 * Receive a UDP message from a server. 100ms timeout.
	 * message of form: "(pi, ti) : message"
	 * @throws IOException 
	 */
	private String receiveMessage() throws IOException {
		// receive the leader acknowledgement.
		byte[] buffer = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
		socket.receive(receivePacket);
		return parseMessage(new String(buffer));
	}
	
	/** 
	 * Parses a server-server message.  <br>
	 * The lamport clock is striped and updated, and the msg is returned.
	 */
	public String parseMessage(String message) {
		// update my timestamp and return msg.
		String[] tokens = message.split(" : ", 2);
		LamportClock clock = LamportClock.parseClock(tokens[0]);
		Integer myts = this.timestamp.getTimestamp();
		Integer otherts = clock.getTimestamp();
		this.timestamp.setTimestamp(Math.max(myts, otherts) + 1);
		this.senderId = clock.getProcessId(); // return link
		return (tokens[1]).trim();
	}
	
	/******************* Paxos Algorithm Methods *************************/
	
	public synchronized boolean proposal(String command) throws InterruptedException {
		LamportClock number = this.timestamp.copy(); // proposal number
		boolean original = false; // executed the originally proposed command.
		proposePrepare(number);
		while ((numAccepts + numRejects) < numServers) {
			wait();
		}
		if (numAccepts >= ((numServers / 2) + 1)) {
			if (proposedNumber == null || proposedCommand == null) {
				this.proposedNumber = number;
				this.proposedCommand = command;
				original = true;
			}
		} else { // rejected prepare.
			System.out.println("Proposal prepare was rejected!");
			return false;
		}
		System.out.format("Executing proposal %s: [%s]. original? %s%n", proposedNumber, proposedCommand, original ? "yes" : "no");
		numRejects = numAccepts = 0;
		this.proposedNumber = null;
		this.proposedCommand = null;
		return original;
	}
	
	/**
	 * Phase 1, prepare the proposal. 
	 * Send the proposal number (lamport timestamp) to all the acceptors.
	 * Acceptor quorum must consist of a majority.
	 */
	public synchronized void proposePrepare(LamportClock clock) {
		// send to all other servers receiving ports
		LamportClock number = clock.copy();
		List<Integer> downedServers = new ArrayList<Integer>();
		for (Integer id : tags.keySet()) {
			try { // catch faulty servers.
				sendMessage(id, new ProposalMessage(number).toString());
				String ping = receiveMessage();
				// System.out.format("%s from %d.%n", ping, id);
			} catch (IOException e) {
				System.err.println("could not establish socket for server " + id);
				downedServers.add(id); // remove inactive server tag.
				numServers = numServers - 1;
				notifyAll();
			}
		}
		
		// remove faulty servers.
		for (Integer id : downedServers) {
			tags.remove(id);
		}
	}
	
	/**
	 * Phase 1, receive a prepare
	 * If the proposal number is less than promised value, reject it!
	 * Otherwise, promise it to accept only above the proposal number.
	 * Send the most recent acceptedNumber and acceptedCommand this round. null otherwise. 
	 * @throws IOException 
	 */
	public synchronized void receiveProposerPrepare(Integer senderId, LamportClock clock) {
		try { // catch faulty servers.
			if (clock.compareTo(promisedNumber) > 0) { // accept the prepare proposal.
				promisedNumber.setClock(clock);
				System.out.println("promised number: " + clock);
				sendMessage(senderId, new AcceptorMessage(acceptedNumber, acceptedCommand).toString());
			} else { // reject the proposal
				sendMessage(senderId, new AcceptorMessage().toString());
			}
		} catch (IOException e) {
			System.err.println("could not establish socket for server " + senderId);
			tags.remove(senderId); // remove inactive server tag.
			numServers = numServers - 1;
			notifyAll();
		}
	}
	
	public synchronized void receiveAcceptorReject() {
		System.out.format("recved acceptor reject!%n");
		numRejects += 1;
		notifyAll();
	}
	
	public synchronized void receiveAcceptorAccept(LamportClock number, String command) {
		// System.out.format("recved acceptor prepare: %s [%s]%n", number, command);
		if (number != null && command != null) {
			if (number.compareTo(proposedNumber) > 1) {
				proposedNumber = number.copy();
				proposedCommand = command;
			}
		}
		
		// acknowledge proposal
		numAccepts += 1;
		notifyAll();
	}
	
	/**
	 * Phase 2, propose a value. 
	 * Send the proposal number (lamport timestamp) to all the acceptors.
	 * Acceptor quorum must consist of a majority.
	 */
	public synchronized void proposeAccept(LamportClock clock) {
	}
	
	/*********************** Hari's Paxos ************************************************/
	//Prepare is ok if receivedSN is not null
	//Else prepare sends a rejection
	public void prepare(LamportClock receivedSN, LamportClock highestSN, String value){
		try {
			ServerTag serverTag = getServerTag(serverId);
			socket.setSoTimeout(100); // send a datagram
			socket.connect(serverTag.getAddress(), serverTag.getUDPPort());
			String command;

			if(receivedSN != null)
				command = String.format("acceptor prepare %s %s %s", receivedSN, highestSN, value);
			else
				command = "acceptor prepare reject";

			DatagramPacket sendPacket = new DatagramPacket(command.getBytes(), command.length());
			sendPacket.setAddress(serverTag.getAddress());
			sendPacket.setPort(serverTag.getPort());
			System.out.format("Sending %s to %s : %d%n", command, serverTag.getAddress().getHostAddress(), serverTag.getUDPPort()); // debug
			socket.send(sendPacket);
			incrementClock();
		} catch (IOException e) {
			System.err.println("Acceptor could not establish socket with leader ");
			e.printStackTrace();
		}
	}

	public void accept(LamportClock receivedSN){
		try {
			ServerTag serverTag = getServerTag(serverId);
			socket.setSoTimeout(100); // send a datagram
			socket.connect(serverTag.getAddress(), serverTag.getUDPPort());
			String command;

			if(receivedSN != null)
				command = String.format("acceptor accept %s", receivedSN);
			else
				command = "acceptor accept reject";

			DatagramPacket sendPacket = new DatagramPacket(command.getBytes(), command.length());
			sendPacket.setAddress(serverTag.getAddress());
			sendPacket.setPort(serverTag.getPort());
			System.out.format("Sending %s to %s : %d%n", command, serverTag.getAddress().getHostAddress(), serverTag.getUDPPort()); // debug
			socket.send(sendPacket);
			incrementClock();
		} catch (IOException e) {
			System.err.println("Acceptor could not establish socket with leader ");
			e.printStackTrace();
		}
	}
	
	/******************* Lamport's Clock Methods *************************/
	
	public synchronized void request() throws InterruptedException {
		// time that request is made.
		queue.add(this.timestamp);
		
		// create server channels.
		// send timestamp and request to all servers.
		List<Integer> downedServers = new ArrayList<Integer>();
		for (Integer id : tags.keySet()) {
			try { // create a socket channel
				if (id != serverId) {
					ServerTag tag = tags.get(id);
					Socket socket = new Socket();
					socket.connect(new InetSocketAddress(tag.getAddress(), tag.getPort()), 100);
					socket.setSoTimeout(100); // 100ms socket timeouts.
					
					// send request string with clock.
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
					writer.format("request %s%n", timestamp.toString());
					writer.println("exit");
					
					// message acknowledgement
					this.timestamp.increment();
					if (reader.readLine() == null) { // 100ms timeout.
						socket.close();
						throw new SocketTimeoutException();
					}
					writer.close();
					reader.close();
					socket.close();
				}
			} catch (IOException err) {
				System.err.println("could not establish socket for server " + id);
				downedServers.add(id); // remove inactive server tag.
				numServers = numServers - 1;
			}
		}
		for (Integer id : downedServers) {
			tags.remove(id);
		}
		
		// wait for acknowledgements.
		while ((numAcks < numServers - 1) || !(timestamp.equals(queue.peek()))) {
			wait();
		}
		
		// enter the critical section.
		numAcks = 0;
	}
	
	public synchronized void receiveRequest(LamportClock timestamp) {
		// On receive(request, (ts, j))) from Pj :
		Integer myts = this.timestamp.getTimestamp();
		Integer otherts = timestamp.getTimestamp();
		this.timestamp.setTimestamp(Math.max(myts, otherts) + 1);
		queue.add(timestamp);
		
		ServerTag tag = tags.get(timestamp.getProcessId());
		try (Socket socket = new Socket();) {
			socket.connect(new InetSocketAddress(tag.getAddress(), tag.getPort()), 100);
			socket.setSoTimeout(100); // 100ms socket timeouts.
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
			writer.format("acknowledge %s%n", this.timestamp.toString());
			writer.println("exit");
			this.timestamp.increment();
		} catch (IOException e) {
			System.err.println("Could not acknowledge the request. server is down.");
			queue.remove(timestamp);
			e.printStackTrace();
		}
	}
	
	public synchronized void receiveAcknowledgement(LamportClock timestamp) {
		// update my timestamp
		Integer myts = this.timestamp.getTimestamp();
		Integer otherts = timestamp.getTimestamp();
		this.timestamp.setTimestamp(Math.max(myts, otherts) + 1);
		
		// increment acks
		numAcks += 1;
		notifyAll();
	}
	
	public synchronized void receiveRelease(LamportClock timestamp) {
		// update my timestamp
		Integer myts = this.timestamp.getTimestamp();
		Integer otherts = timestamp.getTimestamp();
		this.timestamp.setTimestamp(Math.max(myts, otherts) + 1);
		
		// remove the request from the queue.
		timestamp = queue.remove();
		notifyAll();
	}
	
	public synchronized void release(String command) {
		// create server channels.
		// signal timestamped release to other servers.
		for (Integer id : tags.keySet()) {
			if (id != serverId) {
				// create a socket channel
				ServerTag tag = tags.get(id);
				try (Socket socket = new Socket(tag.getAddress(), tag.getPort());
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);) {
					
					// send request string with clock.
					writer.format("release %s%n", this.timestamp.toString());
					writer.println(command);
					writer.println("exit");
					
					// message acknowledgement
					this.timestamp.increment();
				} catch (IOException err) {
					System.err.format("server %d did not receive the release message", id);
				}
			}
		}
	}
	
	/**
	 * Leader election function.
	 * upon awakening, propose leader to other servers.
	 * synchronously wait for replies of all other servers.
	 */
	public synchronized void electLeader(LamportClock timestamp, Integer leaderId) {
		// update my timestamp
		Integer myts = this.timestamp.getTimestamp();
		Integer otherts = timestamp.getTimestamp();
		this.timestamp.setTimestamp(Math.max(myts, otherts) + 1);
		
		// get sender info
		Integer senderId = timestamp.getProcessId();
		
		// when leader election starts.
		leader = Math.max(serverId, leaderId);
		
		// message the leader to the others.
		List<Integer> downedServers = new ArrayList<Integer>();
		for (Integer id : tags.keySet()) {
			if (id != serverId && id != senderId) {
				try { // catch faulty servers.
					messageLeader(id);
				} catch (IOException e) {
					System.err.println("could not establish socket for server " + id);
					downedServers.add(id); // remove inactive server tag.
					numServers = numServers - 1;
					e.printStackTrace();
				}
			}
		}
		
		// remove faulty servers.
		for (Integer id : downedServers) {
			tags.remove(id);
		}
	}
	
	private void messageLeader(Integer serverId) throws IOException {
		// send out the leader.
		ServerTag serverTag = getServerTag(serverId);
		socket.setSoTimeout(100); // send a datagram
		String command = String.format("leader %d %s", leader, timestamp);
		DatagramPacket sendPacket = new DatagramPacket(command.getBytes(), command.length());
		sendPacket.setAddress(serverTag.getAddress());
		sendPacket.setPort(serverTag.getUDPPort());
		System.out.format("Sending %s to %s : %d%n", command, serverTag.getAddress().getHostAddress(), serverTag.getUDPPort()); // debug
		socket.send(sendPacket);
		incrementClock();
		
		// receive the leader acknowledgement.
		byte[] buffer = new byte[1024];
		DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
		socket.receive(receivePacket);
		command = new String(buffer);
		String tokens[] = command.split(" ", 3);
		
		// update my leader.
		Integer leaderId = Integer.parseInt(tokens[1]);
		leader = Math.max(leaderId, leader);
		
		// update my timestamp
		Integer myts = this.timestamp.getTimestamp();
		Integer otherts = LamportClock.parseClock(tokens[2]).getTimestamp();
		this.timestamp.setTimestamp(Math.max(myts, otherts) + 1);
	}

}
