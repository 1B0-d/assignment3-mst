import com.google.gson.*;
import java.io.*;
import java.util.*;

public class Main {
    private static final int WARMUP = 200;
    private static final int RUNS = 50;

    public static void main(String[] args) {
        String inputFile = "src/ass_3_input.json";
        String outputFile = "ass_3_output.json";

        double sumPrimMs = 0, sumKruMs = 0;
        int primWins = 0, kruWins = 0, ties = 0;
        int graphsCount = 0;

        try (FileReader r = new FileReader(inputFile);
             FileWriter fw = new FileWriter(outputFile);
             BufferedWriter bw = new BufferedWriter(fw)) {

            JsonObject in = JsonParser.parseReader(r).getAsJsonObject();
            JsonArray graphs = in.getAsJsonArray("graphs");
            JsonArray outResults = new JsonArray();

            for (JsonElement ge : graphs) {
                JsonObject gObj = ge.getAsJsonObject();
                int id = gObj.get("id").getAsInt();
                JsonArray nodes = gObj.getAsJsonArray("nodes");
                JsonArray edges = gObj.getAsJsonArray("edges");

                Graph g = new Graph();
                for (JsonElement ee : edges) {
                    JsonObject e = ee.getAsJsonObject();
                    g.addEdge(e.get("from").getAsString(), e.get("to").getAsString(), e.get("weight").getAsInt());
                }

                for (int i = 0; i < WARMUP; i++) { Prim.findMST(g, nodes.get(0).getAsString()); Kruskal.findMST(g); }

                double primMs = 0; long primOps = 0; List<Edge> primEdges = null;
                for (int i = 0; i < RUNS; i++) {
                    long t1 = System.nanoTime();
                    AlgoResult pr = Prim.findMST(g, nodes.get(0).getAsString());
                    long t2 = System.nanoTime();
                    primMs += (t2 - t1) / 1_000_000.0;
                    primOps += pr.operations;
                    if (primEdges == null) primEdges = pr.edges;
                }
                primMs /= RUNS; primOps /= RUNS;

                double kruMs = 0; long kruOps = 0; List<Edge> kruEdges = null;
                for (int i = 0; i < RUNS; i++) {
                    long t1 = System.nanoTime();
                    AlgoResult kr = Kruskal.findMST(g);
                    long t2 = System.nanoTime();
                    kruMs += (t2 - t1) / 1_000_000.0;
                    kruOps += kr.operations;
                    if (kruEdges == null) kruEdges = kr.edges;
                }
                kruMs /= RUNS; kruOps /= RUNS;

                int primCost = primEdges.stream().mapToInt(e -> e.weight).sum();
                int kruCost  = kruEdges.stream().mapToInt(e -> e.weight).sum();
                boolean primConnected = primEdges.size() == nodes.size() - 1;
                boolean kruConnected  = kruEdges.size() == nodes.size() - 1;

                graphsCount++;
                sumPrimMs += primMs;
                sumKruMs  += kruMs;

                double deltaMs = kruMs - primMs;
                String faster;
                if (Math.abs(deltaMs) < 1e-9) { faster = "tie"; ties++; }
                else if (primMs < kruMs) { faster = "prim"; primWins++; }
                else { faster = "kruskal"; kruWins++; }

                JsonObject one = new JsonObject();
                one.addProperty("graph_id", id);

                JsonObject ist = new JsonObject();
                ist.addProperty("vertices", nodes.size());
                ist.addProperty("edges", edges.size());
                one.add("input_stats", ist);

                JsonObject primObj = new JsonObject();
                primObj.add("mst_edges", edgesToJson(primEdges));
                primObj.addProperty("total_cost", primCost);
                primObj.addProperty("operations_count", primOps);
                primObj.addProperty("execution_time_ms", round6(primMs));
                primObj.addProperty("connected", primConnected);
                one.add("prim", primObj);

                JsonObject kruObj = new JsonObject();
                kruObj.add("mst_edges", edgesToJson(kruEdges));
                kruObj.addProperty("total_cost", kruCost);
                kruObj.addProperty("operations_count", kruOps);
                kruObj.addProperty("execution_time_ms", round6(kruMs));
                kruObj.addProperty("connected", kruConnected);
                one.add("kruskal", kruObj);

                JsonObject cmp = new JsonObject();
                cmp.addProperty("faster", faster);
                cmp.addProperty("prim_ms", round6(primMs));
                cmp.addProperty("kruskal_ms", round6(kruMs));
                cmp.addProperty("delta_ms", round6(Math.abs(deltaMs)));
                cmp.addProperty("delta_pct", round2(100.0 * Math.abs(deltaMs) / Math.max(primMs, kruMs)));
                cmp.addProperty("speedup", round3(Math.max(primMs, kruMs) / Math.max(1e-12, Math.min(primMs, kruMs))));
                one.add("comparison", cmp);

                outResults.add(one);
            }

            JsonObject summary = new JsonObject();
            summary.addProperty("graphs", graphsCount);
            summary.addProperty("prim_wins", primWins);
            summary.addProperty("kruskal_wins", kruWins);
            summary.addProperty("ties", ties);
            summary.addProperty("avg_prim_ms", round6(sumPrimMs / Math.max(1, graphsCount)));
            summary.addProperty("avg_kruskal_ms", round6(sumKruMs / Math.max(1, graphsCount)));
            summary.addProperty("who_is_faster_overall",
                    (sumPrimMs < sumKruMs) ? "prim" : (sumPrimMs > sumKruMs ? "kruskal" : "tie"));

            JsonObject out = new JsonObject();
            out.add("summary", summary);
            out.add("results", outResults);
            writePrettyWithCompactEdges(out, bw);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double round6(double x){ return Math.round(x * 1_000_000.0) / 1_000_000.0; }
    private static double round3(double x){ return Math.round(x * 1_000.0) / 1_000.0; }
    private static double round2(double x){ return Math.round(x * 100.0) / 100.0; }

    private static JsonArray edgesToJson(List<Edge> edges) {
        JsonArray arr = new JsonArray();
        for (Edge e : edges) {
            JsonObject o = new JsonObject();
            o.addProperty("from", e.from);
            o.addProperty("to", e.to);
            o.addProperty("weight", e.weight);
            arr.add(o);
        }
        return arr;
    }

    private static void writePrettyWithCompactEdges(JsonObject root, Writer w) throws IOException {
        writeObject(root, w, 0);
        w.flush();
    }
    private static void writeElement(JsonElement el, Writer w, int ind) throws IOException {
        if (el == null || el.isJsonNull()) { w.write("null"); return; }
        if (el.isJsonPrimitive()) { w.write(el.toString()); return; }
        if (el.isJsonObject()) { writeObject(el.getAsJsonObject(), w, ind); return; }
        if (el.isJsonArray()) { writeArray(el.getAsJsonArray(), w, ind); return; }
    }
    private static void writeObject(JsonObject obj, Writer w, int ind) throws IOException {
        indent(w, ind); w.write("{\n");
        Iterator<Map.Entry<String, JsonElement>> it = obj.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, JsonElement> e = it.next();
            indent(w, ind + 2); w.write("\"" + e.getKey() + "\": ");
            if ("mst_edges".equals(e.getKey()) && e.getValue().isJsonArray()) {
                writeEdgesCompact(e.getValue().getAsJsonArray(), w, ind + 2);
            } else {
                writeElement(e.getValue(), w, ind + 2);
            }
            w.write(it.hasNext() ? ",\n" : "\n");
        }
        indent(w, ind); w.write("}");
    }
    private static void writeArray(JsonArray arr, Writer w, int ind) throws IOException {
        w.write("[\n");
        for (int i = 0; i < arr.size(); i++) {
            writeElement(arr.get(i), w, ind + 2);
            w.write(i + 1 < arr.size() ? ",\n" : "\n");
        }
        indent(w, ind); w.write("]");
    }
    private static void writeEdgesCompact(JsonArray edges, Writer w, int ind) throws IOException {
        w.write("[\n");
        for (int i = 0; i < edges.size(); i++) {
            JsonObject e = edges.get(i).getAsJsonObject();
            indent(w, ind + 2); w.write(e.toString());
            w.write(i + 1 < edges.size() ? ",\n" : "\n");
        }
        indent(w, ind); w.write("]");
    }
    private static void indent(Writer w, int spaces) throws IOException { for (int i = 0; i < spaces; i++) w.write(' '); }
}
