/** FairReadWriteLock.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 2/13/17
 * 
 * A FairReadWriteLock Synchronizes readers and writers using monitors.
 */

/**
 * FairReadWriteLock
 *
 * Synchronizes readers and writers using monitors.
 * Concurrency conditions:
 * 		No Read-Write / Write-Write conflicts.
 * 		Writer blocks until all preceding readers and writers acquire and release monitor.
 * 		Reader blocks until all preceding writers acquire and release monitor.
 * 		Reader is NOT blocked if all preceding writers have acquired and released the monitor.
 */
class FairReadWriteLock {
	private int rank = 0, number = 0;
	private int numReaders = 0;
	
	public synchronized void beginRead() throws InterruptedException {
		// waits for all preceding writers.
		int position = number;
		while (position > rank) {
			wait();
		}
		numReaders = numReaders + 1;
	}
	
	public synchronized void endRead() throws InterruptedException {
		// may signal waiting writers.
		numReaders = numReaders - 1;
		if (numReaders == 0) 
			notifyAll();
	}
	
	public synchronized void beginWrite() throws InterruptedException {
		int position = number;
		number = number + 1; // block all proceeding readers / writers.
		
		// wait for all preceding writers and readers to finish.
		while (position > rank || numReaders > 0) {
			wait();
		}
	}
	
	public synchronized void endWrite() {
		// signal all readers and writers in the next rank.
		rank = rank + 1;
		notifyAll();
	}
}
