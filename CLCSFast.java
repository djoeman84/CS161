// package clcsFast;
import java.util.*;
import java.io.*;

class CLCSFast {

  static Window window;
  static char[] M, N;
  static int max_path;
  static Blacklist blacklist_up;
  static Blacklist blacklist_down;

  static final boolean DEBUG = false;
  static final boolean RESET_EACH_ITER = false;

  static char getMChar (int row, int offset_y) {
    return M[(row + offset_y) % M.length];
  }

  static char getNChar (int col) {
    return N[col];
  }

  static boolean charMatchAt (int row, int col, int offset_y) {
    return getMChar(row, offset_y) == getNChar(col);
  }


  static void updateBlacklists (int offset_y) {
    int row = M.length - 1;
    int col = N.length - 1;

    while(true) {
      if (row == 0 && col == 0)
        {
          return;
        }

      if (row == 0)
        {
          col--;
          blacklist_down.blacklist(row + 1, col, offset_y);
          continue;
        }

      if (col == 0)
        {
          row--;
          blacklist_up.blacklist(row, col + 1, offset_y);
          continue;         
        }

      int type = window.getCellType(row, col);

      switch (type) {
        case Window.TRANS_DIAG:
          row--;
          col--;

          blacklist_up.blacklist(row, col + 1, offset_y);
          blacklist_down.blacklist(row + 1, col, offset_y);
          break;
        case Window.TRANS_LEFT:
          col--;
          blacklist_down.blacklist(row + 1, col, offset_y);
          break;
        case Window.TRANS_UP:
          row--;
          blacklist_up.blacklist(row, col + 1, offset_y);
          break;
      }


      continue;
    }
  }

  static void findShortestPaths (int lower, int upper) {
    if (lower >= upper)
      return;

    int mid = (lower + upper)/2;

    // System.out.format("lower: %d upper: %d\n", lower, upper);


    window.clear();
    int score = window.solve (mid);
    updateBlacklists (mid);
    window.print(mid);

    if (DEBUG)
      {
        System.out.println(":::blacklist up:::");
        blacklist_up.print();

        System.out.println(":::blacklist down:::");
        blacklist_down.print();
      }

    max_path = Math.max(score, max_path);

    // System.out.format("solved: %d, %d\n", mid, score);

    findShortestPaths(lower, mid);
    findShortestPaths(mid + 1, upper);
  }

  /* Upper and lower bounds are inclusive */
  static int SolveCLCSFast () {

    // find top, copy to bottom

    max_path = window.solve (0);

    updateBlacklists (0);
    blacklist_up.print();
    blacklist_down.print();

    window.print(0);
    updateBlacklists (M.length);


    blacklist_up.print();
    blacklist_down.print();

    findShortestPaths (1, M.length);


    return max_path;
  }




  public static void main(String[] args) {

    boolean eclipse = false;
     
    try {
      if (eclipse)
        System.setIn(new FileInputStream("./sample.in"));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    char[] A, B;
    Scanner s = new Scanner(System.in);
    int T = s.nextInt();
    for (int tc = 0; tc < T; tc++) {
      A = s.next().toCharArray();
      B = s.next().toCharArray();

      M = (A.length > B.length) ? B : A;
      N = (A.length > B.length) ? A : B;

      window = new Window(M.length, N.length);
      blacklist_up = new Blacklist(M.length, N.length, 
                                Blacklist.BlacklistType.NORMAL);
      blacklist_down = new Blacklist(M.length, N.length,
                                Blacklist.BlacklistType.TRANSPOSE);

      System.out.format("%d\n",SolveCLCSFast ());
    }
  }


}

class Window {
  private int [][] window_frame;
  private int size_m;
  private int size_n;

  private int[] row_max;
  private int[] col_max;

  private static final int CELL_DOESNT_EXIST = -1;

  public static final int TRANS_LEFT = 0;
  public static final int TRANS_DIAG = 1;
  public static final int TRANS_UP   = 2;

  private static final int TRANSITION_MASK = 3;
  private static final int TRANS_VAL_SHIFT = 2;


