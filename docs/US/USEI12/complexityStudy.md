# MinimalBackboneController – Complexity

## Notation
- V: number of stations
- E: number of connections

## General idea
The controller builds the railway graph, converts it to an undirected graph and computes a minimal backbone network using Kruskal’s algorithm.

## Graph preparation
The directed graph is converted into an undirected graph.
All stations are added once and all connections are inspected once to avoid duplicated edges.

This step visits all stations and connections:
O(V + E)

## Minimum Spanning Tree
Kruskal’s algorithm is used to build the minimal backbone.

All edges are copied into a list and sorted by weight.
The sorting is done using bubble sort, which has complexity O(E²).

After sorting, the algorithm iterates through all edges and uses Union-Find to check and merge components.
This step takes O(E).

## Total distance
The total distance of the backbone network is computed by iterating through all edges of the MST:
O(E)

## Final complexity
Overall time complexity:

O(E²)

The space complexity is O(V + E), since the graph, edge list and Union-Find structures store all stations and connections.