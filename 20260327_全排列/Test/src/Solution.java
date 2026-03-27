import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Solution {
    List<List<Integer>> ret;
    List<Integer> path;
    boolean[] check;

    public List<List<Integer>> permuteUnique(int[] nums) {
        ret = new ArrayList<>();
        path = new ArrayList<>();
        check = new boolean[nums.length];

        // 排序
        Arrays.sort(nums);

        dfs(nums);
        return ret;
    }

    private void dfs(int[] nums) {
        if (path.size() == nums.length) {
            ret.add(new ArrayList<>(path));
            return;
        }

        for (int i = 0; i < nums.length; i++) {
            if ((check[i] == false) && (i == 0 || nums[i] != nums[i - 1] || check[i - 1] == false)) {
                path.add(nums[i]);
                check[i] = true;
                dfs(nums);
                path.remove(path.size() - 1);
                check[i] = false;
            }
        }
    }
}