  public Window (int size_m, int size_n) {
    

    this.size_m = size_m;
    this.size_n = size_n;

    this.initWindowCells();
    this.initMaxArrs();
  }

  public void print (int offset_y) {
    if (CLCSFast.DEBUG) {
      System.out.format("\nWindow: offset_y = %d\n", offset_y);
      System.out.format("row_max: [");
      for (int i = 0; i < this.size_n; i++) {
        if (i!=0)
          System.out.format(",");  
        System.out.format("%d", this.getMaxRowCell(i));
      }
      System.out.format("]\tcol_max: [");
      for (int i = 0; i < this.size_m; i++) {
        if (i!=0)
          System.out.format(",");  
        System.out.format("%d", this.getMaxColCell(i));
      }
      System.out.format("]\n\t");
      for (int col = 0; col < this.size_n; col++) {
        System.out.format(" %c ", CLCSFast.getNChar(col));
      }

      System.out.format("\n");
      for (int row = 0; row < this.size_m; row++) {
        System.out.format("%c\t", CLCSFast.getMChar(row, offset_y));
        for (int col = 0; col < this.size_n; col++) {
          if (getCell(row, col) < 0 || getCell(row, col) > 10)
            System.out.format("%d ", getCell(row, col));
          else  
            System.out.format(" %d ", getCell(row, col));
        }
        System.out.format("\n");
      }
      System.out.format("\n\t");


      for (int col = 0; col < this.size_n; col++) {
        System.out.format(" %c ", CLCSFast.getNChar(col));
      }

      System.out.format("\n");
      for (int row = 0; row < this.size_m; row++) {
        System.out.format("%c\t", CLCSFast.getMChar(row, offset_y));
        for (int col = 0; col < this.size_n; col++) {
          if (getCell(row, col) == -1)
            System.out.format(" X ");
          else
            {
              switch (this.getCellType(row, col)) {
                case TRANS_DIAG:
                  System.out.format(" \\ ");
                  break;
                case TRANS_UP:
                  System.out.format(" ^ ");
                  break;
                case TRANS_LEFT:
                  System.out.format(" < ");
                  break;
            }
          }
        }
        System.out.format("\n");
      }
      System.out.format("\n");
    }
  }

  public boolean isLegalPathCell (int row, int col) {
    if (row < 0 || col < 0)
      return false;
    return row <= this.getMaxRowCell(col);
  }

  public int getCellSafe(int row, int col) {
    if (row < 0 || col < 0)
      return 0;
    if (col > this.getMaxColCell(row) || row > this.getMaxRowCell(col))
      return -1;
    return this.getCell(row, col);
  }

  public void initWindowCells() {
    this.window_frame = new int[this.size_m][this.size_n];
  }

  public int getCell(int row, int col) {
    return (this.window_frame[row][col] >> TRANS_VAL_SHIFT);
  }

  public int getCellType(int row, int col) {
    return this.window_frame[row][col] & TRANSITION_MASK;
  }

  private void setCell(int row, int col, int value, int trans_type) {
    this.window_frame[row][col] = (value << TRANS_VAL_SHIFT) + trans_type;
  }


  public void initMaxArrs() {
    this.col_max = new int[this.size_m];
    this.row_max = new int[this.size_n];
  }

  public void setMaxRowCell(int col, int value) {
    this.row_max[col] = value;
  }

  public int getMaxRowCell(int col) {
    return this.row_max[col];
  }

  public void setMaxColCell(int row, int value) {
    this.col_max[row] = value;
  }

  public int getMaxColCell(int row) {
    return this.col_max[row];
  }

  public void clear() {
    if (CLCSFast.RESET_EACH_ITER) {
      System.out.println("WARNING::: illegal to call clear in submission");

      for (int row = 0; row < this.size_m; row++)
        for (int col = 0; col < this.size_n; col++)
          this.setCell(row, col, -1, TRANS_DIAG);

      for (int i = 0; i < this.size_m; i++)
        this.setMaxColCell(i, -1);

      for (int i = 0; i < this.size_n; i++)
        this.setMaxRowCell(i, -1);

    }
  }

  private int getRowHint (int col) {
    if (col == 0)
      return 0;
    return this.getMaxRowCell(col - 1);
  }

