package smartcity;

import org.junit.jupiter.api.*;
import smartcity.graph.dagsp.*;
import smartcity.model.*;
import smartcity.metrics.Metrics;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class DAGPathTest {

    @Test
    @DisplayName("Shortest path in linear DAG")
    public void testShortestPathLinear() {
        // 0 --(3)--> 1 --(2)--> 2 --(4)--> 3
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 3.0);
        graph.addEdge(1, 2, 2.0);
        graph.addEdge(2, 3, 4.0);

        List<Integer> topoOrder = Arrays.asList(0, 1, 2, 3);

        DAGShortestPath dagSP = new DAGShortestPath();
        Metrics metrics = new Metrics();
        Map<Integer, Double> distances = dagSP.shortestPaths(graph, 0, topoOrder, metrics);

        assertEquals(0.0, distances.get(0), 0.001);
        assertEquals(3.0, distances.get(1), 0.001);
        assertEquals(5.0, distances.get(2), 0.001);
        assertEquals(9.0, distances.get(3), 0.001);

        assertTrue(metrics.getRelaxations() >= 3);
    }

    @Test
    @DisplayName("Shortest path with multiple paths (chooses minimum)")
    public void testShortestPathMultiplePaths() {
        // 0 --(5)--> 2
        // 0 --(1)--> 1 --(2)--> 2
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 2.0);
        graph.addEdge(0, 2, 5.0);

        List<Integer> topoOrder = Arrays.asList(0, 1, 2);

        DAGShortestPath dagSP = new DAGShortestPath();
        Map<Integer, Double> distances = dagSP.shortestPaths(graph, 0, topoOrder, new Metrics());

        assertEquals(0.0, distances.get(0), 0.001);
        assertEquals(1.0, distances.get(1), 0.001);
        assertEquals(3.0, distances.get(2), 0.001); // Via 1, not direct
    }

    @Test
    @DisplayName("Shortest path with unreachable nodes")
    public void testShortestPathUnreachable() {
        // 0 -> 1, 2 -> 3 (disconnected)
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(2, 3, 1.0);

        List<Integer> topoOrder = Arrays.asList(0, 2, 1, 3);

        DAGShortestPath dagSP = new DAGShortestPath();
        Map<Integer, Double> distances = dagSP.shortestPaths(graph, 0, topoOrder, new Metrics());

        assertEquals(0.0, distances.get(0), 0.001);
        assertEquals(1.0, distances.get(1), 0.001);
        assertEquals(Double.POSITIVE_INFINITY, distances.get(2));
        assertEquals(Double.POSITIVE_INFINITY, distances.get(3));
    }

    @Test
    @DisplayName("Longest path in linear DAG")
    public void testLongestPathLinear() {
        // 0 --(3)--> 1 --(2)--> 2 --(4)--> 3
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 3.0);
        graph.addEdge(1, 2, 2.0);
        graph.addEdge(2, 3, 4.0);

        List<Integer> topoOrder = Arrays.asList(0, 1, 2, 3);

        DAGShortestPath dagSP = new DAGShortestPath();
        Metrics metrics = new Metrics();
        Map<Integer, Double> distances = dagSP.longestPaths(graph, 0, topoOrder, metrics);

        assertEquals(0.0, distances.get(0), 0.001);
        assertEquals(3.0, distances.get(1), 0.001);
        assertEquals(5.0, distances.get(2), 0.001);
        assertEquals(9.0, distances.get(3), 0.001); // Critical path

        assertTrue(metrics.getRelaxations() >= 3);
    }

    @Test
    @DisplayName("Longest path with multiple paths (chooses maximum)")
    public void testLongestPathMultiplePaths() {
        // 0 --(5)--> 2
        // 0 --(1)--> 1 --(2)--> 2
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 2.0);
        graph.addEdge(0, 2, 5.0);

        List<Integer> topoOrder = Arrays.asList(0, 1, 2);

        DAGShortestPath dagSP = new DAGShortestPath();
        Map<Integer, Double> distances = dagSP.longestPaths(graph, 0, topoOrder, new Metrics());

        assertEquals(0.0, distances.get(0), 0.001);
        assertEquals(1.0, distances.get(1), 0.001);
        assertEquals(5.0, distances.get(2), 0.001); // Direct path is longer
    }

    @Test
    @DisplayName("Critical path in complex DAG")
    public void testCriticalPathComplex() {
        // Diamond with different weights
        // 0 --(1)--> 1 --(3)--> 3
        // 0 --(5)--> 2 --(1)--> 3
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 3, 3.0);
        graph.addEdge(0, 2, 5.0);
        graph.addEdge(2, 3, 1.0);

        List<Integer> topoOrder = Arrays.asList(0, 1, 2, 3);

        DAGShortestPath dagSP = new DAGShortestPath();
        Map<Integer, Double> distances = dagSP.longestPaths(graph, 0, topoOrder, new Metrics());

        assertEquals(6.0, distances.get(3), 0.001); // Critical: 0->2->3
    }

    @Test
    @DisplayName("Single node graph")
    public void testShortestPathSingleNode() {
        Graph graph = new Graph(1);
        List<Integer> topoOrder = Arrays.asList(0);

        DAGShortestPath dagSP = new DAGShortestPath();
        Map<Integer, Double> distances = dagSP.shortestPaths(graph, 0, topoOrder, new Metrics());

        assertEquals(0.0, distances.get(0), 0.001);
    }

    @Test
    @DisplayName("Path with zero-weight edges")
    public void testShortestPathZeroWeights() {
        // 0 --(0)--> 1 --(0)--> 2
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 0.0);
        graph.addEdge(1, 2, 0.0);

        List<Integer> topoOrder = Arrays.asList(0, 1, 2);

        DAGShortestPath dagSP = new DAGShortestPath();
        Map<Integer, Double> distances = dagSP.shortestPaths(graph, 0, topoOrder, new Metrics());

        assertEquals(0.0, distances.get(0), 0.001);
        assertEquals(0.0, distances.get(1), 0.001);
        assertEquals(0.0, distances.get(2), 0.001);
    }
}