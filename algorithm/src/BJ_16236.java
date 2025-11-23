import java.sql.Array;
import java.util.*;

public class BJ_16236 {

    // 방향
    static int[] dx = {-1, 0, 1, 0};
    static int[] dy = {0, -1, 0, 1};

    // 위치 반환
    static class Pos {
        int x, y, dist;

        Pos(int x, int y, int dist) {
            this.x = x;
            this.y = y;
            this.dist = dist;
        }
    }

    static class Fish {
        int x, y, dist;

        Fish(int x, int y, int dist) {
            this.x = x;
            this.y = y;
            this.dist = dist;
        }
    }

    // Main
    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);

        int N = sc.nextInt();
        sc.nextLine();  // 버퍼 지우기
        int[][] arr = new int[N][N];

        int sharkX = -1;
        int sharkY = -1;

        for (int i=0; i<N; i++) {
            String input = sc.nextLine();
            String[] tokens = input.split(" ");

            for (int j=0; j<N; j++) {
                arr[i][j] = Integer.parseInt(tokens[j]);

                if (arr[i][j] == 9) {
                    sharkX = i;
                    sharkY = j;
                    arr[i][j] = 0;
                }
            }
        }

        int sharkSize = 2;
        int eatCount = 0;
        int totalTime = 0;

        while (true) {
            // 먹을 수 있는 가장 가까운 물고기 찾기
            Fish target = findNearestFish(arr, N, sharkX, sharkY, sharkSize);

            if (target == null) {
                // 더 이상 먹을 수 있는 물고기가 없음
                break;
            }

            // 물고기 먹기
            totalTime += target.dist;
            sharkX = target.x;
            sharkY = target.y;
            arr[sharkX][sharkY] = 0;
            eatCount++;

            // 상어 크키 증가 체크
            if (eatCount == sharkSize) {
                sharkSize++;
                eatCount = 0;
            }
        }

        System.out.println(totalTime);
    }

    // 먹을 수 있는 가장 가까운 물고기 찾기
    static Fish findNearestFish(int[][] arr, int N, int startX, int startY, int sharkSize) {
        boolean[][] visited = new boolean[N][N];
        Queue<Pos> q = new LinkedList<>();
        List<Fish> fishes = new ArrayList<>();

        q.offer(new Pos(startX, startY, 0));
        visited[startX][startY] = true;

        while (!q.isEmpty()) {
            Pos cur = q.poll();
            int curX = cur.x;
            int curY = cur.y;
            int curDist = cur.dist;

            // 현재 위치에 먹을 수 있는 물고기가 있는지 확인
            if (arr[curX][curY] > 0 && arr[curX][curY] < sharkSize) {
                fishes.add(new Fish(curX, curY, curDist));
            }

            // 4방향 탐색
            for (int i=0; i<4; i++) {
                int nx = curX + dx[i];
                int ny = curY + dy[i];

                if (nx >= 0 && nx < N && ny >= 0 && ny < N && !visited[nx][ny]) {
                    // 지나갈 수 있는 칸인지 확인 (빈칸이거나 상어보다 작거나 같은 물고기)
                    if (arr[nx][ny] == 0 || arr[nx][ny] <= sharkSize) {
                        visited[nx][ny] = true;
                        q.offer(new Pos(nx, ny, curDist + 1));
                    }
                }
            }
        }

        if (fishes.isEmpty()) {
            return null;  // 먹을 수 있는 물고기가 없음
        }

        // 가장 가까운 물고기 선택 (거리 -> 위쪽 -> 왼쪽 우선순위)
        fishes.sort((f1, f2) -> {
            if (f1.dist != f2.dist) return f1.dist - f2.dist;  // 거리 우선
            if (f1.x != f2.x) return f1.x - f2.x;  // 위쪽 우선
            return f1.y - f2.y;
        });

        return fishes.get(0);
    }
}
