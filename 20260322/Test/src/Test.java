import java.util.ArrayList;
import java.util.List;

public class Test {
    public List<List<Integer>> ret = new ArrayList<>();
    public List<Integer> path = new ArrayList<>();

    public List<List<Integer>> subset(int[] nums) {
        dfs(nums, 0);
        return ret;
    }

    private void dfs1(int[] nums, int pos) {
        if (path.size() == nums.length) {
            ret.add(new ArrayList<>(path));
            return;
        }

        // 选择当前数字
        path.add(nums[pos]);
        dfs1(nums, pos + 1);
        path.remove(path.size() - 1);  // 回溯恢复现场

        // 不选择当前数字
        dfs1(nums, pos + 1);
    }

    private void dfs(int[] nums, int pos) {
        // 更新结果
        ret.add(new ArrayList<>(path));

        // 向后遍历数字
        for (int i = pos; i < nums.length; i++) {
            path.add(nums[i]);
            dfs(nums, i + 1);
            path.remove(path.size() - 1);  // 回溯恢复现场
        }
    }
}
