 Complexity Study - MaxThroughputController.java

## File purpose description

The file serves as the host of the backbone procedures of the RailwayNetworkOptimization/MaxThroughputUI.java, and the requests made in USEI14 in general, that being a procedure that calculates the máximum throughput of trains in a given path between one station and another.

Using the Ford-Fulkerson algorithm, it tests out all possible paths between the 2 stations that are possible through the line's direction and that offer the largest amount of throughput.

(note: given the graph given being directed, and of a large and dynamic scale, certain procedures may not be sucessful in one way, or both, given that certain line directions do not allow to reach the station in one specific manner (example: BRAINE-LE-CT-MANAGE gives out a non null value, but the opposite path does due to directed lines))

## File structure

int computeMaxFlow(Station source, Station target): serves just to call the execution of the ford-fulkerson algorithm. adding the created graph in the main menu (Temporal Complexity: O(1))

int fordFulkerson(MapGraph<Station, Double> graph, Station source, Station target): executes the ford-fulkerson algorythm, passing through all n stations between one and the other, running depth first searches in order to find the highest possible throughput (Temporal Complexity: O(n*n(n vertexes, n edges)) == O(n^2))

double dfsFindPath(MapGraph<Station, Double> residual, Station current, Station target, Set<Station> visited, double flow): execution of the depth-first-search between one station and the other in order to obtain the highest possible throughput valalue, calculated by obtaining all possible paths between a Station an the Other, and the highest value of common throughput of trains between both stations (Temporal Complexity: O(x))

boolean isGraphBuilt(): verifies if the graph is built to confirm if the procedures can be executed (Temporal Complexity: O(1))

int getStationCount(): obtains the full count of all stations in the graph (Temporal Complexity: O(1))

int getLineCount(): obtains the full count of all lines in the graph (Temporal Complexity: O(1))

Station getStationById(String id): obtains a station by its id (Temporal Complexity: O(n))

String getStations(): obtains a string containing the description of all stations in the graph (Temporal Complexity: O(n))

## Algorythm usage analysis

As mentioned, the used algorythm is the Ford-Fulkerson algorithm. This algorithm is a dynamic programming algorithm that finds the maximum flow in a network by using the minimum cut method. This algorythm was chosen in comparison to other similar ones (e.g. Edmonds-Karp) because it is more efficient in terms of time complexity, using DFS instead of BFS. It's complexity, in this case, is of O(n^2), which in Edmonds-Karp would be O(n^3).

## Memory usage

The Vertexes and edges of the graph are stored in memory, through a MapGraph object. This object is created once, in a method called "buildFromCsv" in the "BuildRailwayNetworkController.java". The vertexes in this case are "Station" objetcs, and the edges are objects of the "double" type.

## Scalability analysis

In the state it currently is, while it may be possible to scale up the belgian rail network, it may also be prejudicial to the execution of the functionality of the program, due to it's basing on the directions of the lines between one station and the other. Any more scaling may lead to longer execution times directly related to the increased lines, or full fail of the program due to the new directions leading to impossible paths between one station and the other.



## Final conclusion

Given all the methods described above, it is safe to assume that the complexity of this Controller file is O(n^2). It may also be safe to say that, as it is, the belgian network may be scalable, but any scaling must have into account the current directions of the lines.