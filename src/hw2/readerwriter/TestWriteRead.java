package hw2.readerwriter;

import edu.umd.cs.mtc.MultithreadedTestCase;

public class TestWriteRead extends MultithreadedTestCase {
	
	
	public void thread1() throws InterruptedException {
		Run.lock.beginWrite();
		waitForTick(2);
		Run.lock.endWrite();
	}
	
	public void thread2() throws InterruptedException {
		waitForTick(1);
		Run.lock.beginRead();
		assertTick(2);
		Run.lock.endRead();
	}
}
