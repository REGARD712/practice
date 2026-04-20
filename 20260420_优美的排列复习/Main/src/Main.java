class Solution {
    int ret, n;
    boolean[] check;

    public int countArrangement(int nn) {
        n = nn;
        check = new boolean[n + 1];

        dfs(1);
        return ret;
    }

    private void dfs(int pos) {
        if (pos == n + 1) {
            ret++;
            return;
        }

        for (int i = 1; i <= n; i++) {
            if (check[i] == false && (pos % i == 0 || i % pos == 0)) {
                check[i] = true;
                dfs(pos + 1);
                check[i] = false;
            }
        }
    }
}