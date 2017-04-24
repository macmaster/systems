package paxos;

import model.LamportClock;

/** Acceptor.java
 * Acceptor Strategy methods for a paxos process.
 * 
 * By: Gaurav Nagar, Hari Kosuru, 
 * Taylor Schmidt, and Ronald Macmaster.
 * UT-EIDs: gn3544, hk8633, trs2277,  rpm953
 * Date: 4/20/2017
 */
public interface Acceptor {

    void acceptorPrepare(LamportClock sequenceNumber);

    void acceptorAccept(LamportClock sequenceNumber, String request);
}
