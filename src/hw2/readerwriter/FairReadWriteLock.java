package hw2.readerwriter;

import java.util.concurrent.Semaphore;

class FairReadWriteLock {
    private int numReaders = 0;    
    Semaphore wlock = new Semaphore(1);

    public synchronized void beginRead() throws InterruptedException {
        numReaders++;
        if (numReaders == 1)
            wlock.acquire();
    }

    public synchronized void endRead() throws InterruptedException {
        numReaders--;
        if (numReaders == 0)
            wlock.release();
    }

    public synchronized void beginWrite() throws InterruptedException {
        wlock.acquire();
    }

    public synchronized void endWrite() {
        wlock.release();
    }
}