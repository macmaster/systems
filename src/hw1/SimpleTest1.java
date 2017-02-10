package hw1;
import java.util.Arrays;

public class SimpleTest1 {
    public static void main(String[] args) {

        int[] A1 = { 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
        verifyParallelSort(A1);

        int[] sortedArray = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        verifyParallelSort(sortedArray);

        int[] A2 = { 1, 3, 5, 7, 9 };
        verifyParallelSort(A2);

        int[] A3 = { 13, 59, 24, 18, 33, 20, 11, 11, 13, 50, 10999, 97 };
        verifyParallelSort(A3);

        int[] A4 = {};
        verifyParallelSort(A4);

        int[] A5 = { 6 };
        verifyParallelSort(A5);

        final int randomLength = 1000000;
        int[] A6 = new int[randomLength];
        for (int i = 0; i < randomLength; i++) {
            A6[i] = (int) (Math.random() * randomLength);
        }
        verifyParallelSort(A6);

    }

    static void verifyParallelSort(int[] A) {
        int[] B = new int[A.length];
        System.arraycopy(A, 0, B, 0, A.length);

        System.out.println("Verify Parallel Sort for array: ");
        // printArray(A);

        Arrays.sort(A);
        PSort.parallelSort(B, 0, B.length);

        boolean isSuccess = true;
        for (int i = 0; i < A.length; i++) {
            if (A[i] != B[i]) {
                System.out.println("Your parallel sorting algorithm is not correct");
                System.out.println("Expect:");
                printArray(A);
                System.out.println("Your results: " + i);
                System.out.println("A[i]: " + A[i] + " B[i]: " + B[i]);
                printArray(B);
                isSuccess = false;
                break;
            }
        }

        if (isSuccess) {
            System.out.println("Great, your sorting algorithm works for this test case");
        }
        System.out.println("=========================================================");
    }

    public static void printArray(int[] A) {
        for (int i = 0; i < A.length; i++) {
            if (i != A.length - 1) {
                System.out.print(A[i] + " ");
            } else {
                System.out.print(A[i]);
            }
        }
        System.out.println();
    }

    public static void TestInsertionSort(int[] A) {

    }
}