  private int getColHint (int row) {
    if (row == 0)
      return 0;
    return this.getMaxColCell(row - 1);
  }

  public int solve (int offset_y) { 
    int max_col = this.size_n;

    /* get col bounds */
    for (int col = 0; col < max_col; col++) {
      this.setMaxRowCell(col, 
          CLCSFast.blacklist_down.getMaxRow(getRowHint (col), col, offset_y));
    }

    for (int row = 0; row < this.size_m; row++) {

      max_col = CLCSFast.blacklist_up
                              .getMaxCol(row, getColHint(row), offset_y);

      this.setMaxColCell(row, max_col);

      for (int col = 0; col <= max_col; col++) {

        if (row > this.getMaxRowCell(col))
          continue;

        int left = this.getCellSafe(row, col - 1);
        int up   = this.getCellSafe(row - 1, col);
        int diag = this.getCellSafe(row - 1, col - 1);

        assert(left   != CELL_DOESNT_EXIST
              || up   != CELL_DOESNT_EXIST
              || diag != CELL_DOESNT_EXIST);

        int max_type = TRANS_DIAG;
        int max_int  = diag + 1;


        if (diag == CELL_DOESNT_EXIST ||
              max_int < left ||
              !CLCSFast.charMatchAt(row, col, offset_y))
          {
            max_type = TRANS_LEFT;
            max_int  = left;
          }
          
        if (up != CELL_DOESNT_EXIST && max_int < up)
          {
            max_type = TRANS_UP;
            max_int  = up;
          }

          assert(max_int != CELL_DOESNT_EXIST);

        this.setCell(row, col, max_int, max_type);

      }
    }

    return getCell(this.size_m - 1, this.size_n - 1);
  }


}


class Blacklist {

  public static enum BlacklistType {
    NORMAL, TRANSPOSE
  }

  public static enum BlacklistBound {
    ABOVE, BELOW
  }

  private static final int ABOVE_INDEX = 0;
  private static final int BELOW_INDEX = 1; // far greater than the max size
                                                  // of m

  private int[][] blacklist_bounds;
  private int size_m;
  private int size_n;
  private BlacklistType btype;

  public Blacklist (int size_m, int size_n, BlacklistType t) {
    assert (size_m != 0 && size_n != 0);
    this.size_m = 2 * size_m;
    this.size_n = size_n;
    this.btype = t;


    if (this.btype == BlacklistType.NORMAL)
      {
        /* +1 since it is a node map, not an entry map */
        blacklist_bounds = new int[this.size_m]
                          [this.size_n];
      }
    else
      {
        /* +1 since it is a node map, not an entry map */
        blacklist_bounds = new int[this.size_n]
                          [this.size_m];
      }
  }

  public void print () {
    if (CLCSFast.DEBUG) {
      String format = "\n";

      for (int row = 0; row < this.size_m; row++) {
        for (int col = 0; col < this.size_n; col++) {
          if (isInitialized(row, col, 0))
            {
              format += "\t(";
              format += Integer.toString(getBound(row, col, 0, 
                                                        BlacklistBound.ABOVE));
              format += ",";
              format += Integer.toString(getBound(row, col, 0, 
                                                        BlacklistBound.BELOW));
              format += ")";
            }
          else
            {
              format += "\t(-,-)";
            }
        }
        format += "\n";
      }

      System.out.print(format + "\n");
    }
  }

  public void blacklist (int row, int col, int offset_y) {
    if (isValidIndex (row, col, offset_y))
      {
        if (!isInitialized(row, col, offset_y))
          {
            setBound(row, col, offset_y, BlacklistBound.ABOVE);
            setBound(row, col, offset_y, BlacklistBound.BELOW);
          }
        else 
          {
            /* lower bound */
            if (offset_y < getBound(row, col, offset_y, BlacklistBound.ABOVE))
              setBound(row, col, offset_y, BlacklistBound.ABOVE);

            /* upper bound */
            if (offset_y > getBound(row, col, offset_y, BlacklistBound.BELOW))
              setBound(row, col, offset_y, BlacklistBound.BELOW);
          }
      }
  }


