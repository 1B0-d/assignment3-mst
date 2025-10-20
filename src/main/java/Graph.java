

import java.util.*;

public class Graph {
    private final Map<String, List<Edge>> adj = new HashMap<>();

    public void addVertex(String v) {
        adj.putIfAbsent(v, new ArrayList<>());
    }

    public void addEdge(String from, String to, int weight) {
        addVertex(from);
        addVertex(to);
        adj.get(from).add(new Edge(from, to, weight));
        adj.get(to).add(new Edge(to, from, weight));
    }

    public Set<String> getVertices() {
        return adj.keySet();
    }

    public List<Edge> neighbors(String v) {
        List<Edge> list = adj.get(v);
        return list != null ? list : Collections.emptyList();
    }

    public List<Edge> getAllEdges() {
        List<Edge> all = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Map.Entry<String, List<Edge>> e : adj.entrySet()) {
            for (Edge ed : e.getValue()) {
                String a = ed.from, b = ed.to;
                String key = (a.compareTo(b) < 0) ? a + "#" + b : b + "#" + a;
                if (seen.add(key)) all.add(new Edge(a.compareTo(b) < 0 ? a : b,
                        a.compareTo(b) < 0 ? b : a,
                        ed.weight));
            }
        }
        return all;
    }

    public boolean hasVertex(String v) {
        return adj.containsKey(v);
    }

    public boolean isEmpty() {
        return adj.isEmpty();
    }
}
