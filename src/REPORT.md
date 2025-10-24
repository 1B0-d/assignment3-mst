# Assignment 3 — Minimum Spanning Tree (Prim vs Kruskal)
**Author:** Ildar Savzikhanov  
**Group:** SE-2426  
**Course:** Design and Analysis of Algorithms  
**Institution:** Astana IT University

---

## 🧭 Objective
To implement and compare two classical algorithms for constructing a **Minimum Spanning Tree (MST)** — **Prim’s** and **Kruskal’s** — on custom graph data structures in Java.  
The goal is to evaluate their performance on graphs of various sizes and densities, verify correctness, and visualize resulting MSTs.

---

## ⚙️ Methodology

### 1. Data
Four categories of input graphs were generated in JSON format:

| Dataset | Description | Example |
|----------|--------------|----------|
| `ass_3_input_small.json` | 5–30 vertices, 8–348 edges | Dense & Sparse small graphs |
| `ass_3_input_medium.json` | 30–300 vertices, 39–8073 edges | Medium graphs with varied density |
| `ass_3_input_large.json` | 300–1000 vertices, up to 24k edges | Large dense & sparse |
| `ass_3_input_disconnected.json` | 6 vertices, 4 edges | Disconnected test case |

All JSONs were parsed into the custom `Graph` class containing lists of `Edge` objects (`from`, `to`, `weight`).

---

### 2. Algorithms
Both algorithms operate directly on this `Graph` class.

#### Prim’s Algorithm
- Complexity: **O(E log V)** (with priority queue)
- Starts from a random vertex and expands the MST by choosing the smallest edge connecting a new vertex.
- Implementation tracks operation count and execution time.

#### Kruskal’s Algorithm
- Complexity: **O(E log E)**
- Sorts all edges by weight and merges them using **Union–Find (DSU)**.
- Tracks operation count and time similar to Prim’s.

---

### 3. Automation
- `GenerateOutputs.java` — builds `ass_3_output.json` with full MST data.
- `GenerateOutputsCsv.java` — produces a summary table `target/ass_3_output_table.csv`.
- `RenderGraphs.java` — visualizes graphs and highlights MST edges in red (`target/graphs/*.png`).
- `MSTTest.java` — JUnit testing suite verifying:
    - equal cost for connected graphs,
    - |E| ≤ V−1,
    - acyclicity,
    - disconnected handling,
    - reproducibility on fixed inputs,
    - generation of JSON, CSV, and PNG artifacts.

---

## 📊 Results

**Sample from output table (`ass_3_output_table.csv`):**

| file | label | vertices | edges | prim_cost | prim_time_ms | kruskal_cost | kruskal_time_ms | faster | connected | costs_equal |
|------|--------|-----------|--------|------------|---------------|----------------|------------------|---------|------------|--------------|
| small | dense (5v,8e) | 5 | 8 | 4 | 0.02 | 4 | 0.01 | Kruskal | ✅ | ✅ |
| medium | medium-medium (45v,247e) | 45 | 247 | 70 | 0.04 | 70 | 0.06 | Prim | ✅ | ✅ |
| large | large-dense (600v,16173e) | 600 | 16173 | 600 | 7.95 | 600 | 20.00 | Prim | ✅ | ✅ |
| large | large-sparse (1000v,1199e) | 1000 | 1199 | 4742 | 0.80 | 4742 | 0.52 | Kruskal | ✅ | ✅ |
| disconnected | (6v,4e) — 2 components | 6 | 4 | 10 | 0.20 | 10 | 0.02 | N/A | ❌ | ❌ |

---

## 🧩 Graph Visualizations

### Medium Graph (7v, 8e)
![medium](ass_3_input_medium__501.png)

### Dense Graph (9v, 28e)
![dense](ass_3_input_small__402.png)

### Medium-Medium Graph (45v, 247e)
![medium2](24d8fcba-a17e-476b-a4a1-72030c25f16f.png)

### Disconnected Graph (6v, 4e)
![disconnected](ass_3_input_disconnected__999.png)

---

## 🔍 Analysis

### 1. Correctness
- For all connected graphs:  
  `Prim.cost == Kruskal.cost`  
  `|E_MST| = V − 1`
- Disconnected graphs return fewer edges and set `connected = 0`.

### 2. Performance
Empirical results match theoretical predictions:

| Graph Type | Density | Faster Algorithm | Explanation |
|-------------|-----------|------------------|--------------|
| **Sparse (E ≈ V)** | low edge count | **Kruskal** | Sorting fewer edges faster than PQ updates |
| **Dense (E ≈ V²)** | many edges | **Prim** | Avoids sorting all edges, local PQ updates cheaper |
| **Medium** | balanced | depends | roughly equal complexity |

### 3. Observations
- Kruskal dominated most **large-sparse** cases.
- Prim outperformed Kruskal on **dense** and **medium-dense** graphs.
- Both algorithms scaled linearly with vertex count in practical tests.
- Time difference widens significantly for E ≫ V (dense graphs).

---

## 🧪 Testing Summary
| Test | Description | Status |
|------|--------------|--------|
| `costsEqual_onConnectedGraphs()` | Equal MST cost for connected graphs | ✅ |
| `mstHasVMinus1Edges_andAcyclic()` | Valid MST structure | ✅ |
| `handlesDisconnected()` | Works on disconnected graphs | ✅ |
| `reproducibleOnFixedJson()` | Deterministic results | ✅ |
| `produceArtifacts()` | Generates JSON, CSV, PNG | ✅ |

All 5 test suites passed successfully.

---

## 🏁 Conclusion
1. Both algorithms correctly build minimum spanning trees.
2. Performance trends align with theoretical time complexity:
    - **Prim** better on dense and smaller graphs.
    - **Kruskal** faster on sparse and large graphs.
3. The project demonstrates modular OOP design with clear data separation and automated reporting.

---

## 📂 Artifacts
| File | Purpose |
|------|----------|
| `ass_3_input_*.json` | input graphs |
| `ass_3_output.json` | full results |
| `target/ass_3_output_table.csv` | summary table |
| `target/graphs/*.png` | MST visualizations |
| `src/test/java/MSTTest.java` | verification tests |

---

## 🏅 Bonus Section
Implemented custom **Graph** and **Edge** classes, used as inputs to both algorithms and integrated with MST visualizations.  
This fulfills the 10% bonus requirement for object-oriented graph design.

---

## 📚 References
- Cormen, Leiserson, Rivest, Stein — *Introduction to Algorithms*, MIT Press.
- GeeksForGeeks — *Difference between Prim’s and Kruskal’s Algorithm*.
- CLRS Lecture Notes, AITU DAA course materials.

---

**End of Report**
