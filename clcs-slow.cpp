#include <algorithm>
#include <iostream>
#include <string>

using namespace std;

/* 
    GROUP: 
      Laura  Hunter  : lmhunter : 005699511
      Joseph Delgado : delgadoj : 005687435
*/


typedef struct
{
  int row;
  int col;
} position;


string A, B;
string M, N;
int len_m, len_n;

int clcs_run ();
int get (position p, int *frame);
void set (position p, int value, int *frame);
bool is_char_match (position p, int offset);
int get_max_sibling (position p, int offset, int *frame);


int main () {

  int T;
  cin >> T;
  for (int tc = 0; tc < T; tc++) {
    cin >> A >> B;

    int len_a = A.length();
    int len_b = B.length();

    M     = (len_a > len_b) ? B : A;
    N     = (len_a > len_b) ? A : B;
    len_m = (len_a > len_b) ? len_b : len_a;
    len_n = (len_a > len_b) ? len_a : len_b;

    cout << clcs_run() << endl;
  }
  return 0;
}


int clcs_run ()
{

  int max_score = -1;
  int frame[len_m*len_n];

  for (int offset = 0; offset < len_m; ++offset)
    {
      position p = (position){0,0};

      for (p.row = 0; p.row < len_m; ++p.row) 
              /* virtual row (offest from starting point) */
        {
          for (p.col = 0; p.col < len_n; ++p.col)
            {

              int m = get_max_sibling (p, offset, frame);
              set (p, m, frame);

            }
        }
        position bottom_left = (position){len_m - 1, len_n - 1};
        max_score =  max (max_score, get(bottom_left, frame));
    }

    return max_score;
}

int get (position p, int *frame)
{
  if (p.row < 0 || p.col < 0)
    return 0;
  else
    return frame[p.row*len_n + p.col];
}

void set (position p, int value, int *frame)
{
  frame[p.row*len_n + p.col] = value;
}

bool is_char_match (position p, int offset)
{
  return M[(p.row + offset) % len_m] == N[p.col];
}

int get_max_sibling (position p, int offset, int *frame)
{
  position left = (position){p.row, p.col - 1};
  position up   = (position){p.row - 1, p.col};
  position diag = (position){p.row - 1, p.col - 1};

  int max_sib = max (get (left, frame), get (up, frame));

  if (is_char_match(p, offset))
    return max (max_sib, get (diag, frame) + 1);
  return max_sib;
}