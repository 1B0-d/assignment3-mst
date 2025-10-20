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
                    String from = edge.get("from").getAsString();
                    String to = edge.get("to").getAsString();
                    int weight = edge.get("weight").getAsInt();
                    graph.addEdge(from, to, weight);
                }

                long t1 = System.nanoTime();
                List<Edge> primEdges = Prim.findMST(graph, nodes.get(0).getAsString());
                long t2 = System.nanoTime();
                double primTime = (t2 - t1) / 1_000_000.0;
                int primCost = primEdges.stream().mapToInt(e -> e.weight).sum();

                long t3 = System.nanoTime();
                List<Edge> kruskalEdges = Kruskal.findMST(graph);
                long t4 = System.nanoTime();
                double kruskalTime = (t4 - t3) / 1_000_000.0;
                int kruskalCost = kruskalEdges.stream().mapToInt(e -> e.weight).sum();

                JsonObject oneResult = new JsonObject();
                oneResult.addProperty("graph_id", id);

                JsonObject inputStats = new JsonObject();
                inputStats.addProperty("vertices", nodes.size());
                inputStats.addProperty("edges", edges.size());
                oneResult.add("input_stats", inputStats);

                JsonObject primObj = new JsonObject();
                primObj.add("mst_edges", edgesToJson(primEdges));
                primObj.addProperty("total_cost", primCost);
                primObj.addProperty("execution_time_ms", primTime);
                oneResult.add("prim", primObj);

                JsonObject kruskalObj = new JsonObject();
                kruskalObj.add("mst_edges", edgesToJson(kruskalEdges));
                kruskalObj.addProperty("total_cost", kruskalCost);
                kruskalObj.addProperty("execution_time_ms", kruskalTime);
                oneResult.add("kruskal", kruskalObj);

                results.add(oneResult);
            }

            JsonObject output = new JsonObject();
            output.add("results", results);

            try (FileWriter fw = new FileWriter(outputFile)) {
                Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
                gsonPretty.toJson(output, fw);
            }

            System.out.println("Results saved to " + outputFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonArray edgesToJson(List<Edge> edges) {
        JsonArray arr = new JsonArray();
        for (Edge e : edges) {
            JsonObject obj = new JsonObject();
            obj.addProperty("from", e.from);
            obj.addProperty("to", e.to);
            obj.addProperty("weight", e.weight);
            arr.add(obj);
        }
        return arr;
    }
}
