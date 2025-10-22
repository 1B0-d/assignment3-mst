
import java.util.*;
import java.util.stream.*;

public class Kruskal {

    public static AlgoResult findMST(Graph g) {
        List<String> vsList = new ArrayList<>(g.getVertices());
        int n = vsList.size();
        Map<String,Integer> idx = new HashMap<>(n*2);
        for (int i = 0; i < n; i++) idx.put(vsList.get(i), i);

        List<Edge> src = g.getEdgesUnique();
        Edge[] edges = src.toArray(new Edge[0]);
        final long[] sortOps = {0};
        Arrays.sort(edges, (a,b)->{ sortOps[0]++; return Integer.compare(a.weight, b.weight); });

        int[] parent = new int[n], rank = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        List<Edge> mst = new ArrayList<>(Math.max(0, n-1));
        long ops = sortOps[0];

        for (Edge e : edges) {
            int u = idx.get(e.from), v = idx.get(e.to);
            int ru = find(parent, u), rv = find(parent, v); ops += 2;
            if (ru != rv) {
                if (rank[ru] < rank[rv]) parent[ru] = rv;
                else if (rank[ru] > rank[rv]) parent[rv] = ru;
                else { parent[rv] = ru; rank[ru]++; }
                mst.add(e); ops++;
                if (mst.size() == n - 1) break;
            }
        }
        return new AlgoResult(mst, ops);
    }

    private static int find(int[] p, int x) {
        while (p[x] != x) { p[x] = p[p[x]]; x = p[x]; }
        return x;
    }
}
