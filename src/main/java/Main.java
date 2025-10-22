import com.google.gson.*;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        String inputFile = "src/ass_3_input.json";
        String outputFile = "ass_3_output.json";

        try {
            JsonObject json = JsonParser.parseReader(new FileReader(inputFile)).getAsJsonObject();
            JsonArray graphs = json.getAsJsonArray("graphs");
            JsonArray results = new JsonArray();

            for (JsonElement gElem : graphs) {
                JsonObject gObj = gElem.getAsJsonObject();
                int id = gObj.get("id").getAsInt();
                JsonArray nodes = gObj.getAsJsonArray("nodes");
                JsonArray edges = gObj.getAsJsonArray("edges");

                Graph graph = new Graph();
                for (JsonElement e : edges) {
                    JsonObject edge = e.getAsJsonObject();
                    graph.addEdge(edge.get("from").getAsString(),
                            edge.get("to").getAsString(),
                            edge.get("weight").getAsInt());
                }

                long t1 = System.nanoTime();
                AlgoResult primRes = Prim.findMST(graph, nodes.get(0).getAsString());
                long t2 = System.nanoTime();

                long t3 = System.nanoTime();
                AlgoResult kruskalRes = Kruskal.findMST(graph);
                long t4 = System.nanoTime();

                int primCost = primRes.edges.stream().mapToInt(e -> e.weight).sum();
                int kruskalCost = kruskalRes.edges.stream().mapToInt(e -> e.weight).sum();

                JsonObject one = new JsonObject();
                one.addProperty("graph_id", id);

                JsonObject istat = new JsonObject();
                istat.addProperty("vertices", nodes.size());
                istat.addProperty("edges", edges.size());
                one.add("input_stats", istat);

                JsonObject primObj = new JsonObject();
                primObj.add("mst_edges", edgesToJson(primRes.edges));
                primObj.addProperty("total_cost", primCost);
                primObj.addProperty("operations_count", primRes.operations);
                primObj.addProperty("execution_time_ms", (t2 - t1) / 1_000_000.0);
                one.add("prim", primObj);

                JsonObject krObj = new JsonObject();
                krObj.add("mst_edges", edgesToJson(kruskalRes.edges));
                krObj.addProperty("total_cost", kruskalCost);
                krObj.addProperty("operations_count", kruskalRes.operations);
                krObj.addProperty("execution_time_ms", (t4 - t3) / 1_000_000.0);
                one.add("kruskal", krObj);

                results.add(one);
            }

            JsonObject out = new JsonObject();
            out.add("results", results);

            writePrettyWithCompactEdges(out, outputFile);
            System.out.println("Results saved to " + outputFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    private static void writePrettyWithCompactEdges(JsonObject root, String path) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(path))) {
            writeObject(root, w, 0);
        }
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
            indent(w, ind + 2);
            w.write("\"" + e.getKey() + "\": ");
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
            indent(w, ind + 2);
            w.write(e.toString());
            w.write(i + 1 < edges.size() ? ",\n" : "\n");
        }
        indent(w, ind); w.write("]");
    }

    private static void indent(Writer w, int spaces) throws IOException {
        for (int i = 0; i < spaces; i++) w.write(' ');
    }
}
