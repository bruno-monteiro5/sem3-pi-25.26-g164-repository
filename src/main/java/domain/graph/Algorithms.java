package domain.graph;

import domain.graph.matrix.MatrixGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.function.BinaryOperator;

/**
 *
 * @author DEI-ISEP
 *
 */
public class Algorithms {

    /** Performs breadth-first search of a Graph starting in a vertex
     *
     * @param g Graph instance
     * @param vert vertex that will be the source of the search
     * @return a LinkedList with the vertices of breadth-first search
     */
    public static <V, E> LinkedList<V> BreadthFirstSearch(Graph<V, E> g, V vert) {
        LinkedList<V> qbfs = new LinkedList<>();   // ordem de visita
        LinkedList<V> qaux = new LinkedList<>();   // fila auxiliar (queue)

        if (!g.validVertex(vert)) {
            return null;
        }

        boolean[] visited = new boolean[g.numVertices()];

        qbfs.add(vert);
        qaux.add(vert);
        visited[g.key(vert)] = true;

        while (!qaux.isEmpty()) {
            V v = qaux.removeFirst();

            for (V vAdj : g.adjVertices(v)) {

                int vAdjKey = g.key(vAdj);

                if (!visited[vAdjKey]) {
                    qbfs.add(vAdj);
                    qaux.add(vAdj);
                    visited[vAdjKey] = true;
                }
            }
        }
        return qbfs;
    }

    /** Performs depth-first search starting in a vertex
     *
     * @param g Graph instance
     * @param vOrig vertex of graph g that will be the source of the search
     * @param visited set of previously visited vertices
     * @param qdfs return LinkedList with vertices of depth-first search
     */
    private static <V, E> void DepthFirstSearch(Graph<V, E> g, V vOrig, boolean[] visited, LinkedList<V> qdfs) {
        int vOrigKey = g.key(vOrig);

        if (visited[vOrigKey])
            return;

        qdfs.add(vOrig);
        visited[vOrigKey] = true;

        for (V vAdj : g.adjVertices(vOrig)) {
            DepthFirstSearch(g, vAdj, visited, qdfs);
        }
    }

    /** Performs depth-first search starting in a vertex
     *
     * @param g Graph instance
     * @param vert vertex of graph g that will be the source of the search

     * @return a LinkedList with the vertices of depth-first search
     */
    public static <V, E> LinkedList<V> DepthFirstSearch(Graph<V, E> g, V vert) {
        LinkedList<V> qdfs = new LinkedList<>();

        if (!g.validVertex(vert))
            return null;

        boolean[] visited = new boolean[g.numVertices()];

        DepthFirstSearch(g, vert, visited, qdfs);

        return qdfs;
    }

    /** Returns all paths from vOrig to vDest
     *
     * @param g       Graph instance
     * @param vOrig   Vertex that will be the source of the path
     * @param vDest   Vertex that will be the end of the path
     * @param visited set of discovered vertices
     * @param path    stack with vertices of the current path (the path is in reverse order)
     * @param paths   ArrayList with all the paths (in correct order)
     */
    private static <V, E> void allPaths(Graph<V, E> g, V vOrig, V vDest, boolean[] visited,
                                        LinkedList<V> path, ArrayList<LinkedList<V>> paths) {

        int vKey = g.key(vOrig);

        // adiciona o vértice atual ao caminho
        path.add(vOrig);
        visited[vKey] = true;

        // se chegámos ao destino → copiar caminho
        if (vOrig.equals(vDest)) {
            paths.add(new LinkedList<>(path));   // cria cópia independente
        } else {
            // explorar vizinhos
            for (V vAdj : g.adjVertices(vOrig)) {
                int aKey = g.key(vAdj);
                if (!visited[aKey]) {
                    allPaths(g, vAdj, vDest, visited, path, paths);
                }
            }
        }

        // backtracking:
        // remover último vértice e libertar para outros caminhos
        path.removeLast();
        visited[vKey] = false;
    }

    /** Returns all paths from vOrig to vDest
     *
     * @param g     Graph instance
     * @param vOrig information of the Vertex origin
     * @param vDest information of the Vertex destination
     * @return paths ArrayList with all paths from vOrig to vDest
     */
    public static <V, E> ArrayList<LinkedList<V>> allPaths(Graph<V, E> g, V vOrig, V vDest) {
        ArrayList<LinkedList<V>> paths = new ArrayList<>();

        // vértices inválidos → retornar lista vazia
        if (!g.validVertex(vOrig) || !g.validVertex(vDest)) {
            return paths;
        }

        boolean[] visited = new boolean[g.numVertices()];
        LinkedList<V> path = new LinkedList<>();

        allPaths(g, vOrig, vDest, visited, path, paths);

        return paths;
    }

