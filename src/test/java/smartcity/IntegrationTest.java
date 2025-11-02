package smartcity;

import org.junit.jupiter.api.*;
import smartcity.graph.scc.*;
import smartcity.graph.topo.*;
import smartcity.graph.dagsp.*;
import smartcity.model.*;
import smartcity.metrics.Metrics;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that verify the complete pipeline:
 * SCC -> Condensation -> Topological Sort -> DAG Shortest/Longest Paths
 */
public class IntegrationTest {

    @Test
    @DisplayName("Complete pipeline on simple cyclic graph")
    public void testCompletePipelineWithCycle() {
        // Graph with cycle: 0->1->2->0, then 2->3
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 2.0);
        graph.addEdge(1, 2, 3.0);
        graph.addEdge(2, 0, 1.0);
        graph.addEdge(2, 3, 5.0);

        // 1. Find SCCs
        TarjanSCC sccAlgo = new TarjanSCC();
        Metrics sccMetrics = new Metrics();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, sccMetrics);

        assertEquals(2, sccs.size()); // One SCC of size 3, one of size 1

        // 2. Build condensation
        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);
        assertEquals(2, condensation.getVertices());

        // 3. Topological sort
        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics topoMetrics = new Metrics();
        List<Integer> topoOrder = topoSort.sort(condensation, topoMetrics);

        assertEquals(2, topoOrder.size());

        // 4. Shortest paths
        DAGShortestPath dagSP = new DAGShortestPath();
        Metrics spMetrics = new Metrics();
        Map<Integer, Double> distances = dagSP.shortestPaths(
                condensation, topoOrder.get(0), topoOrder, spMetrics);

        assertNotNull(distances);
        assertTrue(distances.values().stream().anyMatch(d -> d != Double.POSITIVE_INFINITY));
    }

    @Test
    @DisplayName("Complete pipeline on pure DAG")
    public void testCompletePipelineDAG() {
        // Pure DAG: 0->1->3, 0->2->3
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(0, 2, 4.0);
        graph.addEdge(1, 3, 2.0);
        graph.addEdge(2, 3, 1.0);

        // 1. Find SCCs (should be all singletons)
        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        assertEquals(4, sccs.size());

        // 2. Build condensation (should be identical to original)
        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);
        assertEquals(4, condensation.getVertices());

        // 3. Topological sort
        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        List<Integer> topoOrder = topoSort.sort(condensation, new Metrics());

        assertEquals(4, topoOrder.size());

        // Find which SCC contains node 0 (source)
        int sourceSCC = -1;
        for (int i = 0; i < sccs.size(); i++) {
            if (sccs.get(i).contains(0)) {
                sourceSCC = i;
                break;
            }
        }
        assertTrue(sourceSCC >= 0, "Source node 0 should be in some SCC");

        // 4. Shortest and longest paths
        DAGShortestPath dagSP = new DAGShortestPath();
        Map<Integer, Double> shortestDist = dagSP.shortestPaths(
                condensation, sourceSCC, topoOrder, new Metrics());
        Map<Integer, Double> longestDist = dagSP.longestPaths(
                condensation, sourceSCC, topoOrder, new Metrics());

        // Find which SCC contains node 3
        int targetSCC = -1;
        for (int i = 0; i < sccs.size(); i++) {
            if (sccs.get(i).contains(3)) {
                targetSCC = i;
                break;
            }
        }
        assertTrue(targetSCC >= 0, "Target node 3 should be in some SCC");

        // Shortest: 0->1->3 = 3
        assertEquals(3.0, shortestDist.get(targetSCC), 0.001);

        // Longest: 0->2->3 = 5
        assertEquals(5.0, longestDist.get(targetSCC), 0.001);
    }

    @Test
    @DisplayName("Complete pipeline with multiple SCCs")
    public void testCompletePipelineMultipleSCCs() {
        // SCC1: 0<->1, SCC2: 2<->3, Connection: 1->2
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 0, 1.0);
        graph.addEdge(2, 3, 1.0);
        graph.addEdge(3, 2, 1.0);
        graph.addEdge(1, 2, 5.0);

        // 1. Find SCCs
        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        assertEquals(2, sccs.size());

        // 2. Build condensation
        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);
        assertEquals(2, condensation.getVertices());

        // 3. Topological sort should succeed
        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        assertDoesNotThrow(() -> {
            topoSort.sort(condensation, new Metrics());
        });
    }

    @Test
    @DisplayName("Metrics are collected throughout pipeline")
    public void testMetricsCollection() {
        Graph graph = new Graph(5);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(2, 3, 1.0);
        graph.addEdge(3, 4, 1.0);

        // Collect metrics at each stage
        Metrics sccMetrics = new Metrics();
        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, sccMetrics);

        assertTrue(sccMetrics.getDfsVisits() > 0, "DFS visits should be > 0");
        assertTrue(sccMetrics.getElapsedTimeNanos() > 0, "Elapsed time should be > 0");

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        Metrics topoMetrics = new Metrics();
        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        List<Integer> topoOrder = topoSort.sort(condensation, topoMetrics);

        assertTrue(topoMetrics.getPushOperations() > 0, "Push operations should be > 0");
        assertTrue(topoMetrics.getPopOperations() > 0, "Pop operations should be > 0");

        // Find source SCC
        int sourceSCC = -1;
        for (int i = 0; i < sccs.size(); i++) {
            if (sccs.get(i).contains(0)) {
                sourceSCC = i;
                break;
            }
        }

        Metrics spMetrics = new Metrics();
        DAGShortestPath dagSP = new DAGShortestPath();
        dagSP.shortestPaths(condensation, sourceSCC, topoOrder, spMetrics);

        assertTrue(spMetrics.getRelaxations() >= 0, "Relaxations should be >= 0");
    }

    @Test
    @DisplayName("Large graph performance test")
    public void testLargeGraph() {
        // Create a large DAG: chain of 100 nodes
        int n = 100;
        Graph graph = new Graph(n);

        for (int i = 0; i < n - 1; i++) {
            graph.addEdge(i, i + 1, 1.0);
        }

        // Run complete pipeline
        Metrics sccMetrics = new Metrics();
        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, sccMetrics);

        assertEquals(n, sccs.size(), "Should have one SCC per node");

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        Metrics topoMetrics = new Metrics();
        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        List<Integer> topoOrder = topoSort.sort(condensation, topoMetrics);

        assertEquals(n, topoOrder.size());

        // Find which SCC contains node 0
        int sourceSCC = -1;
        for (int i = 0; i < sccs.size(); i++) {
            if (sccs.get(i).contains(0)) {
                sourceSCC = i;
                break;
            }
        }
        assertTrue(sourceSCC >= 0, "Node 0 should be in some SCC");

        // Find which SCC contains last node
        int lastNodeSCC = -1;
        for (int i = 0; i < sccs.size(); i++) {
            if (sccs.get(i).contains(n - 1)) {
                lastNodeSCC = i;
                break;
            }
        }
        assertTrue(lastNodeSCC >= 0, "Last node should be in some SCC");

        Metrics spMetrics = new Metrics();
        DAGShortestPath dagSP = new DAGShortestPath();
        Map<Integer, Double> distances = dagSP.shortestPaths(
                condensation, sourceSCC, topoOrder, spMetrics);

        // Distance to last node should be n-1
        double distToLast = distances.get(lastNodeSCC);
        assertNotEquals(Double.POSITIVE_INFINITY, distToLast,
                "Last node should be reachable");
        assertEquals((double)(n - 1), distToLast, 0.001,
                "Distance should be n-1 for chain graph");

        // All operations should complete in reasonable time
        assertTrue(sccMetrics.getElapsedTimeMillis() < 1000,
                "SCC should complete in < 1s");
        assertTrue(topoMetrics.getElapsedTimeMillis() < 1000,
                "Topo sort should complete in < 1s");
        assertTrue(spMetrics.getElapsedTimeMillis() < 1000,
                "Shortest path should complete in < 1s");
    }

    @Test
    @DisplayName("Complex graph with mixed structures")
    public void testComplexMixedGraph() {
        // Complex: cycles, branches, convergence
        Graph graph = new Graph(8);

        // Cycle 1: 0->1->2->0
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(2, 0, 1.0);

        // Branch from cycle
        graph.addEdge(2, 3, 2.0);
        graph.addEdge(3, 4, 1.0);

        // Cycle 2: 5->6->5
        graph.addEdge(5, 6, 1.0);
        graph.addEdge(6, 5, 1.0);

        // Connect to cycle 2
        graph.addEdge(4, 5, 3.0);

        // Final node
        graph.addEdge(6, 7, 1.0);

        // Run complete pipeline
        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        assertTrue(sccs.size() >= 3); // At least 3 SCCs

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        List<Integer> topoOrder = assertDoesNotThrow(() ->
                topoSort.sort(condensation, new Metrics()));

        assertNotNull(topoOrder);
        assertEquals(condensation.getVertices(), topoOrder.size());
    }
}