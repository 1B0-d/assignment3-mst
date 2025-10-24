// src/main/java/GenerateOutputs.java
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateOutputs {

    static class EdgeOut {
        String from, to;
        int weight;
        EdgeOut(String f, String t, int w){ this.from=f; this.to=t; this.weight=w; }
    }
    static class AlgoOut {
        double execution_time_ms;
        long operations;
        int total_cost;
        boolean connected;
        List<EdgeOut> mst_edges = new ArrayList<>();
    }
    static class ResultOut {
        String file;
        int graph_id;
        String label;
        Map<String,Integer> input_stats = new LinkedHashMap<>();
        AlgoOut prim = new AlgoOut();
        AlgoOut kruskal = new AlgoOut();
        Map<String,Object> comparison = new LinkedHashMap<>();
    }
    static class Bundle {
        List<ResultOut> results = new ArrayList<>();
    }

    public static void main(String[] args) throws Exception { run(); }

    public static Path run() throws Exception {
        List<Path> inputs = findInputs();
        if (inputs.isEmpty()) throw new IllegalStateException("No ass_3_input_*.json found");

        Bundle bundle = new Bundle();

        for (Path p : inputs) {
            JsonObject root = readJson(p);
            JsonArray graphs = root.getAsJsonArray("graphs");
            if (graphs == null || graphs.size() == 0) continue;

            for (JsonElement ge : graphs) {
                JsonObject gObj = ge.getAsJsonObject();
                JsonArray nodes = gObj.getAsJsonArray("nodes");
                JsonArray edges = gObj.getAsJsonArray("edges");
                int V = nodes.size();
                int M = edges.size();
                int expected = Math.max(0, V - 1);

                Graph g = new Graph();
                for (JsonElement ee : edges) {
                    JsonObject e = ee.getAsJsonObject();
                    g.addEdge(e.get("from").getAsString(), e.get("to").getAsString(), e.get("weight").getAsInt());
                }


                long t1 = System.nanoTime();
                AlgoResult pr = Prim.findMST(g, nodes.get(0).getAsString());
                long t2 = System.nanoTime();

                AlgoResult kr = Kruskal.findMST(g);
                long t3 = System.nanoTime();

                double primMs    = (t2 - t1) / 1_000_000.0;
                double kruskalMs = (t3 - t2) / 1_000_000.0;

                int primEdgesCnt     = pr.edges.size();
                int kruskalEdgesCnt  = kr.edges.size();
                int primCost         = sumWeight(pr.edges);
                int kruskalCost      = sumWeight(kr.edges);

                boolean primConn     = (primEdgesCnt == expected);
                boolean krusConn     = (kruskalEdgesCnt == expected);
                boolean connected    = primConn && krusConn;
                boolean costsEqual   = connected && (primCost == kruskalCost);
                String  faster       = connected ? (primMs <= kruskalMs ? "prim" : "kruskal") : "n/a";

                ResultOut r = new ResultOut();
                r.file = p.getFileName().toString();
                r.graph_id = gObj.get("id").getAsInt();
                r.label = gObj.get("label").getAsString();

                r.input_stats.put("vertices", V);
                r.input_stats.put("edges", M);

                r.prim.execution_time_ms = primMs;
                r.prim.operations    = Math.max(0L, pr.operations);
                r.prim.total_cost = primCost;
                r.prim.connected  = primConn;
                for (Edge e : pr.edges) r.prim.mst_edges.add(new EdgeOut(e.from, e.to, e.weight));

                r.kruskal.execution_time_ms = kruskalMs;
                r.kruskal.operations = Math.max(0L, kr.operations);
                r.kruskal.total_cost = kruskalCost;
                r.kruskal.connected  = krusConn;
                for (Edge e : kr.edges) r.kruskal.mst_edges.add(new EdgeOut(e.from, e.to, e.weight));

                r.comparison.put("cost_equal", costsEqual);
                r.comparison.put("prim_ms", primMs);
                r.comparison.put("kruskal_ms", kruskalMs);
                r.comparison.put("faster", faster);

                bundle.results.add(r);
            }
        }

        Path out = Paths.get("ass_3_output.json");
        try (BufferedWriter w = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(bundle, w);
        }
        return out;
    }

    private static List<Path> findInputs() throws IOException {
        Path root = Paths.get("").toAbsolutePath();
        try (var s = Files.walk(root, 2)) {
            return s.filter(p -> {
                String name = p.getFileName().toString();
                return name.startsWith("ass_3_input_") && name.endsWith(".json");
            }).sorted().collect(Collectors.toList());
        }
    }
    private static JsonObject readJson(Path p) throws IOException {
        try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(r).getAsJsonObject();
        }
    }
    private static int sumWeight(List<Edge> es){ int s=0; for (Edge e: es) s+=e.weight; return s; }
}
