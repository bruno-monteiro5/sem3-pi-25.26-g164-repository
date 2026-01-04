# UpgradeOrderController – Complexity (USEI11)

## Notation
- V: number of stations
- E: number of directed connections

## General idea
The controller builds a directed graph where A -> B means "A must be upgraded before B".  
Then it checks if the graph has cycles.  
If there are no cycles, it returns a topological order of all stations.  
If there is a cycle, it returns the stations involved in that cycle.

## Build graph
- Adds all stations as vertices: O(V)
- Adds all lines as directed edges: O(E)

So building the graph is O(V + E).

## Cycle detection
Cycle detection is done with DFS using colors (white/gray/black).  
Each station is visited once and each edge is checked once.

Worst case:
O(V + E)

## Topological sort
If no cycles exist, it runs topological sort on the directed graph.  
Topological sort also visits all stations and edges once.

Worst case:
O(V + E)

## Final complexity
In the worst case the controller does:
- cycle detection: O(V + E)
- topological sort (only if no cycles): O(V + E)

So overall time complexity:

O(V + E)

Space complexity is O(V), because it stores color/parent maps and the DFS path.
