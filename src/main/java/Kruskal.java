import java.util.*;

public class Kruskal {

    static class UnionFind {
        private final Map<String, String> parent = new HashMap<>();
        private final Map<String, Integer> rank = new HashMap<>();
        long ops = 0;

        void makeSet(Collection<String> vs) {
            for (String v : vs) { parent.put(v, v); rank.put(v, 0); }
        }
        String find(String v) {
            ops++;
            if (!parent.get(v).equals(v)) parent.put(v, find(parent.get(v)));
            return parent.get(v);
        }
        boolean union(String a, String b) {
            String ra = find(a), rb = find(b);
            if (ra.equals(rb)) return false;
            int rka = rank.get(ra), rkb = rank.get(rb);
            if (rka < rkb) parent.put(ra, rb);
            else if (rka > rkb) parent.put(rb, ra);
            else { parent.put(rb, ra); rank.put(ra, rka + 1); }
            ops++;
            return true;
        }
    }

    public static AlgoResult findMST(Graph graph) {
        List<Edge> result = new ArrayList<>();
        List<Edge> edges = graph.getAllEdges();

        final long[] sortOps = {0};
        edges.sort((a, b) -> { sortOps[0]++; return Integer.compare(a.weight, b.weight); });

        UnionFind uf = new UnionFind();
        uf.makeSet(graph.getVertices());

        for (Edge e : edges) {
            if (uf.union(e.from, e.to)) {
                result.add(e);
                if (result.size() == graph.getVertices().size() - 1) break;
            }
        }
        long ops = sortOps[0] + uf.ops;
        return new AlgoResult(result, ops);
    }
}
