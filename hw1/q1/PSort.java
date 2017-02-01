//UT-EID=

import java.util.*;
import java.util.concurrent.*;

public class PSort implements Callable<int[]> {

    private int[] array;
    private int begin, end;
    private static ExecutorService threadPool;

    /**
     * Initialize and kick off a parallel quick sort.
     */
    public static void parallelSort(int[] A, int begin, int end) {
        try {
            threadPool = Executors.newCachedThreadPool();
            PSort parallelSort = new PSort(A, begin, end);
            Future<int[]> sortResult = threadPool.submit(parallelSort);
            System.out.println("return value: " + sortResult.get());
            // sortResult.get();
        } catch (Exception err) {
            err.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    public PSort(int[] A, int begin, int end) {
        this.array = A;
        this.begin = begin;
        this.end = end;
    }

    /**
     * Recursive parallel calls to quicksort threads.
     * Partition according to a middle pivot, then kick off two more threads.
     * Insertion sort for base cases of length <= 4.
     */
    public int[] call() throws InterruptedException, ExecutionException {
        // System.out.format("begin: %d, end: %d\n", begin, end);
        // base case: insertion sort for length <= 4
        int length = end - begin;
        if (length <= 4) {
            InsertionSort(array, begin, end);
            return array;
        }

        // choose pivot and quicksort
        int low = begin, high = end - 1;
        int pivotI = (low + ((high - low) / 2));
        int pivotValue = array[pivotI];

        while (low <= high) {
            // find a value less than pivot in left sub-array
            while (array[low] < pivotValue) { low += 1; }

            // find a value greater than pivot in right subarray
            while (array[high] > pivotValue) { high -= 1; }

            // if we find two valid values, swap
            if (low <= high) {
                swap(array, low, high);
                low += 1;
                high -= 1;
            }
        }

        // if ((begin >= high + 1) || (low > end)) {
        // SimpleTest.printArray(Arrays.copyOfRange(array, begin, end));
        // System.err.println("ERROR: potential array conflict here!");
        // System.out.format("begin: %d, end: %d\n", begin, end);
        // System.out.format("low: %d, high: %d\n", low, high);
        // }

        // Kick off two more PSort worker threads.
        // [0, 1, 2, 3, 4, 5, 6]
        Future<int[]> leftFuture = null;
        Future<int[]> rightFuture = null;
        if (begin < high) {
            PSort leftSort = new PSort(array, begin, high + 1);
            leftFuture = threadPool.submit(leftSort);
        }
        if (low < end - 1) {
            PSort rightSort = new PSort(array, low, end);
            rightFuture = threadPool.submit(rightSort);
        }
        if (leftFuture != null) { leftFuture.get(); }
        if (rightFuture != null) { rightFuture.get(); }
        return array;
    }

    /**
     * Swap the two elements in the object array.
     */
    private static void swap(int[] array, int low, int high) {
        // if ((low >= high)) {
        // System.err.println("ERROR: potential swap error!");
        // System.out.format("begin: %d, end: %d\n", begin, end);
        // System.out.format("low: %d, high: %d\n", low, high);
        // }
        int temp = array[low];
        array[low] = array[high];
        array[high] = temp;

    }

    /**
     * Insertion sort in place the values in the PSort array. end denotes 1
     * index higher than the top index.
     */
    private static void InsertionSort(int[] array, int begin, int end) {
        for (int i = begin; i < end; i++) {
            int j = i;
            while (j > 0 && array[j - 1] > array[j]) {
                swap(array, j - 1, j);
                j -= 1;
            }
        }
    }

}
