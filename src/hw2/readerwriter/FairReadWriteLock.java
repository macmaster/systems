import java.util.concurrent.Semaphore;

class FairReadWriteLock {
    private int numReaders = 0;    
    Semaphore wlock = new Semaphore(1);
    private int takeANumber = 0;
    private int currentNumber = 0;
    public synchronized void beginRead() throws InterruptedException {
        numReaders++;
        int myNum = takeANumber++;
        while(myNum > currentNumber) {
            wait();
        }
        currentNumber++;
        notifyAll();
    }

    public synchronized void endRead() throws InterruptedException {
        numReaders--;
        // If last reader then signal that writing is allowed
        if (numReaders == 0)
            notifyAll();
    }

    public synchronized void beginWrite() throws InterruptedException {
        int myNum = takeANumber++;
        while(myNum > currentNumber || numReaders > 0) {
            wait();
        }
    }

    public synchronized void endWrite() {
        currentNumber++;
        notifyAll();
    }
}
