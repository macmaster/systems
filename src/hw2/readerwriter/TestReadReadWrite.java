package hw2.readerwriter;

import edu.umd.cs.mtc.MultithreadedTestCase;

public class TestReadReadWrite extends MultithreadedTestCase{
	
	public void thread1 () throws InterruptedException {
		Run.lock.beginRead();
		waitForTick(3);
		Run.lock.endRead();
	}
	
	public void thread2 () throws InterruptedException {
		waitForTick(1);
		Run.lock.beginRead();
		assertTick(1);
		Run.lock.endRead();
	}
	
	public void thread3() throws InterruptedException {
		waitForTick(2);
		Run.lock.beginWrite();
		assertTick(3);
		Run.lock.endWrite();
	}
	
	public void thread4() throws InterruptedException {
		waitForTick(3);
		Run.lock.beginRead();
		assertTick(3);
		Run.lock.endRead();;
	}
	
	
	public void thread5() throws InterruptedException {
		waitForTick(6);
		Run.lock.beginRead();
		System.out.println("Reading in read read write (1)");
		Run.lock.beginRead();
		System.out.println("Reading in read read write (1)");
		Run.lock.beginRead();
		System.out.println("Reading in read read write (1)");
		assertTick(6);
		Run.lock.endRead();
		Run.lock.endRead();
		Run.lock.endRead();
	}
	
	public void thread6() throws InterruptedException {
		waitForTick(7);
		Run.lock.beginWrite();
		assertTick(7);
		waitForTick(9);
		assertTick(9);
		System.out.println("Writing in read read write");
		Run.lock.endWrite();
	}
	
	public void thread7() throws InterruptedException {
		waitForTick(8);
		Run.lock.beginRead();
		assertTick(9);
		System.out.println("Reading in read read write (1)");
		Run.lock.endRead();
	}
}
