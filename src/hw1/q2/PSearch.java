/** PSearch.java
 * By: Taylor Schmidt and Ronald Macmaster
 * UT-EID: trs2277   and    rpm953
 * Date: 1/30/17
 * 
 * Searches for an element in an array with parallel thread execution.
 * Returns -1 upon failure.
 * 
 */

import java.util.*;
import java.util.concurrent.*;

public class PSearch implements Callable<Integer> {

    /** worker thread partition range */
    private int begin, end;

    /** Array search parameters */
    private static int value;
    private static int[] array;
    public static final int failure = -1;

    /** Thread Execution Pool and search manager */
    private static ExecutorService threadPool;
    private static Queue<Future<Integer>> searchResults;

    public PSearch(int begin, int end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     * Allocate a certain number of worker threads. If there are more threads
     * requested than values, place a floor on the threadRange.
     * 
     * Construct a new PSearch thread and submit it to the thread pool
     * 
     * @return index of the value's array location. -1 if value not found.
     */
    public static int parallelSearch(int k, int[] A, int numThreads) {
        int index = failure;
        // null array failure.
        if (A == null) {
            return failure;
        }
        // 0 worker threads error
        else if (numThreads <= 0) {
            return failure;
        }

        try { // prepare search queue
            threadPool = Executors.newFixedThreadPool(numThreads);
            searchResults = new LinkedList<Future<Integer>>();
            array = A; // set the search array.
            value = k;

            // Kick off many parallel search threads
            int threadRange = Math.max(1, A.length / numThreads);
            int begin = 0, end = threadRange;
            while (begin < A.length) {
                end = Math.min(begin + threadRange, A.length);
                PSearch parallelSearch = new PSearch(begin, end);
                searchResults.add(threadPool.submit(parallelSearch));
                begin = end;
            }

            // reduce parallel search results
            for (Future<Integer> result : searchResults) {
                int searchResult = result.get();
                if (searchResult != failure) {
                    index = searchResult;
                    break; // found the value
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
        return index;
    }

    /**
     * Search the array partition for the value.
     */
    public Integer call() {
        for (int index = begin; index < end; index++) {
            if (array[index] == value) {
                // found the search value.
                return index;
            }
        }

        // value not found
        return failure;
    }
}