    /**
     * Computes shortest-path distance from a source vertex to all reachable
     * vertices of a graph g with non-negative edge weights
     * This implementation uses Dijkstra's algorithm
     *
     * @param g        Graph instance
     * @param vOrig    Vertex that will be the source of the path
     * @param visited  set of previously visited vertices
     * @param pathKeys minimum path vertices keys
     * @param dist     minimum distances
     */
    private static <V, E> void shortestPathDijkstra(Graph<V, E> g, V vOrig,
                                                    Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                                    boolean[] visited, V [] pathKeys, E [] dist) {

        int n = g.numVertices();

        // inicialização
        for (int i = 0; i < n; i++) {
            visited[i] = false;
            pathKeys[i] = null;
            dist[i] = null;              // "infinito" (ainda sem caminho)
        }

        int origKey = g.key(vOrig);
        dist[origKey] = zero;            // distância à origem é zero

        V v = vOrig;

        // ciclo principal do Dijkstra
        while (v != null) {

            int vKey = g.key(v);
            visited[vKey] = true;

            // relaxar arestas de v
            for (V vAdj : g.adjVertices(v)) {

                int aKey = g.key(vAdj);
                if (visited[aKey]) continue;

                Edge<V, E> e = g.edge(v, vAdj);
                if (e == null) continue;

                // novo custo passando por v
                E newDist = sum.apply(dist[vKey], e.getWeight());

                if (dist[aKey] == null || ce.compare(newDist, dist[aKey]) < 0) {
                    dist[aKey] = newDist;
                    pathKeys[aKey] = v;   // predecessor de vAdj é v
                }
            }

            // escolher próximo vértice com menor dist não visitado
            v = null;
            E minDist = null;

            for (V vTmp : g.vertices()) {
                int k = g.key(vTmp);
                if (!visited[k] && dist[k] != null &&
                        (minDist == null || ce.compare(dist[k], minDist) < 0)) {
                    minDist = dist[k];
                    v = vTmp;
                }
            }
        }
    }


    /** Shortest-path between two vertices
     *
     * @param g graph
     * @param vOrig origin vertex
     * @param vDest destination vertex
     * @param ce comparator between elements of type E
     * @param sum sum two elements of type E
     * @param zero neutral element of the sum in elements of type E
     * @param shortPath returns the vertices which make the shortest path
     * @return if vertices exist in the graph and are connected, true, false otherwise
     */
    public static <V, E> E shortestPath(Graph<V, E> g, V vOrig, V vDest,
                                        Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                        LinkedList<V> shortPath) {

        shortPath.clear();

        // vértices inexistentes
        if (!g.validVertex(vOrig) || !g.validVertex(vDest))
            return null;

        int n = g.numVertices();
        boolean[] visited = new boolean[n];

        @SuppressWarnings("unchecked")
        V[] pathKeys = (V[]) new Object[n];

        @SuppressWarnings("unchecked")
        E[] dist = (E[]) new Object[n];

        // Dijkstra a partir de vOrig
        shortestPathDijkstra(g, vOrig, ce, sum, zero, visited, pathKeys, dist);

        int destKey = g.key(vDest);
        E length = dist[destKey];

        // não há caminho
        if (length == null)
            return null;

        // reconstruir caminho vOrig -> vDest
        getPath(g, vOrig, vDest, pathKeys, shortPath);

        return length;
    }

    /** Shortest-path between a vertex and all other vertices
     *
     * @param g graph
     * @param vOrig start vertex
     * @param ce comparator between elements of type E
     * @param sum sum two elements of type E
     * @param zero neutral element of the sum in elements of type E
     * @param paths returns all the minimum paths
     * @param dists returns the corresponding minimum distances
     * @return if vOrig exists in the graph true, false otherwise
     */
    public static <V, E> boolean shortestPaths(Graph<V, E> g, V vOrig,
                                               Comparator<E> ce, BinaryOperator<E> sum, E zero,
                                               ArrayList<LinkedList<V>> paths, ArrayList<E> dists) {

        paths.clear();
        dists.clear();

        if (!g.validVertex(vOrig))
            return false;

        int n = g.numVertices();
        boolean[] visited = new boolean[n];

        @SuppressWarnings("unchecked")
        V[] pathKeys = (V[]) new Object[n];

        @SuppressWarnings("unchecked")
        E[] dist = (E[]) new Object[n];

        // Dijkstra a partir de vOrig
        shortestPathDijkstra(g, vOrig, ce, sum, zero, visited, pathKeys, dist);

        // para cada vértice, construir caminho e guardar distância
        for (int i = 0; i < n; i++) {

            V v = g.vertex(i);
            LinkedList<V> path = new LinkedList<>();

            if (dist[i] != null) {
                getPath(g, vOrig, v, pathKeys, path);   // origem -> v
            }

            paths.add(path);        // pode ser lista vazia se não houver caminho
            dists.add(dist[i]);     // null se não houver caminho
        }

        return true;
    }

