package messenger;

import java.io.*;
import java.net.*;
import java.util.*;

import model.LamportClock;
import model.ServerTag;
import server.Server;
import server.ServerTCPListener;

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
	
	//leader election
	private Integer leader;
	private Boolean isFirstLeaderElection;
	private Integer numLeaderProposals;
	
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
		// server port listener
		this.timestamp = new LamportClock(serverId);
		this.serverTag = tags.get(serverId); // set my server tag.
		new ServerTCPListener(server, serverTag.getPort()).start();
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
	public synchronized void electLeader(ServerTag tag, LamportClock timestamp, Integer leaderId){
	    //update my timestamp
	    Integer myts = this.timestamp.getTimestamp();
	    Integer otherts = timestamp.getTimestamp();
	    this.timestamp.setTimestamp(Math.max(myts, otherts) + 1);
	    
	    //get sender info
	    Integer senderId = timestamp.getProcessId();
	    ServerTag senderTag = getServerTag(senderId);
	    
	    //check if leader process already started
	    if (isFirstLeaderElection) {
	        numLeaderProposals = 1;
	        leader = Math.max(serverId, leaderId);
	        isFirstLeaderElection = false;
	    } else {
	        leader = Math.max(leader, leaderId);
	    }
	    
	    //check if server received leader proposal from all others
	    numLeaderProposals++;
	    if (numLeaderProposals == numServers - 1) {
	        isFirstLeaderElection = true; //for next leader election cycle
	    }
	    
	    //send back to sending port of sender server
	    
	    
	    
	    //send to all other servers receiving ports
	    List<Integer> downedServers = new ArrayList<Integer>();
        for (Integer id : tags.keySet()) {
            try { // create a socket channel
                if (id != serverId && id != senderId) {
                    ServerTag sender = tags.get(id);
                    Socket socket = new Socket();
                    socket.connect(new InetSocketAddress(tag.getAddress(), tag.getPort() + 100), 100);
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
        
	}
	
	/**
	 * Initiate leader election. 
	 * notify all servers to start proposing leaders
	 */
	public synchronized void startLeaderElection(){
	    //send default serverId
	    leader = serverId;
	    isFirstLeaderElection = true;
	    numLeaderProposals = 0;
	    
	    for (Integer id: tags.keySet()) {
	        if (id != serverId) {
	            
	        }
	    }
	    
	}
	
	/** incrementClock()
	 * 
	 * signals that an event has occurred. <br>
	 * updates the lamport clock. <br>
	 */
	public synchronized void incrementClock() {
		this.timestamp.increment();
	}
	
}
