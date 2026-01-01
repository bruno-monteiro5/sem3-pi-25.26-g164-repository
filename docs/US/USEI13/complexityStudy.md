# HubCentralityController – Complexity

## Notation
- V: number of stations
- E: number of connections

## General idea
The controller builds a graph with all stations and lines and then computes several centrality metrics for each station.

## Components and basic metrics
Connected components are computed using BFS (undirected) or a DFS-like approach (directed).  
Each station and each connection is visited once, so this step takes O(V + E).

Degree and strength are computed by iterating through all stations and all lines.  
This also takes O(V + E).

## Harmonic closeness
Harmonic closeness is calculated by running Dijkstra’s algorithm from every station.

One execution of Dijkstra takes O((V + E) log V).  
Since it is executed for all stations, the total cost is O(V · E log V).

## Betweenness centrality
Betweenness centrality also runs a Dijkstra-based algorithm from each station.  
For this reason, it has the same complexity: O(V · E log V).

## Final score and sorting
The HubScore is computed in one pass over all stations.

The results are sorted using insertion sort, which takes O(V²).

## Final complexity
Overall time complexity:

O(V · E log V + V²)

For sparse graphs, the dominant term is O(V · E log V).

The space complexity is O(V + E), since the graph and auxiliary structures store all stations and connections.
