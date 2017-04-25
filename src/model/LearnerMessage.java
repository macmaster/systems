
package model;

/** LearnerMessage
 * Learner messages for the paxos algorithm.
 * Contains message data for learning a value.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public class LearnerMessage {
	
	// server transaction
	private String command;
	
	/** LearnerMessage() <br>
	 * 
	 * Constructs a new Learner Message. <br>
	 * @param command server transaction functions as proposal value.
	 */
	public LearnerMessage(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}
	
	@Override
	public String toString() {
		return String.format("learn [%s]", command);
	}
	
}