    /**
     * Extracts from pathKeys the minimum path between voInf and vdInf
     * The path is constructed from the end to the beginning
     *
     * @param g        Graph instance
     * @param vOrig    information of the Vertex origin
     * @param vDest    information of the Vertex destination
     * @param pathKeys minimum path vertices keys
     * @param path     stack with the minimum path (correct order)
     */
    private static <V, E> void getPath(Graph<V, E> g, V vOrig, V vDest,
                                       V [] pathKeys, LinkedList<V> path) {

        path.clear();

        if (!g.validVertex(vOrig) || !g.validVertex(vDest))
            return;

        V v = vDest;
        // constrói do fim para o início
        path.addFirst(v);

        while (!v.equals(vOrig)) {
            int vKey = g.key(v);
            V prev = pathKeys[vKey];
            if (prev == null) {          // não há caminho
                path.clear();
                return;
            }
            path.addFirst(prev);
            v = prev;
        }
    }

    /** Calculates the minimum distance graph using Floyd-Warshall
     *
     * @param g initial graph
     * @param ce comparator between elements of type E
     * @param sum sum two elements of type E
     * @return the minimum distance graph
     */
    public static <V,E> MatrixGraph <V,E> minDistGraph(Graph <V,E> g, Comparator<E> ce, BinaryOperator<E> sum) {
        // trabalhar sempre sobre uma MatrixGraph (matriz de distâncias)
        MatrixGraph<V, E> minDist = new MatrixGraph<>(g);

        int n = minDist.numVertices();

        // Floyd-Warshall: três ciclos aninhados k, i, j
        for (int k = 0; k < n; k++) {

            for (int i = 0; i < n; i++) {

                Edge<V, E> eIK = minDist.edge(i, k);
                if (eIK == null) {
                    // não há caminho i→k, não vale a pena continuar este i
                    continue;
                }

                for (int j = 0; j < n; j++) {

                    Edge<V, E> eKJ = minDist.edge(k, j);
                    if (eKJ == null) {
                        // não há caminho k→j, não há caminho i→k→j
                        continue;
                    }

                    // peso do caminho i→k→j
                    E throughK = sum.apply(eIK.getWeight(), eKJ.getWeight());

                    // aresta direta i→j (pode não existir)
                    Edge<V, E> eIJ = minDist.edge(i, j);

                    // se não existe aresta i→j, ou se o caminho via k é melhor, atualiza
                    if (eIJ == null || ce.compare(throughK, eIJ.getWeight()) < 0) {

                        V vI = minDist.vertex(i);
                        V vJ = minDist.vertex(j);

                        if (eIJ == null) {
                            // criar nova aresta com o novo peso mínimo
                            minDist.addEdge(vI, vJ, throughK);
                        } else {
                            // atualizar o peso existente
                            eIJ.setWeight(throughK);
                        }
                    }
                }
            }
        }

        return minDist;
    }


    public static <V, E> LinkedList<V> topologicalSort(Graph<V, E> g) {
        LinkedList<V> topsort = new LinkedList<>();

        boolean[] visited = new boolean[g.numVertices()];
        // por omissão ficam todos a false

        for (V vOrig : g.vertices()) {
            int key = g.key(vOrig);
            if (!visited[key]) {
                topologicalSortDFS(g, vOrig, visited, topsort);
            }
        }

        return topsort;
    }

    private static <V, E> void topologicalSortDFS(Graph<V, E> g,
                                                  V vOrig,
                                                  boolean[] visited,
                                                  LinkedList<V> topsort) {

        int keyOrig = g.key(vOrig);
        visited[keyOrig] = true;                 // make vOrig as visited

        for (V vAdj : g.adjVertices(vOrig)) {    // for each vAdj of vOrig
            int keyAdj = g.key(vAdj);
            if (!visited[keyAdj]) {
                topologicalSortDFS(g, vAdj, visited, topsort);
            }
        }

        // push vOrig into topsort (no fim da recursão)
        topsort.push(vOrig); // insere à cabeça → ordem topológica correta
    }

}
