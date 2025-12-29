 Complexity Study - MaxThroughputController.java

## File purpose description

The file serves as the host of the backbone procedures of the RailwayNetworkOptimization/MaxThroughputUI.java, and the requests made in USEI14 in general, that being a procedure that calculates the máximum throughput of trains in a given path between one station and another.

Using the Ford-Fulkerson algorithm, it tests out all possible paths between the 2 stations that are possible through the line's direction and that offer the largest amount of throughput.

(note: given the graph given being directed, and of a large and dynamic scale, certain procedures may not be sucessful in one way, or both, given that certain line directions do not allow to reach the station in one specific manner (example: BRAINE-LE-CT-MANAGE gives out a non null value, but the opposite path does due to directed lines))

## File structure

int computeMaxFlow(Station source, Station target): serves just to call the execution of the ford-fulkerson algorithm. adding the created graph in the main menu (Temporal Complexity: O(1))

int fordFulkerson(MapGraph<Station, Double> graph, Station source, Station target): executes the ford-fulkerson algorythm, passing through all n stations between one and the other, running depth first searches in order to find the highest possible throughput (Temporal Complexity: O(n^2))

double dfsFindPath(MapGraph<Station, Double> residual, Station current, Station target, Set<Station> visited, double flow): execution of the depth-first-search between one station and the other in order to obtain the highest possible throughput valalue, calculated by obtaining all possible paths between a Station an the Other, and the highest value of common throughput of trains between both stations (Temporal Complexity: O(x))

boolean isGraphBuilt(): verifies if the graph is built to confirm if the procedures can be executed (Temporal Complexity: O(1))

int getStationCount(): obtains the full count of all stations in the graph (Temporal Complexity: O(1))

int getLineCount(): obtains the full count of all lines in the graph (Temporal Complexity: O(1))

Station getStationById(String id): obtains a station by its id (Temporal Complexity: O(n))

String getStations(): obtains a string containing the description of all stations in the graph (Temporal Complexity: O(n))

## Final conclusion

Given all the methods described above, it is safe to assume that the complexity of this Controller file is O(n^2)