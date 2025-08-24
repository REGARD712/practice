import java.util.ArrayList;

import java.util.List;

public class Test {
    /*
    旋转数组：
    给定一个整数数组 nums，将数组中的元素向右轮转 k 个位置，其中 k 是非负数
     */
    public void rotate(int[] nums, int k) {
        int n = nums.length;
        int[] ret = new int[n];
        for (int i = 0; i < n; i++) {
            ret[(i+k)%n] = nums[i];
        }
        System.arraycopy(ret,0,nums,0,n);
    }


    //杨辉三角
    public static List<List<Integer>> generate(int numRows) {
        List<List<Integer>> list = new ArrayList<>();
        List<Integer> list0 = new ArrayList<>();
        list0.add(1);
        list.add(list0);

        for (int i = 1; i < numRows; i++) {
            List<Integer> curRow = new ArrayList<>();
            curRow.add(1);

            for (int j = 1; j < i; j++) {
                List<Integer> preRow = list.get(i-1);
                int v1 = preRow.get(j);
                int v2 = preRow.get(j-1);
                curRow.add(v1+v2);
            }

            curRow.add(1);
            list.add(curRow);
        }
        return list;
    }

    public static void main1(String[] args) {
        List<List<Integer>> list = generate(5);
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).size(); j++) {
                System.out.print(list.get(i).get(j)+" ");
            }
            System.out.println();
        }
    }
}
