
import java.util.*;

public class Kruskal {
    public static AlgoResult findMST(Graph g) {
        List<String> vlist = g.getVertexList();
        int n = vlist.size();

        List<int[]> edgesInt = g.getEdgesUniqueInt();
        int m = edgesInt.size();
        int[][] edges = new int[m][3];
        for (int i = 0; i < m; i++) {
            int[] e = edgesInt.get(i);
            edges[i][0] = e[0];
            edges[i][1] = e[1];
            edges[i][2] = e[2];
        }

        final long[] sortOps = {0};
        Arrays.sort(edges, (a,b)->{ sortOps[0]++; return Integer.compare(a[2], b[2]); });

        int[] parent = new int[n];
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        List<Edge> mst = new ArrayList<>(n - 1);
        long ops = sortOps[0];

        for (int i = 0; i < m && mst.size() < n - 1; i++) {
            int u = edges[i][0], v = edges[i][1], w = edges[i][2];
            int ru = find(parent, u), rv = find(parent, v); ops += 2;
            if (ru != rv) {
                if (rank[ru] < rank[rv]) parent[ru] = rv;
                else if (rank[ru] > rank[rv]) parent[rv] = ru;
                else { parent[rv] = ru; rank[ru]++; }
                mst.add(new Edge(vlist.get(u), vlist.get(v), w));
                ops++;
            }
        }
        return new AlgoResult(mst, ops);
    }

    private static int find(int[] p, int x) {
        while (p[x] != x) { p[x] = p[p[x]]; x = p[x]; }
        return x;
    }
}
