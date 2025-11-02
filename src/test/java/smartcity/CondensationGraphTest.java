package smartcity;

import org.junit.jupiter.api.*;
import smartcity.graph.scc.*;
import smartcity.model.*;
import smartcity.metrics.Metrics;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class CondensationGraphTest {

    @Test
    @DisplayName("Simple cycle forms single SCC")
    public void testCondensationSimpleCycle() {
        // 0 -> 1 -> 2 -> 0
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 2.0);
        graph.addEdge(1, 2, 3.0);
        graph.addEdge(2, 0, 1.0);

        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        assertEquals(1, condensation.getVertices());
        assertEquals(0, countEdges(condensation)); // No edges in single SCC
    }

    @Test
    @DisplayName("DAG remains unchanged")
    public void testCondensationDAG() {
        // Simple DAG: 0 -> 1 -> 2
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 1.0);

        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        assertEquals(3, condensation.getVertices()); // Each node is its own SCC
        assertEquals(2, countEdges(condensation)); // Same edges
    }

    @Test
    @DisplayName("Multiple SCCs with connections")
    public void testCondensationMultipleSCCs() {
        // SCC1: 0 -> 1 -> 0
        // SCC2: 2 -> 3 -> 2
        // Connection: 1 -> 2
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 0, 1.0);
        graph.addEdge(2, 3, 1.0);
        graph.addEdge(3, 2, 1.0);
        graph.addEdge(1, 2, 5.0); // Connection between SCCs

        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        assertEquals(2, condensation.getVertices());
        assertEquals(1, countEdges(condensation)); // One edge between SCCs
    }

    @Test
    @DisplayName("Complex graph with mixed SCCs and DAG parts")
    public void testCondensationComplex() {
        // SCC1: 0 <-> 1
        // Node: 2 (alone)
        // SCC2: 3 <-> 4
        // Edges: 0->2, 2->3, 1->3
        Graph graph = new Graph(5);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 0, 1.0);
        graph.addEdge(3, 4, 1.0);
        graph.addEdge(4, 3, 1.0);
        graph.addEdge(0, 2, 2.0);
        graph.addEdge(2, 3, 3.0);
        graph.addEdge(1, 3, 4.0);

        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        assertEquals(3, condensation.getVertices()); // 3 SCCs
        assertTrue(countEdges(condensation) >= 2); // At least 2 connections
    }

    @Test
    @DisplayName("Condensation is always a DAG")
    public void testCondensationIsDAG() {
        // Create graph with cycle
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(2, 0, 1.0);
        graph.addEdge(2, 3, 1.0);

        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        // Try topological sort - should succeed if it's a DAG
        smartcity.graph.topo.KahnTopologicalSort topoSort =
                new smartcity.graph.topo.KahnTopologicalSort();

        assertDoesNotThrow(() -> {
            topoSort.sort(condensation, new Metrics());
        });
    }

    @Test
    @DisplayName("No duplicate edges in condensation")
    public void testCondensationNoDuplicates() {
        // Multiple edges between same SCCs
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 0, 1.0); // SCC {0,1}
        graph.addEdge(2, 3, 1.0);
        graph.addEdge(3, 2, 1.0); // SCC {2,3}
        graph.addEdge(0, 2, 1.0); // Edge 1: SCC1 -> SCC2
        graph.addEdge(1, 3, 1.0); // Edge 2: SCC1 -> SCC2 (duplicate)

        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        assertEquals(2, condensation.getVertices());
        assertEquals(1, countEdges(condensation)); // Only one edge between SCCs
    }

    @Test
    @DisplayName("Single node graph")
    public void testCondensationSingleNode() {
        Graph graph = new Graph(1);

        TarjanSCC sccAlgo = new TarjanSCC();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, new Metrics());

        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);

        assertEquals(1, condensation.getVertices());
        assertEquals(0, countEdges(condensation));
    }

    private int countEdges(Graph graph) {
        int count = 0;
        for (int i = 0; i < graph.getVertices(); i++) {
            count += graph.getAdjacentEdges(i).size();
        }
        return count;
    }
}