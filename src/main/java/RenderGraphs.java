import com.google.gson.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class RenderGraphs {

    public static void main(String[] args) throws Exception { run(); }

    public static Path run() throws Exception {
        List<Path> inputs = findInputs();
        if (inputs.isEmpty()) throw new IllegalStateException("No ass_3_input_*.json found");

        Path outDir = Paths.get("target", "graphs");
        Files.createDirectories(outDir);

        for (Path p : inputs) {
            JsonObject root = readJson(p);
            JsonArray graphs = root.getAsJsonArray("graphs");
            if (graphs == null || graphs.size() == 0) continue;

            for (JsonElement ge : graphs) {
                JsonObject gObj = ge.getAsJsonObject();
                String label = gObj.get("label").getAsString();
                int id = gObj.get("id").getAsInt();
                JsonArray nodes = gObj.getAsJsonArray("nodes");
                JsonArray edges = gObj.getAsJsonArray("edges");

                Graph g = new Graph();
                Set<String> nodeNames = new LinkedHashSet<>();
                for (JsonElement n : nodes) nodeNames.add(n.getAsString());
                for (JsonElement ee : edges) {
                    JsonObject e = ee.getAsJsonObject();
                    g.addEdge(e.get("from").getAsString(), e.get("to").getAsString(), e.get("weight").getAsInt());
                }

                AlgoResult mst = Kruskal.findMST(g);
                Set<String> mstPairs = new HashSet<>();
                for (Edge e : mst.edges) {
                    String a = e.from, b = e.to;
                    if (a.compareTo(b) > 0) { String t = a; a = b; b = t; }
                    mstPairs.add(a + "|" + b);
                }

                BufferedImage img = drawGraph(nodeNames, edges, mstPairs, label);
                Path out = outDir.resolve(p.getFileName().toString().replace(".json", "")
                        + "__" + id + ".png");
                javax.imageio.ImageIO.write(img, "png", out.toFile());
            }
        }
        return outDir;
    }

    private static BufferedImage drawGraph(Set<String> nodes, JsonArray edges, Set<String> mstPairs, String title) {
        int W = 1400, H = 900;
        int margin = 80;
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(250, 250, 252));
        g.fillRect(0, 0, W, H);

        g.setColor(Color.DARK_GRAY);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 28f));
        g.drawString(title, margin, margin - 20);

        int n = Math.max(1, nodes.size());
        double cx = W / 2.0, cy = H / 2.0, R = Math.min(W, H) * 0.35;
        Map<String, Point2D.Double> pos = new LinkedHashMap<>();
        int idx = 0;
        for (String name : nodes) {
            double ang = (2 * Math.PI * idx) / n - Math.PI / 2;
            pos.put(name, new Point2D.Double(cx + R * Math.cos(ang), cy + R * Math.sin(ang)));
            idx++;
        }

        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (JsonElement ee : edges) {
            JsonObject e = ee.getAsJsonObject();
            String a = e.get("from").getAsString();
            String b = e.get("to").getAsString();
            int w = e.get("weight").getAsInt();

            String x = a, y = b;
            if (x.compareTo(y) > 0) { String t = x; x = y; y = t; }
            boolean inMst = mstPairs.contains(x + "|" + y);

            Point2D pa = pos.get(a), pb = pos.get(b);
            if (pa == null || pb == null) continue;

            if (inMst) {
                g.setColor(new Color(220, 50, 47));
                g.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            } else {
                g.setColor(new Color(180, 190, 200));
                g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            g.draw(new Line2D.Double(pa, pb));

            g.setColor(new Color(90, 90, 100));
            g.setFont(g.getFont().deriveFont(Font.PLAIN, 16f));
            double tx = (pa.getX() + pb.getX()) / 2.0;
            double ty = (pa.getY() + pb.getY()) / 2.0;
            g.drawString(String.valueOf(w), (float) tx, (float) ty);
        }

        int r = 20;
        for (Map.Entry<String, Point2D.Double> e : pos.entrySet()) {
            Point2D p = e.getValue();
            Shape circle = new Ellipse2D.Double(p.getX() - r, p.getY() - r, 2 * r, 2 * r);
            g.setColor(new Color(52, 101, 164));
            g.fill(circle);
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 15f));
            String name = e.getKey();
            Rectangle2D tb = g.getFontMetrics().getStringBounds(name, g);
            g.drawString(name, (float) (p.getX() - tb.getWidth() / 2), (float) (p.getY() + 5));
        }

        g.dispose();
        return img;
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
}
