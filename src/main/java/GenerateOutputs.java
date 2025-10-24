// src/main/java/GenerateOutputs.java
import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class GenerateOutputs {

    public static void main(String[] args) throws Exception { run(); }

    public static Path run() throws Exception {
        List<Path> inputs = findInputs();
        if (inputs.isEmpty()) throw new IllegalStateException("No ass_3_input_*.json found");

        StringBuilder out = new StringBuilder();
        out.append("{\n  \"results\": [\n");

        boolean firstResult = true;
        for (Path p : inputs) {
            JsonObject root = readJson(p);
            JsonArray graphs = root.getAsJsonArray("graphs");
            if (graphs == null || graphs.size() == 0) continue;

            for (JsonElement ge : graphs) {
                JsonObject gObj = ge.getAsJsonObject();
                JsonArray nodes = gObj.getAsJsonArray("nodes");
                JsonArray edges = gObj.getAsJsonArray("edges");
                int V = nodes.size();

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

                List<Edge> primEdges    = sortEdgesForPrint(pr.edges);
                List<Edge> kruskalEdges = sortEdgesForPrint(kr.edges);

                int wP = sumWeight(primEdges);
                int wK = sumWeight(kruskalEdges);

                if (!firstResult) out.append(",\n");
                firstResult = false;

                out.append("    {\n");
                out.append("      \"graph_id\": ").append(gObj.get("id").getAsInt()).append(",\n");
                out.append("      \"label\": ").append(quote(gObj.get("label").getAsString())).append(",\n");
                out.append("      \"nodes\": ").append(oneLineNodes(nodes)).append(",\n");

                out.append("      \"prim\": {\n");
                out.append("        \"mst_edges\": [\n");
                appendEdges(out, primEdges, "          ");
                out.append("        ],\n");
                out.append("        \"total_cost\": ").append(wP).append(",\n");
                out.append("        \"operations_count\": ").append(Math.max(0, pr.operations)).append(",\n");
                out.append("        \"execution_time_ms\": ").append(quote(fmtMs7(primMs))).append("\n");
                out.append("      },\n");

                out.append("      \"kruskal\": {\n");
                out.append("        \"mst_edges\": [\n");
                appendEdges(out, kruskalEdges, "          ");
                out.append("        ],\n");
                out.append("        \"total_cost\": ").append(wK).append(",\n");
                out.append("        \"operations_count\": ").append(Math.max(0, kr.operations)).append(",\n");
                out.append("        \"execution_time_ms\": ").append(quote(fmtMs7(kruskalMs))).append("\n");
                out.append("      }\n");

                out.append("    }");
            }
        }

        out.append("\n  ]\n}\n");

        Path outPath = Paths.get("target", "ass_3_output.json");
        Files.createDirectories(outPath.getParent());
        try (Writer w = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8)) {
            w.write(out.toString());
        }
        return outPath;
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
    private static String quote(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) { if (c=='\\'||c=='"') b.append('\\'); b.append(c); }
        b.append('"'); return b.toString();
    }
    private static String oneLineNodes(JsonArray nodes) {
        StringBuilder sb = new StringBuilder("[");
        for (int i=0;i<nodes.size();i++) { if (i>0) sb.append(","); sb.append(quote(nodes.get(i).getAsString())); }
        sb.append("]"); return sb.toString();
    }
    private static void appendEdges(StringBuilder sb, List<Edge> edges, String indent) {
        for (int i=0;i<edges.size();i++) {
            Edge e = edges.get(i);
            sb.append(indent).append("{\"from\":\"").append(e.from)
                    .append("\",\"to\":\"").append(e.to)
                    .append("\",\"weight\":").append(e.weight).append("}");
            if (i < edges.size()-1) sb.append(",");
            sb.append("\n");
        }
    }
    private static List<Edge> sortEdgesForPrint(List<Edge> edges) {
        List<Edge> copy = new ArrayList<>(edges);
        copy.sort(Comparator.comparingInt((Edge e)->e.weight).thenComparing(e->e.from).thenComparing(e->e.to));
        return copy;
    }
    private static String fmtMs7(double ms){ return String.format(java.util.Locale.US, "%.7f", ms); }
}
