package hw2.garden;

/** CyclicBarrier.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 2/13/17
 * 
 * A CyclicBarrier will trip when 
 * the given number of parties (threads) are waiting upon it 
 */

import java.util.concurrent.Semaphore;

public class CyclicBarrier {
	
	private final int parties;
	private int position;
	
	private Semaphore mutex;
	private Semaphore barrier, waiter;
	
	/** CyclicBarrier <br>
	 * 
	 * Constructs a new CyclicBarrier Object. <br>
	 */
	public CyclicBarrier(int parties) {
		// Creates a new CyclicBarrier that will trip when
		// the given number of parties (threads) are waiting upon it
		
		if (parties <= 0) { throw new IllegalArgumentException("parties must be > 0"); }
		
		this.parties = parties;
		this.position = 1;
		mutex = new Semaphore(1);
		barrier = new Semaphore(parties);
		waiter = new Semaphore(0);
	}
	
	int await() throws InterruptedException {
		// Waits until all parties have invoked await on this barrier.
		// If the current thread is not the last to arrive then it is
		// disabled for thread scheduling purposes and lies dormant until
		// the last thread arrives.
		// Returns: the arrival index of the current thread, where index
		// (parties - 1) indicates the first to arrive and zero indicates
		// the last to arrive.
		
		// get barrier rank
		barrier.acquire();
		mutex.acquire();
		int rank = parties - position;
		position = position + 1;
		mutex.release();
		
		// wait for others
		if (rank > 0) {
			waiter.acquire();
		}
		
		// last thread to leave?
		if (!waiter.hasQueuedThreads()) {
			// continue and reset
			position = 1;
			barrier.release(parties);
			return rank;
		} else {
			waiter.release();
			return rank;
		}
		
	}
	
}
