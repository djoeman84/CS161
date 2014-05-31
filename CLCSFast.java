// package clcsFast;
import java.util.*;

class CLCSFast {

  static int[][] window;
  static char[] M, N;
  static int max_path;
  static Blacklist blacklist;

  static char getMChar (int row, int offset_y) {
    return M[(row + offset_y) % M.length];
  }

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

  static void clearWindow () {
    System.out.println("WARNING::: REMOVE THIS CALL-- clearWindow");
    for (int row = 0; row < M.length; row++) {
      for (int col = 0; col < N.length; col++) {
        window[row][col] = -1;
      }
    }
  }

  static void updateBlacklist (int offset_y) {
    for (int row = M.length - 1; row >= 0;) {
      for (int col = N.length - 1; col >= 0; ) {
        
        blacklist.setPath(row + offset_y, col, offset_y);

        if (row == 0 && col == 0)
            return;

        if (row == 0)
          {
              col--;
              continue;
          }
        if (col == 0 || col <= blacklist.leftMostValidRow (row, col, offset_y))
          {
            row--;
            continue;
          }


        if (getMChar(row, offset_y) == N[col])
          {
            if (getCell(row - 1, col - 1, offset_y) 
                == (getCell(row, col, offset_y) - 1))
              {
                row--;
                col--;
                continue;
              }
          }
        
        if (getCell(row - 1, col, offset_y) == getCell(row, col, offset_y))
          row--;
        else
          col--;

      }
    }
  }

  static int SolveWindow (int offset_y) {
    int col_hint = 1;
    for (int row = 0; row < M.length; row++) {

      int leftMostRow = blacklist.leftMostValidRow (row, col_hint, offset_y);
      System.out.format("leftMostValidRow = %d\n", leftMostRow);
      col_hint = leftMostRow + 1;
      for (int col = leftMostRow; col < N.length; col++) {

        window[row][col] = Math.max(
                            getCell(row - 1, col, offset_y),
                            getCell(row, col - 1, offset_y));

        if (M[(row + offset_y) % M.length] == N[col])
          window[row][col] = Math.max(
                            getCell(row, col, offset_y),
                            getCell(row - 1, col - 1, offset_y) + 1);
      }
    }

    printWindow (offset_y);
    System.out.println(blacklist);

    return window[M.length - 1][N.length - 1];
  }

  static void findShortestPaths (int lower, int upper) {
    if (lower >= upper)
      return;

    int mid = (lower + upper)/2;

    // System.out.format("lower: %d upper: %d\n", lower, upper);

    clearWindow();
    int score = SolveWindow (mid);
    updateBlacklist (mid);

    max_path = Math.max(score, max_path);

    // System.out.format("solved: %d, %d\n", mid, score);

    findShortestPaths(lower, mid);
    findShortestPaths(mid + 1, upper);
  }

  /* Upper and lower bounds are inclusive */
  static int SolveCLCSFast () {

    // find top, copy to bottom

    max_path = SolveWindow (0);

    updateBlacklist (0);
    updateBlacklist (M.length);

    printWindow(0);

    System.out.println(blacklist);

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
      blacklist = new Blacklist(M.length, N.length);

      System.out.format("%d\n",SolveCLCSFast ());
    }
  }


}


class Blacklist {

  private static final int BITS_PER_BYTE = 8;
  private byte[][][] blacklist_bitmap;
  private int size_m;
  private int size_n;

  public Blacklist (int size_m, int size_n) {
    assert (size_m != 0 && size_n != 0);
    this.size_m = size_m;
    this.size_n = size_n;

    int bitmap_entry_size = ((size_m - 1)/BITS_PER_BYTE) + 1;

    blacklist_bitmap = new byte[size_m * 2][size_n][bitmap_entry_size];
  }

  @Override
  public String toString () {
    String format = "";

    for (int row = 0; row < size_m * 2; row++) {
      for (int col = 0; col < size_n; col++) {
        format += "\t[";
        for (int off = 0; off <= size_m; off++) {
          if ((blacklist_bitmap[row][col][off/BITS_PER_BYTE] 
                                      & (1 << off%BITS_PER_BYTE)) == 0x0)
            {
              format += "_";
            }
          else
            {
              format += "1";     
            }
        }
        format += "]";
      }
      format += "\n";
    }

    return format;
  }

  public void setPath (int row, int col, int offset_y) {
    System.out.format("setPath (%d, %d, %d);\n", row, col, offset_y);

    System.out.format("blacklist_bitmap[%d][%d][%d];\n", row, col, offset_y/BITS_PER_BYTE);

    System.out.format("(%d << (%d));\n", 1, offset_y%BITS_PER_BYTE);

    blacklist_bitmap[row][col][offset_y/BITS_PER_BYTE] 
                                  |= (1 << (offset_y%BITS_PER_BYTE));
  }

  public int leftMostValidRow (int row, int col_hint, int offset_y) {
    int bound = -1;
    int new_bound = -1;
    for (int col = col_hint; col >= 0; col--) {

      new_bound = getBelowBoundPathNum (row, col, offset_y);

      if (new_bound != -1)
        if (bound != -1 && new_bound != bound)
          return col + 1;
        bound = new_bound;
    }
    return 0;
  }

  public boolean rightIsValidPath (int row, int col, int offset_y) {

    return false;
  }

  public boolean doesTouchWall (int row, int col, int offset_y) {
    for (int off = 0; off < size_m; off += BITS_PER_BYTE) {
      if (blacklist_bitmap[row][col][off/BITS_PER_BYTE] != 0x0)
        return true;
    }
    return false;
  }

  private int getBelowBoundPathNum (int row, int col, int offset_y) {
    /* iterate over all higher bits, check if bits are on, return on index */
    for (int off = offset_y; off < size_m; off++) {
      if (((blacklist_bitmap[row][col][off/BITS_PER_BYTE])
                          & (1 << (off%BITS_PER_BYTE))) != 0x0)
        return off;
    }
    return -1;
  }

  private int doesTouchAbovePath (int row, int col, int offset_y) { //TODO FIX SAME ISSUE AS getBelowBoundPathNum
    /* iterate over all lower bits, check if bits are on in any */
    for (int off = offset_y; off >= 0; off--) {
      if (((blacklist_bitmap[row][col][off/BITS_PER_BYTE])
                          & (1 << (off%BITS_PER_BYTE))) != 0x0)
        return off;
    }
    return -1;
  }
}