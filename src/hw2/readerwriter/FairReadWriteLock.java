package hw2.readerwriter;

import java.util.concurrent.Semaphore;

class FairReadWriteLock {
    int numReaders = 0;
    Semaphore mutex = new Semaphore(1);
    Semaphore wlock = new Semaphore(1);

    public void beginRead() throws InterruptedException {
        mutex.acquire();
        numReaders++;
        if (numReaders == 1)
            wlock.acquire();
        mutex.release();
    }

    public void endRead() throws InterruptedException {
        mutex.acquire();
        numReaders--;
        if (numReaders == 0)
            wlock.release();
        mutex.release();
    }

    public void beginWrite() throws InterruptedException {
        wlock.acquire();
    }

    public void endWrite() {
        wlock.release();
    }
}