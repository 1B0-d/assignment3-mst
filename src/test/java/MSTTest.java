// src/test/java/MSTTest.java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.*;
import java.io.Reader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class MSTTest {

    static class ParsedGraph {
        Graph g;
        List<String> nodes = new ArrayList<>();
        int V;
        int id;
        String label;
    }

    private static Reader openJson(String name) throws Exception {
        var cl = MSTTest.class.getClassLoader();
        var url = cl.getResource(name);
        if (url != null) return new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);

        List<Path> candidates = List.of(
                Paths.get(name),
                Paths.get("src","test","resources", name),
                Paths.get("src","test","java", name),
                Paths.get("src","main","resources", name),
                Paths.get("src","main","java", name)
        );
        for (Path p : candidates) {
            if (Files.exists(p)) return Files.newBufferedReader(p, StandardCharsets.UTF_8);
        }

        Path root = Paths.get("").toAbsolutePath();
        try (Stream<Path> s = Files.walk(root, 8)) {
            Optional<Path> hit = s.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().equals(name))
                    .findFirst();
            if (hit.isPresent()) return Files.newBufferedReader(hit.get(), StandardCharsets.UTF_8);
        }

        throw new java.io.FileNotFoundException("Cannot locate JSON: " + name);
    }

    private static List<ParsedGraph> readGraphs(String jsonName) throws Exception {
        try (Reader r = openJson(jsonName)) {
            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            JsonArray arr = root.getAsJsonArray("graphs");
            List<ParsedGraph> out = new ArrayList<>();
            for (JsonElement ge : arr) {
                JsonObject gObj = ge.getAsJsonObject();
                JsonArray nodes = gObj.getAsJsonArray("nodes");
                JsonArray edges = gObj.getAsJsonArray("edges");

                ParsedGraph pg = new ParsedGraph();
                pg.id = gObj.get("id").getAsInt();
                pg.label = gObj.get("label").getAsString();
                for (JsonElement n : nodes) pg.nodes.add(n.getAsString());
                pg.V = pg.nodes.size();

                Graph g = new Graph();
                for (JsonElement ee : edges) {
                    JsonObject e = ee.getAsJsonObject();
                    g.addEdge(e.get("from").getAsString(), e.get("to").getAsString(), e.get("weight").getAsInt());
                }
                pg.g = g;
                out.add(pg);
            }
            return out;
        }
    }

    private static int sumCost(List<Edge> es) {
        int s = 0;
        for (Edge e : es) s += e.weight;
        return s;
    }

    private static boolean isAcyclic(List<String> nodes, List<Edge> es) {
        Map<String,Integer> idx = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) idx.put(nodes.get(i), i);
        DSU dsu = new DSU(nodes.size());
        for (Edge e : es) {
            int u = idx.get(e.from);
            int v = idx.get(e.to);
            if (!dsu.union(u, v)) return false;
        }
        return true;
    }

    static class DSU {
        int[] p, r;
        DSU(int n){ p=new int[n]; r=new int[n]; for(int i=0;i<n;i++) p[i]=i; }
        int find(int x){ return p[x]==x?x:(p[x]=find(p[x])); }
        boolean union(int a,int b){ a=find(a); b=find(b); if(a==b) return false; if(r[a]<r[b]){int t=a;a=b;b=t;} p[b]=a; if(r[a]==r[b]) r[a]++; return true; }
    }

    @Test
    void costsEqual_onConnectedGraphs() throws Exception {
        List<String> files = List.of(
                "ass_3_input_small.json",
                "ass_3_input_medium.json",
                "ass_3_input_large.json"
        );
        for (String f : files) {
            for (ParsedGraph pg : readGraphs(f)) {
                AlgoResult pr = Prim.findMST(pg.g, pg.nodes.get(0));
                AlgoResult kr = Kruskal.findMST(pg.g);
                int expected = Math.max(0, pg.V - 1);
                boolean primConn = pr.edges.size() == expected;
                boolean krusConn = kr.edges.size() == expected;
                if (primConn && krusConn) {
                    assertEquals(sumCost(pr.edges), sumCost(kr.edges), "cost mismatch: " + pg.label);
                }
            }
        }
    }

    @Test
    void mstHasVMinus1Edges_andAcyclic() throws Exception {
        List<String> files = List.of(
                "ass_3_input_small.json",
                "ass_3_input_medium.json",
                "ass_3_input_large.json",
                "ass_3_input_disconnected.json"
        );
        for (String f : files) {
            for (ParsedGraph pg : readGraphs(f)) {
                AlgoResult pr = Prim.findMST(pg.g, pg.nodes.get(0));
                AlgoResult kr = Kruskal.findMST(pg.g);
                int expected = Math.max(0, pg.V - 1);
                assertTrue(pr.edges.size() <= expected, "Prim edges > V-1: " + pg.label);
                assertTrue(kr.edges.size() <= expected, "Kruskal edges > V-1: " + pg.label);
                assertTrue(isAcyclic(pg.nodes, pr.edges), "Prim cycle: " + pg.label);
                assertTrue(isAcyclic(pg.nodes, kr.edges), "Kruskal cycle: " + pg.label);
            }
        }
    }

    @Test
    void handlesDisconnected() throws Exception {
        ParsedGraph pg = readGraphs("ass_3_input_disconnected.json").get(0);
        AlgoResult pr = Prim.findMST(pg.g, pg.nodes.get(0));
        AlgoResult kr = Kruskal.findMST(pg.g);
        int expected = Math.max(0, pg.V - 1);
        assertTrue(pr.edges.size() < expected, "Prim must return < V-1 on disconnected");
        assertTrue(kr.edges.size() < expected, "Kruskal must return < V-1 on disconnected");
    }

    @Test
    void reproducibleOnFixedJson() throws Exception {
        List<String> files = List.of(
                "ass_3_input_small.json",
                "ass_3_input_medium.json"
        );
        for (String f : files) {
            List<ParsedGraph> gs = readGraphs(f);
            for (int i = 0; i < Math.min(5, gs.size()); i++) {
                ParsedGraph pg = gs.get(i);
                AlgoResult p1 = Prim.findMST(pg.g, pg.nodes.get(0));
                AlgoResult p2 = Prim.findMST(pg.g, pg.nodes.get(0));
                AlgoResult k1 = Kruskal.findMST(pg.g);
                AlgoResult k2 = Kruskal.findMST(pg.g);
                assertEquals(sumCost(p1.edges), sumCost(p2.edges), "Prim cost not stable: " + pg.label);
                assertEquals(sumCost(k1.edges), sumCost(k2.edges), "Kruskal cost not stable: " + pg.label);
                assertEquals(p1.edges.size(), p2.edges.size(), "Prim size not stable: " + pg.label);
                assertEquals(k1.edges.size(), k2.edges.size(), "Kruskal size not stable: " + pg.label);
            }
        }
    }

    @Test
    void produceArtifacts() throws Exception {
        Path json = GenerateOutputs.run();
        assertTrue(Files.exists(json));
        Path csv = GenerateOutputsCsv.run();
        assertTrue(Files.exists(csv));
        Path dir = RenderGraphs.run();
        assertTrue(Files.exists(dir));
        try (var s = Files.list(dir)) {
            boolean any = s.anyMatch(p -> p.getFileName().toString().endsWith(".png"));
            assertTrue(any);
        }
    }
}
