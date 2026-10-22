import java.util.Arrays;

/**
 * 快速排序算法实现
 * Quick Sort Algorithm Implementation
 *
 * 平均时间复杂度: O(n log n)
 * 最坏时间复杂度: O(n²) —— 数组已有序时（可通过随机化基准优化）
 * 空间复杂度:     O(log n) —— 递归调用栈
 * 是否稳定:       否
 */
public class QuickSort {

    // ─────────────────────────────────────────────
    // 一、标准快速排序（随机化基准，防止最坏情况）
    // ─────────────────────────────────────────────

    /**
     * 标准快速排序入口
     *
     * @param arr   待排序数组
     * @param left  左边界索引
     * @param right 右边界索引
     */
    public static void quickSort(int[] arr, int left, int right) {
        if (left < right) {
            // 随机选取基准，降低最坏情况概率
            randomizePivot(arr, left, right);
            int pivotIndex = partition(arr, left, right);
            quickSort(arr, left, pivotIndex - 1);
            quickSort(arr, pivotIndex + 1, right);
        }
    }

    /**
     * 随机选取 [left, right] 内一个元素与末尾交换，作为基准
     */
    private static void randomizePivot(int[] arr, int left, int right) {
        int randomIndex = left + (int) (Math.random() * (right - left + 1));
        swap(arr, randomIndex, right);
    }

    /**
     * 分区操作（Lomuto 分区方案）
     * 以 arr[right] 为基准，将数组分为 <=pivot 和 >pivot 两部分
     *
     * @return 基准元素最终所在的索引
     */
    private static int partition(int[] arr, int left, int right) {
        int pivot = arr[right]; // 基准值
        int i = left - 1;       // i 指向小于等于 pivot 区域的末尾

        for (int j = left; j < right; j++) {
            if (arr[j] <= pivot) {
                i++;
                swap(arr, i, j);
            }
        }
        // 将基准放到正确位置
        swap(arr, i + 1, right);
        return i + 1;
    }

    // ─────────────────────────────────────────────
    // 二、三路快速排序（Dutch National Flag，处理大量重复元素）
    // ─────────────────────────────────────────────

    /**
     * 三路快速排序入口
     * 将数组分为 <pivot、==pivot、>pivot 三个区间，
     * 相等元素无需继续递归，显著提升重复元素场景的性能
     *
     * @param arr   待排序数组
     * @param left  左边界索引
     * @param right 右边界索引
     */
    public static void quickSort3Way(int[] arr, int left, int right) {
        if (left >= right) return;

        int pivot = arr[left + (int) (Math.random() * (right - left + 1))];

        // lt: arr[left..lt-1]  < pivot
        // gt: arr[gt+1..right] > pivot
        // i:  当前遍历指针
        int lt = left, i = left, gt = right;

        while (i <= gt) {
            if (arr[i] < pivot) {
                swap(arr, lt++, i++);
            } else if (arr[i] > pivot) {
                swap(arr, i, gt--);
            } else {
                i++;
            }
        }
        // arr[lt..gt] 全等于 pivot，无需再排这段
        quickSort3Way(arr, left, lt - 1);
        quickSort3Way(arr, gt + 1, right);
    }

    // ─────────────────────────────────────────────
    // 工具方法
    // ─────────────────────────────────────────────

    /**
     * 交换数组中两个索引位置的元素
     */
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    // ─────────────────────────────────────────────
    // 主方法 —— 多场景测试
    // ─────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║          快速排序算法演示                ║");
        System.out.println("╚══════════════════════════════════════════╝");

        // 测试场景
        int[][] testCases = {
            {64, 34, 25, 12, 22, 11, 90},  // 普通乱序
            {1, 2, 3, 4, 5, 6, 7},         // 已升序（最坏情况，随机化基准优化）
            {7, 6, 5, 4, 3, 2, 1},         // 已降序
            {5, 5, 5, 5, 5},               // 全部相同
            {-10, 3, -5, 0, 8, -1, 7},     // 含负数
            {42},                           // 单元素
            {}                              // 空数组
        };
        String[] labels = {
            "普通乱序",
            "已升序  ",
            "已降序  ",
            "全部相同",
            "含负数  ",
            "单元素  ",
            "空数组  "
        };

        System.out.println("\n── 标准快速排序（随机化基准 + Lomuto 分区）──");
        for (int i = 0; i < testCases.length; i++) {
            int[] arr = Arrays.copyOf(testCases[i], testCases[i].length);
            int[] original = Arrays.copyOf(arr, arr.length);
            if (arr.length > 0) {
                quickSort(arr, 0, arr.length - 1);
            }
            System.out.printf("  [%s]  排序前: %-32s-> 排序后: %s%n",
                    labels[i], Arrays.toString(original) + " ", Arrays.toString(arr));
        }

        System.out.println("\n── 三路快速排序（Dutch National Flag）──");
        for (int i = 0; i < testCases.length; i++) {
            int[] arr = Arrays.copyOf(testCases[i], testCases[i].length);
            int[] original = Arrays.copyOf(arr, arr.length);
            if (arr.length > 0) {
                quickSort3Way(arr, 0, arr.length - 1);
            }
            System.out.printf("  [%s]  排序前: %-32s-> 排序后: %s%n",
                    labels[i], Arrays.toString(original) + " ", Arrays.toString(arr));
        }

        System.out.println("\n── 算法复杂度 ──");
        System.out.println("  平均时间复杂度 : O(n log n)");
        System.out.println("  最坏时间复杂度 : O(n²)      — 随机化基准可大幅规避");
        System.out.println("  空间复杂度     : O(log n)   — 递归调用栈");
        System.out.println("  稳定性         : 不稳定");
    }
}
