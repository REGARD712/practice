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
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                char ch = board[row][col];
                if (ch >= '1' && ch <= '9') {
                    int num = ch - '0';
                    checkRow[row][num] = checkCol[col][num] = grid[row/3][col/3][num] = true;
                }
            }
        }

        dfs();
    }

    private boolean dfs() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                // 拿到当前位置的字符
                char ch = board[row][col];
                if (ch == '.') {
                    for (int num = 1; num <= 9; num++) {
                        if (!checkRow[row][num] && !checkCol[col][num] && !grid[row/3][col/3][num]) {
                            board[row][col] = (char)(num + '0');
                            checkRow[row][num] = checkCol[col][num] = grid[row/3][col/3][num] = true;
                            if (dfs()) return true;
                            board[row][col] = '.';
                            checkRow[row][num] = checkCol[col][num] = grid[row/3][col/3][num] = false;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }
}
