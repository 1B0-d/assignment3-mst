
import java.util.*;

public class Prim {
    public static AlgoResult findMST(Graph g, String startName) {
        if (g == null || g.isEmpty()) return new AlgoResult(new ArrayList<>(), 0);
        Map<String,Integer> idx = g.getIndex();
        List<String> vlist = g.getVertexList();
        int n = vlist.size();
        int start = (startName != null && idx.containsKey(startName)) ? idx.get(startName) : 0;

        final int INF = Integer.MAX_VALUE / 4;
        int[] key = new int[n];
        int[] parent = new int[n];
        boolean[] inMST = new boolean[n];
        Arrays.fill(key, INF);
        Arrays.fill(parent, -1);

        MinIndexedBinaryHeap pq = new MinIndexedBinaryHeap(n);
        for (int v = 0; v < n; v++) pq.insert(v, key[v]);
        pq.decrease(start, 0);

        long ops = 0;
        List<Edge> mst = new ArrayList<>(n - 1);
        while (!pq.isEmpty()) {
            int v = pq.pollMinKeyIndex(); ops++;
            inMST[v] = true;
            if (parent[v] != -1) mst.add(new Edge(vlist.get(parent[v]), vlist.get(v), key[v]));
            for (int[] adj : g.neighborsInt(v)) {
                int u = adj[0];
                int w = adj[1];
                if (!inMST[u] && w < key[u]) {
                    key[u] = w;
                    parent[u] = v;
                    pq.decrease(u, w); ops++;
                }
            }
            if (mst.size() == n - 1) break;
        }
        return new AlgoResult(mst, ops);
    }

    static final class MinIndexedBinaryHeap {
        private final int sizeMax;
        private int size;
        private final int[] pm;
        private final int[] im;
        private final int[] key;

        MinIndexedBinaryHeap(int maxSize) {
            this.sizeMax = maxSize;
            this.size = 0;
            this.pm = new int[maxSize];
            this.im = new int[maxSize];
            this.key = new int[maxSize];
            Arrays.fill(pm, -1);
            Arrays.fill(im, -1);
            Arrays.fill(key, Integer.MAX_VALUE / 4);
        }

        boolean isEmpty() { return size == 0; }
        boolean contains(int ki) { return pm[ki] != -1; }

        void insert(int ki, int k) {
            if (contains(ki)) throw new IllegalArgumentException("index already in heap");
            pm[ki] = size;
            im[size] = ki;
            key[ki] = k;
            swim(size++);
        }

        int pollMinKeyIndex() {
            int minki = im[0];
            delete(minki);
            return minki;
        }

        void decrease(int ki, int k) {
            if (!contains(ki)) insert(ki, k);
            else if (k < key[ki]) { key[ki] = k; swim(pm[ki]); }
        }

        private void delete(int ki) {
            int i = pm[ki];
            swap(i, --size);
            pm[ki] = -1;
            im[size] = -1;
            if (i == size) return;
            swim(i);
            sink(i);
        }

        private void swim(int i) {
            while (i > 0) {
                int p = (i - 1) >>> 1;
                if (less(i, p)) { swap(i, p); i = p; } else break;
            }
        }
        private void sink(int i) {
            while (true) {
                int l = (i << 1) + 1, r = l + 1, s = i;
                if (l < size && less(l, s)) s = l;
                if (r < size && less(r, s)) s = r;
                if (s != i) { swap(i, s); i = s; } else break;
            }
        }
        private boolean less(int i, int j) { return key[im[i]] < key[im[j]]; }
        private void swap(int i, int j) {
            int iKi = im[i], jKi = im[j];
            im[i] = jKi; im[j] = iKi;
            pm[iKi] = j; pm[jKi] = i;
        }
    }
}