  private int getBound (int row, int col, int offset_y, BlacklistBound b) {
    int index = ((b == BlacklistBound.ABOVE) ? ABOVE_INDEX : BELOW_INDEX);
    int r = (this.btype == BlacklistType.NORMAL) ? (row + offset_y) : col;
    int c = (this.btype == BlacklistType.NORMAL) ? col : (row + offset_y);

    return BitMasker.getIndex(index, blacklist_bounds[r][c]) - 1;
  }

  private void setBound (int row, int col, int offset_y, BlacklistBound b) {

    int index = ((b == BlacklistBound.ABOVE) ? ABOVE_INDEX : BELOW_INDEX);
    int r = (this.btype == BlacklistType.NORMAL) ? (row + offset_y) : col;
    int c = (this.btype == BlacklistType.NORMAL) ? col : (row + offset_y);

    blacklist_bounds[r][c] = 
            BitMasker.update(index, blacklist_bounds[r][c], (offset_y + 1));
  }

  private boolean isInitialized (int row, int col, int offset_y) {
    return this.btype == BlacklistType.NORMAL ?
            blacklist_bounds[row + offset_y][col] != 0 :
            blacklist_bounds[col][row + offset_y] != 0;
  }

  private boolean isValidIndex (int row, int col, int offset_y) {
    return ((row + offset_y) < this.size_m)
          && (col < this.size_n);
  }

  public int getMaxCol (int row, int col_hint, int offset_y) {
    for (int col = col_hint; col < this.size_n - 1; col++) {
      if (hasAboveBoundPathNum (row + offset_y, col, offset_y))
        return col - 1;
        
    }
    return this.size_n - 1;
  }

  public int getMaxRow (int row_hint, int col, int offset_y) {
    for (int row = row_hint; row < this.size_m - 1; row++) {
      if (hasBelowBoundPathNum (row, col, offset_y))
        return (row - 1) - offset_y;
        
    }
    return (this.size_m/2) - 1;
  }

  private boolean hasBelowBoundPathNum (int row, int col, int offset_y) {
    /* iterate over all higher bits, check if bits are on, return on index */ 
    if (!isInitialized(row, col, 0))
      return false;
    return (getBound(row, col, 0, BlacklistBound.ABOVE) > offset_y ||
        getBound(row, col, 0, BlacklistBound.BELOW) > offset_y);
  }

  private boolean hasAboveBoundPathNum (int row, int col, int offset_y) {
    /* iterate over all lower bits, check if bits are on in any */
    if (!isInitialized(row, col, 0))
      return false;
    return (getBound(row, col, 0, BlacklistBound.ABOVE) < offset_y ||
        getBound(row, col, 0, BlacklistBound.BELOW) < offset_y);
  }
}



class BitMasker {
  public static int ZERO = 0;
  public static int ONE = 1;

  private static final int ZERO_SHIFT = 0;
  private static final int ONE_SHIFT  = 16; /* split 32 in half */


  private static final int MASK_SHOW_LOWER = ((1<<ONE_SHIFT) - 1);
  private static final int MASK_HIDE_LOWER = (-1)^((1<<ONE_SHIFT) - 1);


  private static final int MASK_SHOW_ZERO = MASK_SHOW_LOWER;
  private static final int MASK_HIDE_ZERO = MASK_HIDE_LOWER;

  private static final int MASK_SHOW_ONE  = MASK_HIDE_LOWER;
  private static final int MASK_HIDE_ONE  = MASK_SHOW_LOWER;

  public static int getIndex (int index, int storage_value) {
    int shift = (index == ZERO) ? ZERO_SHIFT : ONE_SHIFT;
    return (storage_value >> shift) & MASK_SHOW_LOWER;
  }

  public static int update (int index, int storage_value, int insert_value) {
    int shift = (index == ZERO) ? ZERO_SHIFT : ONE_SHIFT;
    int mask  = (index == ZERO) ? MASK_HIDE_ZERO : MASK_HIDE_ONE;
    storage_value &= mask; // remove old bits in section
    return (storage_value | (insert_value << shift));
  }
}