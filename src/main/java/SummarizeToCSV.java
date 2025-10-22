/* SummarizeToCSV.java */
import com.google.gson.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SummarizeToCSV {
    public static void main(String[] args) throws Exception {
        String inPath = args.length>0 ? args[0] : "ass_3_output.json";
        String outPath = args.length>1 ? args[1] : "ass_3_summary.csv";

        try (Reader r = Files.newBufferedReader(Paths.get(inPath));
             PrintWriter pw = new PrintWriter(Files.newBufferedWriter(Paths.get(outPath)))) {

            JsonObject root = JsonParser.parseReader(r).getAsJsonObject();
            pw.println("graph_id,V,E,prim_cost,kruskal_cost,prim_ms,kruskal_ms,prim_ops,kruskal_ops,faster");

            for (JsonElement e : root.getAsJsonArray("results")) {
                JsonObject o = e.getAsJsonObject();
                int id = o.get("graph_id").getAsInt();
                JsonObject stats = o.getAsJsonObject("input_stats");
                int V = stats.get("vertices").getAsInt();
                int E = stats.get("edges").getAsInt();
                JsonObject pr = o.getAsJsonObject("prim");
                JsonObject kr = o.getAsJsonObject("kruskal");

                int pc = pr.get("total_cost").getAsInt();
                int kc = kr.get("total_cost").getAsInt();
                double pms = pr.get("execution_time_ms").getAsDouble();
                double kms = kr.get("execution_time_ms").getAsDouble();
                long pops = pr.get("operations_count").getAsLong();
                long kops = kr.get("operations_count").getAsLong();
                String faster = o.getAsJsonObject("comparison").get("faster").getAsString();

                pw.printf(Locale.US, "%d,%d,%d,%d,%d,%.6f,%.6f,%d,%d,%s%n",
                        id, V, E, pc, kc, pms, kms, pops, kops, faster);
            }
        }
        System.out.println("OK");
    }
}
