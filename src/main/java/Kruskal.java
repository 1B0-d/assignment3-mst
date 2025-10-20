

import java.util.*;

public class Kruskal {

    static class UnionFind {
        private final Map<String, String> parent = new HashMap<>();
        private final Map<String, Integer> rank = new HashMap<>();

        public void makeSet(Collection<String> vertices) {
            for (String v : vertices) {
                parent.put(v, v);
                rank.put(v, 0);
            }
        }

        public String find(String v) {
            if (!parent.get(v).equals(v)) {
                parent.put(v, find(parent.get(v)));
            }
            return parent.get(v);
        }

        public boolean union(String a, String b) {
            String ra = find(a), rb = find(b);
            if (ra.equals(rb)) return false;
            int rka = rank.get(ra), rkb = rank.get(rb);
            if (rka < rkb) parent.put(ra, rb);
            else if (rka > rkb) parent.put(rb, ra);
            else { parent.put(rb, ra); rank.put(ra, rka + 1); }
            return true;
        }
    }

    public static List<Edge> findMST(Graph graph) {
        List<Edge> result = new ArrayList<>();
        List<Edge> edges = graph.getAllEdges();
        Collections.sort(edges);

        UnionFind uf = new UnionFind();
        uf.makeSet(graph.getVertices());

        for (Edge e : edges) {
            if (uf.union(e.from, e.to)) {
                result.add(e);
            }
        }
        return result;
    }
}
