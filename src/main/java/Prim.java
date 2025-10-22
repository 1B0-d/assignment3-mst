
import java.util.*;

public class Prim {
    public static AlgoResult findMST(Graph g, String start) {
        if (g == null || g.isEmpty()) return new AlgoResult(new ArrayList<>(), 0);
        if (start == null || !g.getVertices().contains(start)) start = g.getVertices().iterator().next();

        Set<String> vis = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));
        List<Edge> mst = new ArrayList<>();

        long ops = 0;
        vis.add(start);
        for (Edge e : g.neighbors(start)) { pq.add(e); ops++; }

        while (!pq.isEmpty() && mst.size() < g.getVertices().size() - 1) {
            Edge e = pq.poll(); ops++;
            if (vis.contains(e.to)) continue;
            mst.add(e);
            vis.add(e.to);
            for (Edge nx : g.neighbors(e.to)) if (!vis.contains(nx.to)) { pq.add(nx); ops++; }
        }
        return new AlgoResult(mst, ops);
    }
}
