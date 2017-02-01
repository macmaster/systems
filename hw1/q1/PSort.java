
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

		// choose pivot and quicksort
		int pivot = (begin + ((end - begin) / 2));
		int pivotValue = array[pivot];
		int low = begin, high = end - 1;

		while (low <= high) {
			// find a value less than pivot in left subarray
			while (array[low] < pivotValue) {
				low = low + 1;
			}

			// find a value greater than pivot in right subarray
			while (array[high] > pivotValue) {
				high = high - 1;
			}

			// if we find two valid values, swap
			if (low <= high) {
				// swap(array, low, high);
				swap(low, high);
				low = low + 1;
				high = high - 1;
			}

		}
		// Kick off two more PSort worker threads.
		PSort leftSort = new PSort(array, begin, high + 1);
		PSort rightSort = new PSort(array, low, end);
		Future<int[]> leftFuture = threadPool.submit(leftSort);
		Future<int[]> rightFuture = threadPool.submit(rightSort);
		leftFuture.get();
		rightFuture.get();
		return array;
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
	 */
	private void InsertionSort(int begin, int end) {
		for (int i = begin; i < end; i++) {
			for (int j = i; j > begin; j--) {
				if (array[j] < array[j - 1]) {
					swap(j - 1, j);
				}
			}
		}
	}

}
