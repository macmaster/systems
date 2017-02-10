
/** PSort.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 1/30/17
 * 
 * Sorts the elements of an array in parallel using quicksort.
 * uses Insertion sort for base case: arrays of length <= 4.
 * 
 * Credits:
 * Quick Sort implementation was derived from
 *      http://www.java2novice.com/java-sorting-algorithms/quick-sort/
 * Insertion Sort implementation was derived from
 *      http://www.java2novice.com/java-sorting-algorithms/insertion-sort/
 */

package hw1;
import java.util.concurrent.*;

public class PSort implements Callable<int[]> {

    private int begin, end;
    private int[] array;
    private static ExecutorService threadPool;

    /**
     * Initialize and kick off a parallel quick sort.
     */
    public static void parallelSort(int[] A, int begin, int end) {
        try {
            threadPool = Executors.newCachedThreadPool();
            PSort parallelSort = new PSort(A, begin, end);
            Future<int[]> sortResult = threadPool.submit(parallelSort);
            // System.out.println("return value: " + sortResult.get());
            sortResult.get();
        } catch (Exception err) {
            err.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }

    public PSort(int[] array, int begin, int end) {
        this.array = array;
        this.begin = begin;
        this.end = end;
    }

    /**
     * Recursive parallel calls to quicksort threads. Partition according to a
     * middle pivot, then kick off two more threads. Insertion sort for base
     * cases of length <= 4.
     */
    public int[] call() throws InterruptedException, ExecutionException {
        // System.out.format("begin: %d, end: %d\n", begin, end);
        // base case: insertion sort for length <= 4
        int length = end - begin;
        if (length <= 4) {
            InsertionSort(begin, end);
            return array;
        }

        /**
         * Credits: Quick Sort implementation was derived from
         * http://www.java2novice.com/java-sorting-algorithms/quick-sort/
         */
        // choose pivot and quicksort
        int low = begin, high = end - 1;
        int pivotI = (low + ((high - low) / 2));
        int pivotValue = array[pivotI];

        while (low <= high) {
            // find a value less than pivot in left sub-array
            while (array[low] < pivotValue) {
                low += 1;
            }

            // find a value greater than pivot in right subarray
            while (array[high] > pivotValue) {
                high -= 1;
            }

            // if we find two valid values, swap
            if (low <= high) {
                swap(low, high);
                low += 1;
                high -= 1;
            }
        }

        // Kick off two more PSort worker threads.
        // unconditional thread launching
        PSort leftSort = new PSort(array, begin, high + 1);
        PSort rightSort = new PSort(array, low, end);
        Future<int[]> rightFuture = threadPool.submit(rightSort);
        Future<int[]> leftFuture = threadPool.submit(leftSort);
        leftFuture.get();
        rightFuture.get();
        return array;

        // conditional thread launching
        // Future<int[]> leftFuture = null;
        // Future<int[]> rightFuture = null;
        // if (begin < high) {
        // PSort leftSort = new PSort(array, begin, high + 1);
        // leftFuture = threadPool.submit(leftSort);
        // }
        // if (low < end - 1) {
        // PSort rightSort = new PSort(array, low, end);
        // rightFuture = threadPool.submit(rightSort);
        // }
        // if (leftFuture != null) { leftFuture.get(); }
        // if (rightFuture != null) { rightFuture.get(); }

    }

    /**
     * Swap the two elements in the object array.
     */
    private void swap(int low, int high) {
        int temp = array[low];
        array[low] = array[high];
        array[high] = temp;
    }

    /**
     * Insertion sort in place the values in the PSort array. end denotes 1
     * index higher than the top index.
     * 
     * Insertion Sort implementation was derived from
     * http://www.java2novice.com/java-sorting-algorithms/insertion-sort/
     */
    private void InsertionSort(int begin, int end) {
        for (int i = begin; i < end; i++) {
            int j = i;
            while (j > begin && array[j - 1] > array[j]) {
                swap(j - 1, j);
                j -= 1;
            }
        }
    }

}
