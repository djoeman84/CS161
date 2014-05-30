import java.util.*;
class CLCSFast {

  static int getCell (int row, int col) {
    return 1;
  }

  static int SolveCLCSFast (char[] A, char[] B) {
    char [] M = (A.length > B.length) ? B : A;
    char [] N = (A.length > B.length) ? A : B;

    int[][] window = new int[M.length][N.length];

    for (int row = 0; row < M.length; row++) {
      for (int col = 0; col < N.length; col++) {

        System.out.format("M[%d] = %c, N[%d] = %c\n", row, M[row], col, N[col]);
      }
    }


    // 

    return 2;
  }




  public static void main(String[] args) {
    char[] A, B;
    Scanner s = new Scanner(System.in);
    int T = s.nextInt();
    for (int tc = 0; tc < T; tc++) {
      A = s.next().toCharArray();
      B = s.next().toCharArray();
      SolveCLCSFast (A, B);
    }
  }


}


class Blacklist {
  public static int helloWorld () {
    return 1;
  }
}