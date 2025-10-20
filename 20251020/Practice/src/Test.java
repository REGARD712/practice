import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        int[] array = {34,93,71,31,65,51,94,10,38,99};
        System.out.println("排序前："+ Arrays.toString(array));
        quickSort(array);
        System.out.println("排序后："+ Arrays.toString(array));
    }

    // 快速排序
    public static void quickSort(int[] arr) {
        quick(arr,0,arr.length-1);
    }
    private static void quick (int[] arr, int start, int end) {
        if (start >= end) {
            return;
        }

        // 小范围使用直接插入排序
        if (end-start+1 <= 7) {
            insertSortRange(arr,start,end);
            return;
        }

        // 三数取中
        int midIndex = getMiddleNum(arr,start,end);
        swap(arr,start,midIndex);

        // 划分
        int pivot = partition(arr,start,end);

        // 递归
        quick(arr,start,pivot-1);
        quick(arr,pivot+1,end);
    }

    private static int partition(int[] arr, int left, int right) {
        int temp = arr[left];

        while (left < right) {
            while (left < right && arr[right] > temp) {
                right--;
            }
            arr[left] = arr[right];

            while (left < right && arr[left] < temp) {
                left++;
            }
            arr[right] = arr[left];
        }
        arr[left] = temp;
        return left;
    }

    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    private static int getMiddleNum(int[] arr,int start, int end) {
        int mid = (start + end) / 2;
        if (start < end) {
            if (mid < start) {
                return start;
            } else if (mid > end) {
                return end;
            } else {
                return mid;
            }
        } else {
            if (mid < end) {
                return end;
            } else if (mid > start) {
                return start;
            } else {
                return mid;
            }
        }
    }

    private static void insertSortRange(int[] arr,int start, int end) {
        for (int i = start+1; i < end; i++) {
            int temp = arr[i];
            int j = i - start;
            for (; j >= start; j--) {
                if (arr[j] > temp) {
                    arr[j+1] = arr[j];
                } else {
                    arr[j+1] = temp;
                    break;
                }
            }
            arr[j+1] = temp;
        }
    }
}
