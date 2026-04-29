public class Main {
    char[][] board;
    char[] word;
    int m, n;
    boolean[][] visit;

    public boolean exist(char[][] givenBoard, String givenWord) {
        board = givenBoard; word = givenWord.toCharArray();
        m = board.length; n = board[0].length;
        visit = new boolean[m][n];

        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (board[i][j] == word[0]) {
                    visit[i][j] = true;
                    if (dfs(i, j, 1)) return true;
                    visit[i][j] = false; // 恢复现场
                }
            }
        }

        return false;
    }

    // 向量数组
    int[] dx = {0, 0, 1, -1};
    int[] dy = {1, -1, 0, 0};

    private boolean dfs(int i, int j, int pos) {
        // 递归出口
        if (pos == word.length) {
            return true;
        }

        // 在board中找word[pos]
        for (int k = 0; k < 4; k++) {
            int x = i + dx[k], y = j +dy[k];
            if (x >= 0 && x < m && y >= 0 && y < n) {
                if (!visit[x][y] && board[x][y] == word[pos]) {
                    visit[x][y] = true;
                    if (dfs(x, y, pos + 1)) return true;
                    visit[x][y] = false; // 恢复现场
                }
            }
        }

        return false;
    }
}
