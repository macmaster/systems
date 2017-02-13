package hw2.readerwriter;

class FairReadWriteLock {
    private int rank = 0, number = 0;
    private int numReaders = 0;

    public synchronized void beginRead() throws InterruptedException {
        int position = number;
        while (position > rank) {
            wait();
        }
        numReaders = numReaders + 1;
    }

    public synchronized void endRead() throws InterruptedException {
        numReaders = numReaders - 1;
        if (numReaders == 0)
            notifyAll();
    }

    public synchronized void beginWrite() throws InterruptedException {
        int position = number;
        number = number + 1;
        while (position > rank || numReaders > 0) {
            wait();
        }
    }

    public synchronized void endWrite() {
        rank = rank + 1;
        notifyAll();
    }
}
