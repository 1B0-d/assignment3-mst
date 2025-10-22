
import java.util.*;

public class Graph {
    private final Map<String, List<Edge>> adj = new HashMap<>();
    private final List<Edge> edgesUnique = new ArrayList<>();
    private final Set<String> seenUndirected = new HashSet<>();

    public void addEdge(String a, String b, int w) {
        adj.putIfAbsent(a, new ArrayList<>()); adj.putIfAbsent(b, new ArrayList<>());
        adj.get(a).add(new Edge(a, b, w));     adj.get(b).add(new Edge(b, a, w));
        String u = a.compareTo(b) < 0 ? a+"#"+b : b+"#"+a;
        if (seenUndirected.add(u)) edgesUnique.add(new Edge(a.compareTo(b) < 0 ? a : b, a.compareTo(b) < 0 ? b : a, w));
    }
    public Set<String> getVertices() { return adj.keySet(); }
    public List<Edge> neighbors(String v) { return adj.getOrDefault(v, Collections.emptyList()); }
    public List<Edge> getEdgesUnique() { return edgesUnique; }
    public boolean isEmpty() { return adj.isEmpty(); }
}
