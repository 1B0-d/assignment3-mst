/* RenderGraphs.java */
import com.google.gson.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;

public class RenderGraphs {
    public static void main(String[] args) throws Exception {
        String inPath = args.length > 0 ? args[0] : "ass_3_input.json";
        String outDir = args.length > 1 ? args[1] : "graphs_png";
        Files.createDirectories(Paths.get(outDir));

        try (Reader reader = Files.newBufferedReader(Paths.get(inPath))) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

            for (JsonElement e : root.getAsJsonArray("graphs")) {
                JsonObject g = e.getAsJsonObject();
                int id = g.get("id").getAsInt();
                List<String> nodes = new ArrayList<>();
                for (JsonElement ne : g.getAsJsonArray("nodes")) nodes.add(ne.getAsString());

                Map<String,Integer> idx = new HashMap<>();
                for (int i = 0; i < nodes.size(); i++) idx.put(nodes.get(i), i);

                int W = 1200, H = 800, R = Math.min(W, H) / 3;
                Point[] pos = new Point[nodes.size()];
                double cx = W / 2.0, cy = H / 2.0;
                for (int i = 0; i < nodes.size(); i++) {
                    double ang = 2 * Math.PI * i / Math.max(1, nodes.size()) - Math.PI / 2;
                    pos[i] = new Point((int)(cx + R * Math.cos(ang)), (int)(cy + R * Math.sin(ang)));
                }

                BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = img.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE); g2.fillRect(0, 0, W, H);

                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(60, 60, 60));
                for (JsonElement ee : g.getAsJsonArray("edges")) {
                    JsonObject eobj = ee.getAsJsonObject();
                    int u = idx.get(eobj.get("from").getAsString());
                    int v = idx.get(eobj.get("to").getAsString());
                    int w = eobj.get("weight").getAsInt();
                    Point a = pos[u], b = pos[v];
                    g2.drawLine(a.x, a.y, b.x, b.y);
                    int lx = (a.x + b.x) / 2, ly = (a.y + b.y) / 2;
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                    g2.drawString(String.valueOf(w), lx + 4, ly - 4);
                    g2.setColor(new Color(60, 60, 60));
                }

                int rad = 20;
                for (int i = 0; i < nodes.size(); i++) {
                    Point p = pos[i];
                    g2.setColor(new Color(52, 120, 246));
                    g2.fillOval(p.x - rad, p.y - rad, 2 * rad, 2 * rad);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                    String label = nodes.get(i);
                    FontMetrics fm = g2.getFontMetrics();
                    int tw = fm.stringWidth(label);
                    g2.drawString(label, p.x - tw / 2, p.y + fm.getAscent() / 2 - 2);
                }

                g2.dispose();
                String outPath = outDir + "/graph_" + id + ".png";
                ImageIO.write(img, "png", new File(outPath));
            }
        }
    }
}
