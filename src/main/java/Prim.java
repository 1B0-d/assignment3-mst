import java.util.*;

public class Prim {

    public static AlgoResult findMST(Graph graph, String start) {
        if (graph == null || graph.isEmpty()) return new AlgoResult(new ArrayList<>(), 0);

        if (start == null || !graph.getVertices().contains(start)) {
            Iterator<String> it = graph.getVertices().iterator();
            if (!it.hasNext()) return new AlgoResult(new ArrayList<>(), 0);
            start = it.next();
        }

        Set<String> vis = new HashSet<>();
        List<Edge> mst = new ArrayList<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));

        long ops = 0;

        vis.add(start);
        pq.addAll(graph.neighbors(start)); ops += graph.neighbors(start).size();

        while (!pq.isEmpty() && mst.size() < graph.getVertices().size() - 1) {
            Edge e = pq.poll(); ops++;
            if (vis.contains(e.to)) continue;
            mst.add(e);
            vis.add(e.to);
            for (Edge nxt : graph.neighbors(e.to)) {
                if (!vis.contains(nxt.to)) { pq.add(nxt); ops++; }
            }
        }
        return new AlgoResult(mst, ops);
    }
}
