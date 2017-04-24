
package model;

/** AcceptorMessage
 * Acceptor messages for the paxos algorithm.
 * Contains message replies for an acceptor. accept and reject requests.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public class AcceptorMessage {
	
	// proposal number
	private LamportClock number;
	
	// server transaction
	private String command;
	
	// accept, choose, or reject
	private String type;
	
	/** AcceptorMessage() <br>
	 * 
	 * Constructs a new Acceptor Message. <br>
	 * proposes an accept .
	 * @param clock lamport timestamp functions as proposal number.
	 * @param command server transaction functions as proposal value.
	 */
	public AcceptorMessage(LamportClock clock, String command, String type) {
		this.number = clock;
		this.command = command;
		this.type = type;
	}
	
	/** AcceptorMessage() <br>
	 * 
	 * Constructs a new Proposal Message. <br>
	 * proposes a reject.
	 */
	public AcceptorMessage() {
		this.type = "reject";
	}

	
	public LamportClock getNumber() {
		return number;
	}
	
	public String getCommand() {
		return command;
	}
	
	public String getType() {
		return type;
	}
	
	
	@Override
	public String toString() {
		if(type.equals("reject")){
			return String.format("acceptor reject");
		} else if(type.equals("accept") || type.equals("choose")){
			return String.format("acceptor %s [%s] %s", type, command, number);			
		} else { // bad proposal message.
			return null;
		}
	}
	
}
