public class Main {
    boolean[][] checkRow, checkCol;
    boolean[][][] grid;
    char[][] board;

    public void solveSudoku(char[][] givenBoard) {
        board = givenBoard;
        checkRow = new boolean[9][10];
        checkCol = new boolean[9][10];
        grid = new boolean[3][3][10];

        // 初始化数独状态
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                char ch = board[i][j];
                if (ch >= '1' && ch <= '9') {
                    int num = ch - '0';
                    checkRow[i][num] = checkCol[j][num] = grid[i/3][j/3][num] = true;
                }
            }
        }

        dfs();
    }

    private boolean dfs() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                char ch = board[i][j];
                if (ch == '.') {
                    // 将数字放到该位置上
                    for (int num = 1; num <= 9; num++) {
                        if (!checkRow[i][num] && !checkCol[j][num] && !grid[i/3][j/3][num]) {
                            board[i][j] = (char)(num + '0');
                            checkRow[i][num] = checkCol[j][num] = grid[i/3][j/3][num] = true;
                            if (dfs()) return true; // 告诉上层该位置放该数字有效
                            // 若放法无效，就恢复现场
                            board[i][j] = '.';
                            checkRow[i][num] = checkCol[j][num] = grid[i/3][j/3][num] = false;
                        }
                    }
                    return false; // 若走到此处仍未返回，说明该位置已经没有数字能放了
                }
            }
        }
        return true; // 若走到此处仍未返回，说明数独中已经没有空位置
    }
}
