import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class MSTTest {

    static class Parsed {
        final List<JsonObject> graphs;
        Parsed(List<JsonObject> g){ this.graphs=g; }
    }

    static class OutRow {
        String file; int graphId; String label;
        int n, m, wPrim, wKruskal, mstEdges;
        double primMs, kruskalMs;
    }
    static class OutWrap { List<OutRow> results = new ArrayList<>(); }

    @Test
    public void correctnessAndConsistency() throws Exception {
        String[] candidates = {
                "ass_3_input_small.json",
                "ass_3_input_medium.json",
                "ass_3_input_large.json",
                "ass_3_input_large_300_1000.json",
                "ass_3_input_extralarge_1000_2000.json",

                "src/ass_3_input_small.json",
                "src/ass_3_input_medium.json",
                "src/ass_3_input_large.json",
                "src/ass_3_input_large_300_1000.json",
                "src/ass_3_input_extralarge_1000_2000.json",

                "src/test/resources/ass_3_input_small.json",
                "src/test/resources/ass_3_input_medium.json",
                "src/test/resources/ass_3_input_large.json",
                "src/test/resources/ass_3_input_large_300_1000.json",
                "src/test/resources/ass_3_input_extralarge_1000_2000.json"
        };

        List<String> available = new ArrayList<>();
        for (String p : candidates) if (Files.exists(Paths.get(p))) available.add(p);
        assertTrue(!available.isEmpty(),
                "No input datasets found (place *_small/medium/large.json in project root, src/ or src/test/resources/)");

        OutWrap out = new OutWrap();

        for (String path : available) {
            Parsed parsed = readInput(path);
            assertTrue(parsed.graphs.size() >= 1, "Empty graphs in " + path);

            for (JsonObject gObj : parsed.graphs) {
                int V = gObj.getAsJsonArray("nodes").size();
                Graph g = buildGraph(gObj);

                long t1 = System.nanoTime();
                AlgoResult pr = Prim.findMST(g, gObj.getAsJsonArray("nodes").get(0).getAsString());
                long t2 = System.nanoTime();
                AlgoResult kr = Kruskal.findMST(g);
                long t3 = System.nanoTime();

                int primCost = mstCost(pr.edges);
                int kruCost  = mstCost(kr.edges);

                if (pr.edges.size() == V - 1 && kr.edges.size() == V - 1) {
                    assertEquals(primCost, kruCost, "cost mismatch in " + path);
                }

                if (pr.edges.size() == V - 1 || kr.edges.size() == V - 1) {
                    assertEquals(V - 1, pr.edges.size(), "Prim |E|!=V-1 in " + path);
                    assertEquals(V - 1, kr.edges.size(), "Kruskal |E|!=V-1 in " + path);
                } else {
                    assertTrue(pr.edges.size() < V - 1 && kr.edges.size() < V - 1, "disconnected handling in " + path);
                }

                assertTrue(isAcyclic(pr.edges), "Prim cycles in " + path);
                assertTrue(isAcyclic(kr.edges), "Kruskal cycles in " + path);

                double primMs = (t2 - t1) / 1_000_000.0;
                double kruskalMs = (t3 - t2) / 1_000_000.0;
                assertTrue(primMs >= 0.0);
                assertTrue(kruskalMs >= 0.0);
                assertTrue(pr.operations >= 0);
                assertTrue(kr.operations >= 0);

                AlgoResult pr2 = Prim.findMST(g, gObj.getAsJsonArray("nodes").get(0).getAsString());
                AlgoResult kr2 = Kruskal.findMST(g);
                assertEquals(primCost, mstCost(pr2.edges), "Prim reproducibility in " + path);
                assertEquals(kruCost, mstCost(kr2.edges), "Kruskal reproducibility in " + path);

                OutRow row = new OutRow();
                row.file = Paths.get(path).getFileName().toString();
                row.graphId = gObj.get("id").getAsInt();
                row.label = gObj.get("label").getAsString();
                row.n = V;
                row.m = gObj.getAsJsonArray("edges").size();
                row.wPrim = primCost;
                row.wKruskal = kruCost;
                row.mstEdges = (V > 0) ? (V - 1) : 0;
                row.primMs = primMs;
                row.kruskalMs = kruskalMs;
                out.results.add(row);
            }
        }

        Path outPath = Paths.get("target", "ass_3_output.json");
        Files.createDirectories(outPath.getParent());
        try (Writer w = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8)) {
            new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create().toJson(out, w);
        }
    }

    private Parsed readInput(String path) throws Exception {
        try (Reader r = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            List<JsonObject> gs = new ArrayList<>();
            for (JsonElement e : root.getAsJsonArray("graphs")) gs.add(e.getAsJsonObject());
            return new Parsed(gs);
        }
    }

    private Graph buildGraph(JsonObject gObj) {
        Graph g = new Graph();
        for (JsonElement ee : gObj.getAsJsonArray("edges")) {
            JsonObject e = ee.getAsJsonObject();
            g.addEdge(e.get("from").getAsString(), e.get("to").getAsString(), e.get("weight").getAsInt());
        }
        return g;
    }

    private int mstCost(List<Edge> edges) {
        int s=0; for (Edge e: edges) s += e.weight; return s;
    }

    private boolean isAcyclic(List<Edge> edges) {
        Map<String,Integer> id = new HashMap<>();
        int idx=0;
        for (Edge e: edges) {
            if (!id.containsKey(e.from)) id.put(e.from, idx++);
            if (!id.containsKey(e.to)) id.put(e.to, idx++);
        }
        int[] p = new int[idx];
        for (int i=0;i<idx;i++) p[i]=i;
        for (Edge e: edges) {
            int u = id.get(e.from), v = id.get(e.to);
            int ru = find(p,u), rv=find(p,v);
            if (ru==rv) return false;
            p[ru]=rv;
        }
        return true;
    }

    private int find(int[] p, int x){
        while(p[x]!=x){ p[x]=p[p[x]]; x=p[x]; }
        return x;
    }
    @Test
    void produceOutput() throws Exception {
        Path out = GenerateOutputs.run();
        assertTrue(Files.exists(out));
    }

    @Test
    void produceBoth() throws Exception {
        Path json = GenerateOutputs.run();
        Path csv  = GenerateOutputsCsv.run();
        assertTrue(Files.exists(json));
        assertTrue(Files.exists(csv));
    }
}
