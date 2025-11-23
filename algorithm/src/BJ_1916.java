import java.util.*;

public class BJ_1916 {

    static ArrayList<ArrayList<Pos>> graph = new ArrayList<>();
    static boolean[] visited;

    static class Pos {
        int node, cost;

        Pos(int node, int cost) {
            this.node = node;
            this.cost = cost;
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int N = sc.nextInt();
        int M = sc.nextInt();

        for (int i=0; i<=N; i++) {
            graph.add(new ArrayList<>());
        }

        for (int i=0; i<M; i++) {
            int start = sc.nextInt();
            int end = sc.nextInt();
            int cost = sc.nextInt();

            graph.get(start).add(new Pos(end, cost));
        }

        int u = sc.nextInt();
        int v = sc.nextInt();

        int answer = dijkstra(N, u, v);

        System.out.println(answer);
    }

    static int dijkstra(int N, int start, int v) {

        PriorityQueue<Pos> pq = new PriorityQueue<>((a, b) -> a.cost - b.cost);
        int[] distArr = new int[N+1];
        Arrays.fill(distArr, Integer.MAX_VALUE);
        visited = new boolean[N+1];

        distArr[start] = 0;
        pq.offer(new Pos(start, 0));

        while (!pq.isEmpty()) {
            Pos pos = pq.poll();

            int curNode = pos.node;
            int curCost = pos.cost;

            if (!visited[curNode]) {
                visited[curNode] = true;

                for (Pos next : graph.get(curNode)) {
                    if (distArr[next.node] > next.cost + distArr[curNode]) {
                        distArr[next.node] = next.cost + distArr[curNode];
                        pq.add(new Pos(next.node, distArr[next.node]));
                    }
                }
            }
        }

        return distArr[v];
    }
}