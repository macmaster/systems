
package model;

/** ProposalMessage
 * Proposal messages for the paxos algorithm.
 * Contains message data for a proposal. prepare and accept requests.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public class ProposalMessage {
	
	// proposal number
	private LamportClock number;
	
	// server transaction
	private String command;
	
	// prepare or accept
	private String type;
	
	/** ProposalMessage() <br>
	 * 
	 * Constructs a new Proposal Message. <br>
	 * proposes an accept .
	 * @param proposalTimestamp lamport timestamp functions as proposal number.
	 * @param command server transaction functions as proposal value.
	 */
	public ProposalMessage(LamportClock clock, String command) {
		this.number = clock;
		this.command = command;
		this.type = "accept";
	}
	
	/** ProposalMessage() <br>
	 * 
	 * Constructs a new Proposal Message. <br>
	 * proposes a prepare.
	 * @param proposalTimestamp lamport timestamp functions as proposal number.
	 * @param command server transaction functions as proposal value.
	 */
	public ProposalMessage(LamportClock clock) {
		this.number = clock;
		this.type = "prepare";
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
		if(type.equals("prepare")){
			return String.format("proposer prepare %s", number);
		} else if(type.equals("accept")){
			return String.format("proposer accept %s %s", command, number);			
		} else { // bad proposal message.
			return null;
		}
	}
	
}
