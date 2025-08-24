import java.util.ArrayList;
import java.util.List;

public class Test {
    //杨辉三角
    public static List<List<Integer>> generate(int numRows) {
        List<List<Integer>> list = new ArrayList<>();

        List<Integer> list0 = new ArrayList<>();
        list0.add(1);
        list.add(list0);

        for (int i = 1; i < numRows; i++) {
            List<Integer> curRow = new ArrayList<>();
            curRow.add(1);

            List<Integer> preRow = list.get(i-1);
            for (int j = 1; j < i; j++) {
                int v1 = preRow.get(j);
                int v2 = preRow.get(j-1);
                curRow.add(v1+v2);
            }

            curRow.add(1);
            list.add(curRow);
        }
        return list;
    }

    public static void main(String[] args) {
        List<List<Integer>> list = generate(5);
        System.out.println(list);
    }
}
