/**
 * 二分查找（Binary Search）
 *
 * 前提：数组必须是有序数组（此处默认升序）。
 * 时间复杂度：O(log n)  空间复杂度：迭代 O(1) / 递归 O(log n)
 */
public class BinarySearch {

    /**
     * 迭代版二分查找。
     *
     * @param arr    升序排列的数组
     * @param target 待查找的元素
     * @return 找到则返回元素下标，否则返回 -1
     */
    public static int binarySearch(int[] arr, int target) {
        if (arr == null || arr.length == 0) {
            return -1;
        }

        int left = 0;
        int right = arr.length - 1;

        while (left <= right) {
            // 使用 left + (right - left) / 2 而非 (left + right) / 2，避免大数相加溢出
            int mid = left + (right - left) / 2;

            if (arr[mid] == target) {
                return mid;
            } else if (arr[mid] < target) {
                left = mid + 1;   // 目标在右半区间
            } else {
                right = mid - 1;  // 目标在左半区间
            }
        }

        return -1;
    }

    /**
     * 递归版二分查找。
     *
     * @param arr    升序排列的数组
     * @param target 待查找的元素
     * @return 找到则返回元素下标，否则返回 -1
     */
    public static int binarySearchRecursive(int[] arr, int target) {
        if (arr == null || arr.length == 0) {
            return -1;
        }
        return binarySearchRecursive(arr, target, 0, arr.length - 1);
    }

    private static int binarySearchRecursive(int[] arr, int target, int left, int right) {
        if (left > right) {
            return -1;
        }

        int mid = left + (right - left) / 2;

        if (arr[mid] == target) {
            return mid;
        } else if (arr[mid] < target) {
            return binarySearchRecursive(arr, target, mid + 1, right);
        } else {
            return binarySearchRecursive(arr, target, left, mid - 1);
        }
    }

    public static void main(String[] args) {
        int[] arr = {1, 3, 5, 7, 9, 11, 13, 15, 17, 19};

        int[] targets = {7, 1, 19, 4};
        for (int target : targets) {
            int index = binarySearch(arr, target);
            int indexRecursive = binarySearchRecursive(arr, target);

            if (index != -1) {
                System.out.printf("元素 %d 找到，下标为 %d（递归结果：%d）%n", target, index, indexRecursive);
            } else {
                System.out.printf("元素 %d 未找到%n", target);
            }
        }
    }
}
