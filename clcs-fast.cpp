#include <algorithm>
#include <iostream>
#include <string>

using namespace std;

/* 
    GROUP: 
      Laura  Hunter  : lmhunter : 005699511
      Joseph Delgado : delgadoj : 005687435
*/


#define NO_BOUND NULL
#define TRANS_SHIFT 2
#define TRANS_SHOW_MASK ((1 << TRANS_SHIFT) - 1)
#define TRANS_HIDE_MASK (~TRANS_SHOW_MASK)

#define TRANS_TYPE_NONE 0
#define TRANS_TYPE_LEFT 1
#define TRANS_TYPE_DOWN 2
#define TRANS_TYPE_DIAG 3


#define PRINT_DEBUG false
#define CLEAR_EACH_TIME false


/* 
  ::: NOTE ON UPPER AND LOWER :::
    - upper > lower (by index value in dp_window)
  */

int dp_window[2048][2048];
string A, B;

typedef struct
{
  int row;
  int col;
} position;

typedef struct
{
  int run_index;
  int *row_bounds;
} bound;

typedef struct
{
  int value;
  int type;
} transition_cell;

string M, N;
int len_m, len_n;

int clcs_run ();
int clcs_solve (bound upper, bound lower);
int clcs_populate_dp (int run_index, bound upper, bound lower);
void clcs_populate_row_bound (int *row_bounds);

inline bool is_char_match_at (position p, int run_index);
inline bool is_valid_cell (position p, int run_index, bound upper, bound lower);

inline int get_window_entry_value_safe (position p);
inline int get_window_entry_value (position p);
inline int get_window_entry_shift_type (position p);
inline transition_cell get_window_entry (position p);
inline void set_window_entry (position p, transition_cell c);
inline transition_cell find_max_transition (position p, int run_index,
          bound upper, bound lower);


inline int get_col_min (int run_index, int row, bound lower);
inline int get_col_max (int run_index, int row, bound upper);

inline void set_col_min (int row, int *arr, int value);
inline void set_col_max (int row, int *arr, int value);


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
  bound no_bound = {0, NO_BOUND};
  int maximum = clcs_populate_dp (0, no_bound, no_bound);

  int first_row_bounds[len_m];

  clcs_populate_row_bound (first_row_bounds);
  bound lower_bound = {0, first_row_bounds};
  bound upper_bound = {len_m, first_row_bounds};

  return max (maximum, clcs_solve (upper_bound, lower_bound));
}


int clcs_solve (bound upper, bound lower)
{
  int run_index = (upper.run_index + lower.run_index) / 2;

  if (upper.run_index - lower.run_index <= 1)
    return -1;

  int score = clcs_populate_dp (run_index, upper, lower);

  int mid_arr[len_m];

  clcs_populate_row_bound (mid_arr);
  bound mid_bound = (bound){run_index, mid_arr};

  int child_max = -1;

  if (run_index - 1 != lower.run_index)
    child_max = max (clcs_solve (mid_bound, lower), child_max);
  if (run_index + 1 != upper.run_index)
    child_max = max (clcs_solve (upper, mid_bound), child_max);

  return max (child_max, score);
}


int clcs_populate_dp (int run_index, bound upper, bound lower)
{
  int last_max_col = len_m - 1;
  int last_min_col = 0;

  position p = {0,0};

  for (p.row = 0; p.row < len_m; ++p.row)
    {
      int min_col = get_col_min (run_index, p.row, upper);
      int max_col = get_col_max (run_index, p.row, lower);

      for (p.col = min_col; p.col <= max_col; ++p.col)
        {
          int max_entry = -1;
          int max_entry_type = TRANS_TYPE_NONE;

          transition_cell t = 
                  find_max_transition (p, run_index, upper, lower);

          set_window_entry (p, t);

        }
    }

    return get_window_entry_value((position){len_m - 1, len_n - 1});
}

void clcs_populate_row_bound (int *row_bounds)
{

  position p = (position){len_m - 1, len_n - 1};

  set_col_min (p.row, row_bounds, p.col);
  set_col_max (p.row, row_bounds, p.col);

  while (p.row != 0 || p.col != 0)
    {
      if (p.row == 0)
        {
          p.col --;
          set_col_min (p.row, row_bounds, p.col);
        }
      else if (p.col == 0)
        {
          p.row --;
          set_col_min (p.row, row_bounds, p.col);
          set_col_max (p.row, row_bounds, p.col);
        }
      else
        {
          switch (get_window_entry_shift_type (p))
            {
              case (TRANS_TYPE_LEFT):
                p.col --;
                set_col_min (p.row, row_bounds, p.col);
                break;
              case (TRANS_TYPE_DIAG):
                p.col --;
                /* no break, continue into TRANS_TYPE_DOWN */
              case (TRANS_TYPE_DOWN):
                p.row --;
                set_col_min (p.row, row_bounds, p.col);
                set_col_max (p.row, row_bounds, p.col);
                break;
            }
        }
    }

}


