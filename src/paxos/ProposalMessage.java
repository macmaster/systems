
package paxos;

import model.LamportClock;

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
	 * @param proposalTimestamp lamport timestamp functions as proposal number.
	 * @param command server transaction functions as proposal value.
	 * @param accept true if the message is an "accept" request. "prepare" otherwise.
	 */
	public ProposalMessage(LamportClock clock, String command, boolean accept) {
		this.number = clock;
		this.command = command;
		this.type = accept ? "accept" : "prepare";
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
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	@Override
	public String toString() {
		return String.format("proposal %s %s %s", type, number, command);
	}
	
}
