# Complexity Study - UpgradeOrderController.java

## 1. Identification
**File:** `UpgradeOrderController.java`  
**Description:** Controller responsible for USEI11 - Directed Line Upgrade Plan. Calculates the station upgrade order based on directed dependencies using topological sorting. If cycles exist in the graph, it identifies the stations involved.

---

## 2. General Code Structure
The `UpgradeOrderController` class includes:
- Method that builds the directed graph from stations and lines;
- Method that calculates the upgrade order using topological sorting;
- Auxiliary method that detects cycles using DFS with vertex coloring;
- Method that reconstructs the found cycle.

The code follows a **recursive** structure in the cycle detection method (DFS), and **iterative** in the remaining operations.

---

## 3. Time Complexity Analysis

### 3.1 Graph Construction
The graph construction (`buildGraph`) performs the following operations:
- Adds all vertices (stations) to the graph: **O(V)** where V is the number of stations
- Adds all edges (lines) to the graph: **O(E)** where E is the number of lines

**Total complexity:** O(V + E)

### 3.2 Upgrade Order Calculation
The `calculateUpgradeOrder()` method executes:

1. **Cycle detection** (`detectCycles`):
    - Initialization of color and parent maps: O(V)
    - DFS on all unvisited vertices
    - For each vertex, explores all adjacent edges

According to the **Graph Traversals** slide:
```
Algorithm void DFS(Graph<V,E> G, V vOrig, boolean[] visited, LinkedList<V> qdfs){
    if (visited[vOrigKey]) return
    add vOrig-element to qdfs
    visited[vOrigKey]=true
    for (each vAdj of vOrig) {
        Recursively call DFS(G, vAdj, visited, qdfs)
    }
}
Time Complexity: O(V + E)
```

The implementation uses colored DFS to detect cycles, visiting each vertex once and exploring all edges.

**Cycle detection complexity:** O(V + E)

2. **Topological Sorting** (if no cycles exist):
    - DFS-based algorithm:
   ```
   Algorithm void topologSort(Graph<V,E> g, V vOrig, boolean[] visited, LinkedList<V> topsort) {
       make vOrig as visited
       for (each vAdj of vOrig)
           if (!visited[vAdj])
               topologSort(g,vAdj,visited,topsort)
       push vOrig into topsort
   }
   Time Complexity: O(V + E)
   ```

**Topological sorting complexity:** O(V + E)

### 3.3 Cycle Reconstruction
The `extractCycle` method traverses the recursion stack to reconstruct the cycle:
- In the worst case, traverses all vertices in the stack: **O(V)**

**Total complexity:** O(V)

### 3.4 Global Analysis
The `calculateUpgradeOrder` method executes:
- Cycle detection: O(V + E)
- If cycle exists: cycle extraction O(V)
- If no cycle: topological sorting O(V + E)

**Dominant complexity:** O(V + E)

---

## 4. Space Complexity Analysis

The program uses:
- **Directed graph** (MapGraph): O(V + E)
    - Vertices: O(V)
    - Edges: O(E)
- **Auxiliary structures in cycle detection:**
    - Map<Station, Integer> color: O(V)
    - Map<Station, Station> parent: O(V)
    - LinkedList<Station> path (recursion stack): O(V) in the worst case
- **Result:**
    - Set<Station> or LinkedList<Station>: O(V)

**Total space complexity:** O(V + E)

---

## 5. Data Structures Used

### MapGraph (used to represent the graph)
According to the **Adjacency Map Structure**:

| Operation | Complexity |
|----------|--------------|
| addVertex(v) | O(1) |
| addEdge(u,v,w) | O(1) |
| adjVertices(v) | O(deg(v)) |
| vertices() | O(V) |


---

## 6. Case Analysis

| Case | Description | Complexity |
|------|-----------|--------------|
| **Best case** | Small acyclic graph, few edges | O(V + E) |
| **Average case** | Graph with E ≈ V, no cycles or small cycles | O(V + E) |
| **Worst case** | Dense graph with E ≈ V², or with multiple cycles | O(V + E) |

**Note:** Even in the worst case, the complexity remains O(V + E) because each vertex and edge are visited at most once in the DFS.



---

## 7. Conclusion

The `UpgradeOrderController.java` class presents:
- **Time complexity:** O(V + E) for all main operations
- **Space complexity:** O(V + E)

The algorithm is **efficient** because:
1. Uses DFS traversal that visits each vertex and edge exactly once
2. Cycle detection is integrated into the traversal, with no additional overhead
3. The MapGraph structure offers O(1) operations for basic operations
