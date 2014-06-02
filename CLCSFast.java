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
    int node_row     = M.length;
    int node_col     = N.length;

    while(true) {
      if (node_row == 0 && node_col == 0)
        {
          return;
        }

      if (node_row == 0)
        {
          node_col--;
          blacklist_down.blacklist(node_row + 1, node_col, offset_y);
          continue;
        }

      if (node_col == 0)
        {
          node_row--;
          blacklist_up.blacklist(node_row, node_col + 1, offset_y);
          continue;         
        }

      int row = node_row - 1;
      int col = node_col - 1;


      if (charMatchAt(row, col, offset_y) 
            && window.isLegalPathCell(row - 1, col - 1))
        {
          node_row--;
          node_col--;

          blacklist_up.blacklist(node_row, node_col + 1, offset_y);
          blacklist_down.blacklist(node_row + 1, node_col, offset_y);
          continue;          
        }

      if (window.getCell(row, col - 1, offset_y) 
            == window.getCell(row, col, offset_y))
        {
          node_col--;
          blacklist_down.blacklist(node_row + 1, node_col, offset_y);
        }
      else
        {
          node_row--;
          blacklist_up.blacklist(node_row, node_col + 1, offset_y);
        }
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
    updateBlacklists (M.length);

    blacklist_up.print();
    blacklist_down.print();

    findShortestPaths (1, M.length);


    return max_path;
  }




  public static void main(String[] args) {

    boolean eclipse = true;
     
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
  private short [][] window_frame;
  private int size_m;
  private int size_n;

  private short[] row_max;
  private short[] col_max;


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
      System.out.format("\n");
    }
  }

  public boolean isLegalPathCell (int row, int col) {
    if (row < 0 || col < 0)
      return false;
    return row <= this.getMaxRowCell(col);
  }

  public int getCell(int row, int col, int offset_y) {
    if (row < 0 || col < 0)
      return 0;
    if (col > this.getMaxColCell(row))
      return this.getCell(row, this.getMaxColCell(row));
    if (row > this.getMaxRowCell(col))
      return this.getCell(this.getMaxRowCell(col),col);
    return this.getCell(row, col);
  }

  public void initWindowCells() {
    this.window_frame = new short[this.size_m][this.size_n];
  }

  public int getCell(int row, int col) {
    return (int) this.window_frame[row][col];
  }

  public void setCell(int row, int col, int value) {
    this.window_frame[row][col] = (short)value;
  }


  public void initMaxArrs() {
    this.col_max = new short[this.size_m];
    this.row_max = new short[this.size_n];
  }

  public void setMaxRowCell(int col, int value) {
    this.row_max[col] = (short)value;
  }

  public int getMaxRowCell(int col) {
    return (int)this.row_max[col];
  }

  public void setMaxColCell(int row, int value) {
    this.col_max[row] = (short)value;
  }

  public int getMaxColCell(int row) {
    return (int)this.col_max[row];
  }

  public void clear() {
    if (CLCSFast.RESET_EACH_ITER) {
      System.out.println("WARNING::: illegal to call clear in submission");

      for (int row = 0; row < this.size_m; row++)
        for (int col = 0; col < this.size_n; col++)
          setCell(row, col, -1);

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

        setCell(row, col, Math.max(this.getCell(row - 1, col, offset_y),
                                    this.getCell(row, col - 1, offset_y)));

        if (CLCSFast.charMatchAt(row, col, offset_y))
          setCell(row, col, Math.max(
                                this.getCell(row, col, offset_y),
                                this.getCell(row - 1, col - 1, offset_y) + 1));
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

  private static int NUM_BOUNDS = 2;

  private short[][][] blacklist_bounds;
  private int size_m;
  private int size_n;
  private int size_nodes_m;
  private int size_nodes_n;
  private BlacklistType btype;

  public Blacklist (int size_m, int size_n, BlacklistType t) {
    assert (size_m != 0 && size_n != 0);
    this.size_m = size_m;
    this.size_n = size_n;
    this.size_nodes_m = this.size_m * 2 + 1;
    this.size_nodes_n = this.size_n + 1;
    this.btype = t;


    if (this.btype == BlacklistType.NORMAL)
      {
        /* +1 since it is a node map, not an entry map */
        blacklist_bounds = new short[this.size_nodes_m]
                          [this.size_nodes_n][NUM_BOUNDS];
      }
    else
      {
        /* +1 since it is a node map, not an entry map */
        blacklist_bounds = new short[this.size_nodes_n]
                          [this.size_nodes_m][NUM_BOUNDS];
      }
  }

  public void print () {
    if (CLCSFast.DEBUG) {
      String format = "";

      for (int row = 0; row < this.size_nodes_m; row++) {
        for (int col = 0; col < this.size_nodes_n; col++) {
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

      System.out.print(format);
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
    int index = (b == BlacklistBound.ABOVE) ? 0 : 1;
    int r = (this.btype == BlacklistType.NORMAL) ? (row + offset_y) : col;
    int c = (this.btype == BlacklistType.NORMAL) ? col : (row + offset_y);

    return (short)(blacklist_bounds[r][c][index] - 1);
  }

  private void setBound (int row, int col, int offset_y, BlacklistBound b) {
    int index = (b == BlacklistBound.ABOVE) ? 0 : 1;
    int r = (this.btype == BlacklistType.NORMAL) ? (row + offset_y) : col;
    int c = (this.btype == BlacklistType.NORMAL) ? col : (row + offset_y);

    blacklist_bounds[r][c][index] = (short)(offset_y + 1);
  }

  private boolean isInitialized (int row, int col, int offset_y) {
    return  this.btype == BlacklistType.NORMAL ?
            blacklist_bounds[row + offset_y][col][0] != (short)0 :
            blacklist_bounds[col][row + offset_y][0] != (short)0;
  }

  private boolean isValidIndex (int row, int col, int offset_y) {
    return ((row + offset_y) < this.size_nodes_m)
          && (col < this.size_nodes_n);
  }

  public int getMaxCol (int row, int col_hint, int offset_y) {
    for (int col = col_hint; col < this.size_nodes_n; col++) {
      if (getAboveBoundPathNum (row + offset_y, col, offset_y) != -1)
        return col - 1;
        
    }
    return this.size_n - 1;
  }

  public int getMaxRow (int row_hint, int col, int offset_y) {
    for (int row = row_hint; row < this.size_nodes_m; row++) {
      if (getBelowBoundPathNum (row, col, offset_y) != -1)
        return (row - 1) - offset_y;
        
    }
    return this.size_m - 1;
  }

  private int getBelowBoundPathNum (int row, int col, int offset_y) {
    /* iterate over all higher bits, check if bits are on, return on index */ 
    if (!isInitialized(row, col, 0))
      return -1;
    if (getBound(row, col, 0, BlacklistBound.ABOVE) > offset_y)
      return getBound(row, col, 0, BlacklistBound.ABOVE);
    if (getBound(row, col, 0, BlacklistBound.BELOW) > offset_y)
      return getBound(row, col, 0, BlacklistBound.BELOW);
    return -1;
  }

  private int getAboveBoundPathNum (int row, int col, int offset_y) {
    /* iterate over all lower bits, check if bits are on in any */
    if (!isInitialized(row, col, 0))
      return -1;
    if (getBound(row, col, 0, BlacklistBound.ABOVE) < offset_y)
      return getBound(row, col, 0, BlacklistBound.ABOVE);
    if (getBound(row, col, 0, BlacklistBound.BELOW) < offset_y)
      return getBound(row, col, 0, BlacklistBound.BELOW);
    return -1;
  }
}