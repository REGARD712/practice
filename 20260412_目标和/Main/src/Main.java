public class Main {
    int ret, aim, path;

    public int findTargetSumWays(int[] nums, int target) {
        aim = target;

        dfs(nums, 0);
        return ret;
    }

    private void dfs(int[] nums, int pos) {
        // 递归出口
        if (pos == nums.length) {
            if (path == aim) ret++;
            return;
        }

        // 添加加号的情况
        path += nums[pos];
        dfs(nums, pos + 1);
        path -= nums[pos]; // 回溯恢复现场

        // 添加减号的情况
        path -= nums[pos];
        dfs(nums, pos + 1);
        path += nums[pos]; // 回溯恢复现场
    }
}

class Main2 {
    int ret, aim;

    public int findTargetSumWays(int[] nums, int target) {
        aim = target;

        dfs(nums, 0, 0);
        return ret;
    }

    private void dfs(int[] nums, int pos, int path) {
        // 递归出口
        if (pos == nums.length) {
            if (path == aim) ret++;
            return;
        }

        // 添加加号的情况
        dfs(nums, pos + 1, path + nums[pos]);

        // 添加减号的情况
        dfs(nums, pos + 1, path - nums[pos]);
    }
}
