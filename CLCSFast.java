// package clcsFast;
import java.util.*;

class CLCSFast {

  static int[][] window;
  static char[] M, N;
  static int max_path;

  static int getCell (int row, int col, int offset_y) {
    if (row < 0 || col < 0)
      return 0;
    return window[row][col];
  }

  static void printWindow (int offset_y) {
    System.out.format("\nWindow: offset_y = %d\n", offset_y);
    System.out.format("\t");
    for (int col = 0; col < N.length; col++) {
      System.out.format(" %c ", N[col]);
    }

    System.out.format("\n");
    for (int row = 0; row < M.length; row++) {
      System.out.format("%c\t", M[(row + offset_y) % M.length]);
      for (int col = 0; col < N.length; col++) {
        System.out.format(" %d ", window[row][col]);
      }
      System.out.format("\n");
    }
    System.out.format("\n");
  }

  static int SolveWindow (int offset_y) {
    for (int row = 0; row < M.length; row++) {
      for (int col = 0; col < N.length; col++) {

        window[row][col] = Math.max(
                            getCell(row - 1, col, offset_y),
                            getCell(row, col - 1, offset_y));

        if (M[(row + offset_y) % M.length] == N[col])
          window[row][col] = Math.max(
                            getCell(row, col, offset_y),
                            getCell(row - 1, col - 1, offset_y) + 1);
      }
    }

    // printWindow (offset_y);

    return window[M.length - 1][N.length - 1];
  }

  static void findShortestPaths (int lower, int upper) {
    if (lower >= upper)
      return;

    int mid = (lower + upper)/2;

    // System.out.format("lower: %d upper: %d\n", lower, upper);

    int score = SolveWindow(mid);

    max_path = Math.max(score, max_path);

    // System.out.format("solved: %d, %d\n", mid, score);

    findShortestPaths(lower, mid);
    findShortestPaths(mid + 1, upper);
  }

  /* Upper and lower bounds are inclusive */
  static int SolveCLCSFast () {

    // find top, copy to bottom

    max_path = SolveWindow (0);

    findShortestPaths (1, M.length);


    return max_path;
  }




  public static void main(String[] args) {
    char[] A, B;
    Scanner s = new Scanner(System.in);
    int T = s.nextInt();
    for (int tc = 0; tc < T; tc++) {
      A = s.next().toCharArray();
      B = s.next().toCharArray();

      M = (A.length > B.length) ? B : A;
      N = (A.length > B.length) ? A : B;

      window = new int[M.length][N.length];

      System.out.format("%d\n",SolveCLCSFast ());
    }
  }


}


class Blacklist {
  char[][][] blacklist_bitmap;

  public Blacklist (int size_m, int size_n) {
    
  }

  public static int helloWorld () {
    return 1;
  }
}