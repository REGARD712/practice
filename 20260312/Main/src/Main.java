import java.util.ArrayList;
import java.util.List;

public class Main {
    static List<List<Integer>> ret = new ArrayList<>();
    static List<Integer> path = new ArrayList<>();
    static boolean[] check;
    public static List<List<Integer>> solution(int[] nums) {
        check = new boolean[nums.length];
        dfs(nums);
        return ret;
    }

    private static void dfs(int[] nums) {
        // 递归出口
        if (path.size() == nums.length) {
            ret.add(new ArrayList<>(path));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if (check[i] == false) {
                path.add(nums[i]);
                check[i] = true;
                dfs(nums);
                // 回溯：恢复现场
                path.remove(path.size() - 1);
                check[i] = false;
            }
        }
    }

    public static void main(String[] args) {
        int[] nums = {1, 2, 3};
        System.out.println(solution(nums));
    }
}
