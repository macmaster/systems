package hw2.readerwriter;

import edu.umd.cs.mtc.TestFramework;

public class Run {
    final static OldFairReadWriteLock lock = new OldFairReadWriteLock();

    public static void main(String[] args) throws Throwable {
        TestFramework.runOnce(new TestReadRead());
        TestFramework.runOnce(new TestReadWrite());
        TestFramework.runOnce(new TestWriteRead());
        TestFramework.runOnce(new TestReadReadWrite());
        // TestFramework.runOnce(new TestFairReadWriteAfterWrite());
        System.out.println("All tests passed!");
    }
}