import java.util.*;
import java.io.*;

public class Main {
    static int N, M, P, C, D, rudR, rudC;
    static int[][] board, ds = {{1,0}, {0,-1}, {-1,0}, {0,1}};
    static int[][] rds = {{1,1}, {1,0}, {1,-1}, {0,1}, {0,-1}, {-1,1}, {-1,0}, {-1,-1}};
    static Queue<int[]> q = new LinkedList<>();
    static int[] santaScores;
    static Map<Integer, int[]> santas = new HashMap<>();
    static Set<Integer> stuns = new HashSet<>(), nextStuns = new HashSet<>(); 
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());

        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        P = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        D = Integer.parseInt(st.nextToken());
        board = new int[N][N];
        santaScores = new int[P+1];

        st = new StringTokenizer(br.readLine());
        rudR = Integer.parseInt(st.nextToken()) - 1; 
        rudC = Integer.parseInt(st.nextToken()) - 1;
        board[rudR][rudC] = -1;

        for (int i = 0; i < P; i++) {
            st = new StringTokenizer(br.readLine());
            int n = Integer.parseInt(st.nextToken());
            int r = Integer.parseInt(st.nextToken()) - 1;
            int c = Integer.parseInt(st.nextToken()) - 1;

            board[r][c] = n;
            santas.put(n, new int[]{r, c});
        }

        for (int i = 0; i < M; i++) {
            stuns = nextStuns;
            nextStuns = new HashSet<>();
            if (santas.keySet().size() == 0) break;
            moveRud();
            for (int j = 1; j <= P; j++) {
                if (santas.containsKey(j)) {
                    moveSanta(j);
                }
            }
            
            for (int n : santas.keySet()) {
                santaScores[n]++;
            }
        }

        String answer = "";
        for (int i = 0; i < P; i++) {
            answer += santaScores[i+1] + " ";
        }
        System.out.println(answer);                
    }

    private static void moveRud() {
        int[] nearSanta = getNearSanta();
        int dr = nearSanta[0] > rudR? 1 : nearSanta[0] == rudR? 0 : -1;
        int dc = nearSanta[1] > rudC? 1 : nearSanta[1] == rudC? 0 : -1;
        board[rudR][rudC] = 0;
        rudR += dr;
        rudC += dc;

        if (board[rudR][rudC] != 0) {
            rudCrush(board[rudR][rudC], dr, dc);
            nextStuns.add(board[rudR][rudC]);
        }
        board[rudR][rudC] = -1;
    }

    private static int[] getNearSanta() {
        double min = Double.MAX_VALUE;
        int[] minSanta = null;
        for (int s : santas.keySet()) {
            int[] curr = santas.get(s);
            double currDis = Math.pow((curr[0] - rudR), 2) + Math.pow((curr[1] - rudC), 2);
            
            if (currDis < min) {
                min = currDis;
                minSanta = curr;
            } else if (currDis == min) {
                if (curr[0] > minSanta[0]) {
                    min = currDis;
                    minSanta = curr;
                } else if (curr[0] == minSanta[0] && curr[1] > minSanta[1]) {
                    minSanta = curr;
                    min = currDis;
                }
            }
        }

        return minSanta;
    }

    private static void moveSanta(int santaNum) {
       if (stuns.contains(santaNum) || nextStuns.contains(santaNum)) return;
       int[] curr = santas.get(santaNum);
       
       double dis = Math.pow((curr[0] - rudR), 2) + Math.pow((curr[1] - rudC), 2);
       double minDis = Double.MAX_VALUE;
       int minD = -1;

       for (int i = 0; i < 4; i++) {
        int nr = curr[0] + ds[i][0];
        int nc = curr[1] + ds[i][1];

        if (nr < 0 || nr >= N || nc < 0 || nc >= N || board[nr][nc] > 0) continue;
        double currDis = Math.pow((nr - rudR), 2) + Math.pow((nc - rudC), 2);
        if (currDis < minDis) {
            minDis = currDis;
            minD = i;

            if (currDis == 0.0) break;
        }
       }
       
       if (minDis < dis) {
        int nr = curr[0] + ds[minD][0];
        int nc = curr[1] + ds[minD][1];
        if (board[nr][nc] == -1) {
            santaCrush(santaNum, ds[minD][0], ds[minD][1]);
            nextStuns.add(santaNum);
            return;
        }
        
        board[curr[0]][curr[1]] = 0;
        board[nr][nc] = santaNum;
        santas.put(santaNum, new int[]{nr, nc});
       }

    }

    private static void rudCrush(int santaNum, int dr, int dc) {
        santaScores[santaNum] += C;
        int[] curr = santas.get(santaNum);
        int nr = curr[0] + (C * dr);
        int nc = curr[1] + (C * dc);

        if (nr < 0 || nr >= N || nc < 0 || nc >= N) {
            santas.remove(santaNum);
            return;
        }

        if (board[nr][nc] != 0) {
            react(nr, nc, dr, dc);
            return;
        }

        board[nr][nc] = santaNum;
        santas.put(santaNum, new int[]{nr, nc});
    }

    private static void santaCrush(int santaNum, int dr, int dc) {
        santaScores[santaNum] += D;
        int[] curr = santas.get(santaNum);
        int nr = curr[0] + (dr * -1 * (D-1));
        int nc = curr[1] + (dc * -1 * (D-1));

        if (nr < 0 || nr >= N || nc < 0 || nc >= N) {
            santas.remove(santaNum);
            board[curr[0]][curr[1]] = 0;
            return;
        }

        if (!(nr == curr[0] && nc == curr[1]) && board[nr][nc] > 0) {
            react(curr[0], curr[1], dr*-1, dc*-1);
            return;
        }

        board[curr[0]][curr[1]] = 0;
        board[nr][nc] = santaNum;
        santas.put(santaNum, new int[]{nr, nc});
    }

    private static void react(int r, int c, int dr, int dc) {
        int nr = r;
        int nc = c;
        int prevVal = board[r][c]; 
        while(true) {
            nr += dr;
            nc += dc;

            if (nr < 0 || nr >= N || nc < 0 || nc >= N) {
                if (prevVal != 0) santas.remove(prevVal);
                break;
            }

            int tmp = board[nr][nc];
            board[nr][nc] = prevVal;
            santas.put(prevVal, new int[]{nr, nc});
            prevVal = tmp;            
        }
        board[r][c] = 0;
    }
}
