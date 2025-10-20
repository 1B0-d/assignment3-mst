

import java.util.*;

public class Prim {

    public static List<Edge> findMST(Graph graph, String start) {
        List<Edge> mst = new ArrayList<>();
        if (graph == null || graph.isEmpty()) return mst;

        if (start == null || !graph.hasVertex(start)) {
            Iterator<String> it = graph.getVertices().iterator();
            if (!it.hasNext()) return mst;
            start = it.next();
        }

        Set<String> visited = new HashSet<>();
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(e -> e.weight));

        visited.add(start);
        pq.addAll(graph.neighbors(start));

        while (!pq.isEmpty() && visited.size() < graph.getVertices().size()) {
            Edge edge = pq.poll();
            if (visited.contains(edge.to)) {
                continue;
            }
            mst.add(edge);
            visited.add(edge.to);

            for (Edge next : graph.neighbors(edge.to)) {
                if (!visited.contains(next.to)) {
                    pq.add(next);
                }
            }
        }
        return mst;
    }
}
