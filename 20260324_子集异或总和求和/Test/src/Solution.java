public class Solution {
    int sum;  // 记录异或值之和
    int path; // 记录子集中所有元素的异或值

    public int subsetXORSum(int[] nums) {
        dfs(nums, 0);
        return sum;
    }

    private void dfs(int[] nums, int pos) {
        sum += path;  // 更新结果

        // 从当前数字往后遍历
        for (int i = pos; i < nums.length; i++) {
            path ^= nums[i];
            dfs(nums, i + 1);
            path ^= nums[i];  // 回溯时恢复现场
        }
    }
}
