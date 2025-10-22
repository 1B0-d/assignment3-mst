
public class Edge implements Comparable<Edge> {
    public final String from, to;
    public final int weight;
    public Edge(String from, String to, int weight) { this.from = from; this.to = to; this.weight = weight; }
    @Override public int compareTo(Edge o) { return Integer.compare(this.weight, o.weight); }
    @Override public String toString() { return "{\"from\":\""+from+"\",\"to\":\""+to+"\",\"weight\":"+weight+"}"; }
}
