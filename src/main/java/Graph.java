
import java.util.*;

public class Graph {
    private final Map<String, List<Edge>> adj = new HashMap<>();
    private final List<Edge> edgesUnique = new ArrayList<>();
    private final Set<String> seenUndirected = new HashSet<>();

    private Map<String,Integer> index = null;
    private List<String> vertexList = null;
    @SuppressWarnings("unchecked")
    private List<int[]>[] adjInt = null;
    private List<int[]> edgesUniqueInt = null;

    public void addEdge(String a, String b, int w) {
        adj.putIfAbsent(a, new ArrayList<>()); adj.putIfAbsent(b, new ArrayList<>());
        adj.get(a).add(new Edge(a, b, w));     adj.get(b).add(new Edge(b, a, w));
        String key = a.compareTo(b) < 0 ? a + "#" + b : b + "#" + a;
        if (seenUndirected.add(key)) {
            boolean ab = a.compareTo(b) < 0;
            String from = ab ? a : b;
            String to   = ab ? b : a;
            edgesUnique.add(new Edge(from, to, w));
        }
        index = null; vertexList = null; adjInt = null; edgesUniqueInt = null;
    }

    public Set<String> getVertices() { return adj.keySet(); }
    public List<Edge> neighbors(String v) { return adj.getOrDefault(v, Collections.emptyList()); }
    public List<Edge> getEdgesUnique() { return edgesUnique; }
    public boolean isEmpty() { return adj.isEmpty(); }

    public Map<String,Integer> getIndex() { ensureIndexed(); return index; }
    public List<String> getVertexList() { ensureIndexed(); return vertexList; }
    public List<int[]> neighborsInt(int vid) { ensureIndexed(); return adjInt[vid]; }
    public List<int[]>[] getAdjInt() { ensureIndexed(); return adjInt; }
    public List<int[]> getEdgesUniqueInt() { ensureIndexed(); return edgesUniqueInt; }

    private void ensureIndexed() {
        if (index != null) return;
        vertexList = new ArrayList<>(adj.keySet());
        Collections.sort(vertexList);
        index = new HashMap<>(vertexList.size() * 2);
        for (int i = 0; i < vertexList.size(); i++) index.put(vertexList.get(i), i);

        int n = vertexList.size();
        adjInt = new ArrayList[n];
        for (int i = 0; i < n; i++) adjInt[i] = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String from = vertexList.get(i);
            List<Edge> list = adj.getOrDefault(from, Collections.emptyList());
            for (Edge e : list) {
                int to = index.get(e.to);
                adjInt[i].add(new int[]{to, e.weight});
            }
        }

        edgesUniqueInt = new ArrayList<>(edgesUnique.size());
        for (Edge e : edgesUnique) {
            int u = index.get(e.from);
            int v = index.get(e.to);
            int w = e.weight;
            if (u > v) { int tmp = u; u = v; v = tmp; }
            edgesUniqueInt.add(new int[]{u, v, w});
        }
    }
}
