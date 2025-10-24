// src/main/java/GenerateOutputsCsv.java
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateOutputsCsv {

    public static void main(String[] args) throws Exception { run(); }

    public static Path run() throws Exception {
        List<Path> inputs = findInputs();
        if (inputs.isEmpty()) throw new IllegalStateException("No ass_3_input_*.json found");

        Path outPath = Paths.get("target", "ass_3_output_table.csv");
        Files.createDirectories(outPath.getParent());

        try (BufferedWriter w = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8)) {
            w.write(String.join(",",
                    "file","graph_id","label",
                    "vertices","edges_count","expected_mst_edges",
                    "prim_cost","prim_edges","prim_ops","prim_time_ms",
                    "kruskal_cost","kruskal_edges","kruskal_ops","kruskal_time_ms",
                    "faster",
                    "connected","costs_equal"
            ));
            w.newLine();

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
                    String faster        = connected ? (primMs <= kruskalMs ? "prim" : "kruskal") : "n/a";

                    String[] row = new String[] {
                            csv(p.getFileName().toString()),
                            csv(String.valueOf(gObj.get("id").getAsInt())),
                            csv(gObj.get("label").getAsString()),
                            csv(String.valueOf(V)),
                            csv(String.valueOf(M)),
                            csv(String.valueOf(expected)),
                            csv(String.valueOf(primCost)),
                            csv(String.valueOf(primEdgesCnt)),
                            csv(String.valueOf(Math.max(0, pr.operations))),
                            csv(fmtMs7(primMs)),
                            csv(String.valueOf(kruskalCost)),
                            csv(String.valueOf(kruskalEdgesCnt)),
                            csv(String.valueOf(Math.max(0, kr.operations))),
                            csv(fmtMs7(kruskalMs)),
                            csv(faster),
                            csv(connected ? "1" : "0"),
                            csv(costsEqual ? "1" : "0")
                    };
                    w.write(String.join(",", row));
                    w.newLine();
                }
            }
        }
        return Paths.get("target", "ass_3_output_table.csv");
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
    private static String fmtMs7(double ms){ return String.format(java.util.Locale.US, "%.7f", ms); }
    private static String csv(String s){
        if (s==null) return "\"\"";
        StringBuilder b=new StringBuilder("\"");
        for (char c: s.toCharArray()) { if (c=='"') b.append("\"\""); else b.append(c); }
        b.append('"'); return b.toString();
    }
}
