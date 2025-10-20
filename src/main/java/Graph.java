
import java.util.*;

public class Graph {
    private final Map<String, List<Edge>> adj = new HashMap<>();

    public void addEdge(String from, String to, int weight) {
        adj.putIfAbsent(from, new ArrayList<>());
        adj.putIfAbsent(to, new ArrayList<>());
        adj.get(from).add(new Edge(from, to, weight));
        adj.get(to).add(new Edge(to, from, weight));
    }

    public Set<String> getVertices() {
        return adj.keySet();
    }

    public List<Edge> getAllEdges() {

        List<Edge> all = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Map.Entry<String, List<Edge>> entry : adj.entrySet()) {
            for (Edge e : entry.getValue()) {
                String key = e.from.compareTo(e.to) < 0 ? e.from + "#" + e.to : e.to + "#" + e.from;
                if (seen.add(key)) all.add(new Edge(
                        key.split("#")[0],
                        key.split("#")[1],
                        e.weight
                ));
            }
        }
        return all;
    }
}
