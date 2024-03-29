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

import model.*;
import server.Server;
import server.ServerTCPListener;
import server.ServerUDPListener;

/**
 * ServerMessenger
 * Contains communication methods for the server.
 * Manages the server-side Lookup Table.
 * <p>
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
	
	private boolean decided = false;
	
	// Lamport's Algorithm
	private Integer numAcks = 0;
	private LamportClock timestamp;
	private PriorityQueue<LamportClock> queue;
	
	/**
	 * ServerMessenger
	 * <p>
	 * Constructs a new ServerMessenger object. <br>
	 */
	public ServerMessenger(Server server) {
		this.server = server;
		this.queue = new PriorityQueue<LamportClock>();
	}
	
	/**
	 * start()
	 * <p>
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
	
	/**
	 * incrementClock()
	 * <p>
	 * signals that an event has occurred. <br>
	 * updates the lamport clock. <br>
	 */
	public synchronized void incrementClock() {
		this.timestamp.increment();
	}
	
	/**
	 * link to this specific server
	 */
	public Integer getServerId() {
		return serverId;
	}
	
	/**
	 * return link to the last server to send a msg
	 */
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
	 *
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
	
	/** 
	 * Called each time before a command is executed. <br>
	 * Starts a paxos round. If the proposal fails to propose its own command, retries next round.
	 * 1) phase 1: propose a preperation.
	 * 2) phase 2: propose an accept. (your value or someone else's value).
	 * 3) value accepted, broadcast learner messages.
	 */
	public synchronized boolean proposal(String command) throws InterruptedException {
		LamportClock number = this.timestamp.copy(); // proposal number
		boolean original = false; // executed the originally proposed command.
		
		// clear quorum for next phase.
		numRejects = numAccepts = 0;
		this.proposedNumber = null;
		this.proposedCommand = null;
		
		// phase 1: propose a preperation.
		proposePrepare(number);
		while ((numAccepts + numRejects) < numServers) {
			wait();
		}
		
		// select proposal value.
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
		
		// clear quorum for next phase.
		numRejects = numAccepts = 0;
		
		// phase 2: propose an accept.
		sendAcceptorAccept(proposedNumber, proposedCommand);
		while ((numAccepts + numRejects) < numServers) {
			wait();
		}
		
		// the accept proposal was rejected.
		if (numAccepts < ((numServers / 2) + 1)) {
			System.out.println("Proposal accept was rejected!");
			return false;
		}
		
		// value accepted. notify the learners (NOTE: shouldn't we be printing out 'acceptedCommand'?)
		System.out.format("Executing proposal %s: [%s]. original? %s%n", proposedNumber, proposedCommand, original ? "yes" : "no");
		
		// send to all other but yourself. 
		sendLearnedValue(proposedNumber, proposedCommand);
		
		// clear quorum for next phase.
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
	public synchronized void proposePrepare(LamportClock number) {
		// send to all other servers receiving ports
		List<Integer> downedServers = new ArrayList<Integer>();
		for (Integer id : tags.keySet()) {
			System.out.println("DEBUG: Leader sending proposal to server " + id);
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
	 *
	 * @throws IOException
	 */
	public synchronized void receiveProposerPrepare(Integer senderId, LamportClock number) {
		System.out.println("DEBUG: Acceptor receiving proposal from server " + senderId);
		try { // catch faulty servers.
			if (number.compareTo(promisedNumber) > 0) { // accept the prepare proposal.
				promisedNumber = number.copy();
				System.out.println("promised number: " + number);
				sendMessage(senderId, new AcceptorMessage(acceptedNumber, acceptedCommand, "accept").toString());
				String ping = receiveMessage();
			} else { // reject the proposal
				sendMessage(senderId, new AcceptorMessage().toString());
				String ping = receiveMessage();
			}
			
		} catch (IOException e) {
			System.err.println("could not establish socket for server " + senderId);
			tags.remove(senderId); // remove inactive server tag.
			numServers = numServers - 1;
			notifyAll();
		}
	}
	
	/** 
	 * Proposal learns its prepare OR accept proposal was rejected. <br>
	 */
	public synchronized void receiveAcceptorReject() {
		System.out.format("received acceptor reject!%n");
		numRejects += 1;
		notifyAll();
	}
	
	/** 
	 * Proposer learns its prepare proposal was accepted. <br>
	 */
	public synchronized void receiveAcceptorAccept(LamportClock number, String command) {
		System.out.format("received acceptor prepare: %s [%s]%n", number, command);
		if (number != null && command != null) {
			if (number.compareTo(proposedNumber) > 0) {
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
	public synchronized void sendAcceptorAccept(LamportClock number, String command) {
		List<Integer> downedServers = new ArrayList<Integer>();
		for (Integer id : tags.keySet()) {
			try { // catch faulty servers.
				sendMessage(id, new ProposalMessage(number, command).toString());
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
	 * Phase 2, Acceptor receives a proposal for an accept. <br>
	 * [logic]
	 */
	public synchronized void receiveProposerAccept(Integer senderId, LamportClock number, String command) {
		try {// catch faulty servers.
			if (number.compareTo(promisedNumber) >= 0) {
				promisedNumber = number.copy();
				acceptedNumber = number.copy();
				acceptedCommand = command;
				AcceptorMessage message = new AcceptorMessage(acceptedNumber, acceptedCommand, "choose");
				sendMessage(senderId, message.toString());
				String ping = receiveMessage();
			} else { // reject the proposal
				sendMessage(senderId, new AcceptorMessage().toString());
				String ping = receiveMessage();
			}
		} catch (IOException e) {
			System.err.println("could not establish socket for server " + senderId);
			tags.remove(senderId); // remove inactive server tag.
			numServers = numServers - 1;
			notifyAll();
		}
	}
	
	/** 
	 * Proposer learns that the acceptor has chosen its value. <br>
	 */
	
	public synchronized void receiveAcceptorChoose(LamportClock number, String command) {
		numAccepts += 1;
		notifyAll();
	}
	
	/** 
	 * Learner learns the value that was chosen.  <br>
	 * Execute the command and advance the Paxos round.
	 */
	public synchronized void sendLearnedValue(LamportClock number, String command) {
		// send final command for execution to all but myself
		List<Integer> downedServers = new ArrayList<Integer>();
		for (Integer id : tags.keySet()) {
			if (id != serverId) {
				try { // catch faulty servers.
					sendMessage(id, new LearnerMessage(command).toString());
					String ping = receiveMessage();
				} catch (IOException e) {
					System.err.println("could not establish socket for server " + id);
					downedServers.add(id); // remove inactive server tag.
					numServers = numServers - 1;
					notifyAll();
				}
			}
		}
		
		// remove faulty servers.
		for (Integer id : downedServers) {
			tags.remove(id);
		}
	}
	
	public synchronized void receiveLearnedValue(String command) {
		System.out.format("Learned that the value was: [%s]%n", command);
		this.acceptedCommand = null;
		this.acceptedNumber = null;
	}
}