inline transition_cell find_max_transition (position p, int run_index,
          bound upper, bound lower)
{
  bool is_min_col = false;
  transition_cell t = {-1, TRANS_TYPE_NONE};

  position diag = {p.row - 1, p.col - 1};
  position up   = {p.row - 1, p.col    };
  position left = {p.row    , p.col - 1};

  if (is_valid_cell (left, run_index, upper, lower))
    {
      int val = get_window_entry_value_safe (left);
      if (val > t.value)
        {
          t.value = val;
          t.type = TRANS_TYPE_LEFT;
        }
    }

  if (is_valid_cell (up, run_index, upper, lower))
    {
      int val = get_window_entry_value_safe (up);
      if (val > t.value)
        {
          t.value = val;
          t.type = TRANS_TYPE_DOWN;
        }
    }


  if (is_char_match_at (p, run_index) 
          && is_valid_cell (diag, run_index, upper, lower))
    {
      t.value = get_window_entry_value_safe (diag) + 1;
      t.type = TRANS_TYPE_DIAG;
    }

  return t;
}








inline int get_window_entry_value_safe (position p)
{
  if (p.row == -1 || p.col == -1)
    return 0;
  return dp_window[p.row][p.col] >> TRANS_SHIFT;
}

inline int get_window_entry_value (position p)
{
  return dp_window[p.row][p.col] >> TRANS_SHIFT;
}

inline int get_window_entry_shift_type (position p)
{
  return (dp_window[p.row][p.col] & TRANS_SHOW_MASK);
}

inline transition_cell get_window_entry (position p)
{
  int val = dp_window[p.row][p.col];
  return (transition_cell)
    {
      (val >> TRANS_SHIFT),
      (val & TRANS_SHOW_MASK)
    };
}

inline void set_window_entry (position p, transition_cell c)
{
  dp_window[p.row][p.col] = 
        (((c.value << TRANS_SHIFT) & TRANS_HIDE_MASK) | c.type);
}



inline bool is_char_match_at (position p, int run_index)
{
  return (M[(p.row + run_index) % len_m] == N[p.col]);
}

inline bool is_valid_cell (position p, int run_index, bound upper, bound lower)
{
  if (p.row < 0 || p.col < 0)
    return true;
  return p.col >= get_col_min(run_index, p.row, upper) &&
         p.col <= get_col_max(run_index, p.row, lower);
}





#define MIN_SHIFT  0
#define MAX_SHIFT 16


#define MASK_SHOW_LOWER ((1<<MAX_SHIFT) - 1)
#define MASK_HIDE_LOWER (-1)^((1<<MAX_SHIFT) - 1)


#define MASK_SHOW_MIN MASK_SHOW_LOWER
#define MASK_HIDE_MIN MASK_HIDE_LOWER

#define MASK_SHOW_MAX MASK_HIDE_LOWER
#define MASK_HIDE_MAX MASK_SHOW_LOWER

inline int get_col_min (int run_index, int row, bound upper)
{
  if (upper.row_bounds == NO_BOUND)
    return 0;

  int upper_index = row - (upper.run_index - run_index);
  if (upper_index >= 0)
    return (upper.row_bounds[upper_index] >> MIN_SHIFT) & MASK_SHOW_LOWER;

  return 0;
}
inline int get_col_max (int run_index, int row, bound lower)
{
  if (lower.row_bounds == NO_BOUND)
    return len_n - 1;


  /* corresponding row in lower bound array */
  int lower_index = row + (run_index - lower.run_index);
  if (lower_index < len_m)
    return (lower.row_bounds[lower_index] >> MAX_SHIFT) & MASK_SHOW_LOWER;

  return len_n - 1;
}



inline void set_col_min (int row, int *arr, int value)
{
  arr[row] &= (MASK_HIDE_MIN); // remove old bits in section
  arr[row] |= (value << MIN_SHIFT);
}

inline void set_col_max (int row, int *arr, int value)
{
  arr[row] &= (MASK_HIDE_MAX); // remove old bits in section
  arr[row] |= (value << MAX_SHIFT);
}









