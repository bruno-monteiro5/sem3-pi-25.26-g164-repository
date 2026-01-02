# RiskAwarePathController – Complexity

## Notation
- V: number of stations
- E: number of connections

## General idea
The controller computes the shortest path between two stations using an edge cost that may be negative.
Because of that, it uses the Bellman-Ford algorithm and is also able to detect negative cycles.

## Bellman-Ford algorithm
Bellman-Ford relaxes all edges V-1 times.

- The outer loop runs V-1 times
- Inside, all edges are processed

This results in a time complexity of:
O(V · E)

An early stop is used if no updates occur, but the worst case remains the same.

## Negative cycle detection
After the main relaxation phase, all edges are checked one more time to detect negative cycles.
This requires iterating through all edges once:
O(E)

If a negative cycle is found, the cycle is reconstructed by following predecessors, which may take up to V steps:
O(V)

## Path reconstruction
If no negative cycle exists, the shortest path is rebuilt by following predecessors from target to source.
In the worst case, this visits all stations:
O(V)

## Final complexity
Overall time complexity:

O(V · E)

The space complexity is O(V), since distance and predecessor information is stored for each station.