package smartcity;

import org.junit.jupiter.api.*;
import smartcity.graph.topo.*;
import smartcity.model.*;
import smartcity.metrics.Metrics;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class TopologicalSortTest {

    @Test
    @DisplayName("Simple linear DAG")
    public void testTopologicalSortSimpleLinear() {
        // 0 -> 1 -> 2 -> 3
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(2, 3, 1.0);

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics metrics = new Metrics();
        List<Integer> order = topoSort.sort(graph, metrics);

        assertEquals(4, order.size());
        assertEquals(0, order.get(0)); // 0 must come first
        assertEquals(3, order.get(3)); // 3 must come last
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(1) < order.indexOf(2));
        assertTrue(order.indexOf(2) < order.indexOf(3));

        // Verify metrics
        assertTrue(metrics.getPushOperations() >= 4);
        assertTrue(metrics.getPopOperations() == 4);
        assertTrue(metrics.getElapsedTimeNanos() > 0);
    }

    @Test
    @DisplayName("DAG with multiple valid orderings")
    public void testTopologicalSortDiamond() {
        // Diamond: 0 -> 1,2 -> 3
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(0, 2, 1.0);
        graph.addEdge(1, 3, 1.0);
        graph.addEdge(2, 3, 1.0);

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics metrics = new Metrics();
        List<Integer> order = topoSort.sort(graph, metrics);

        assertEquals(4, order.size());
        assertEquals(0, order.get(0)); // 0 must be first
        assertEquals(3, order.get(3)); // 3 must be last
        // 1 and 2 can be in any order, but both must come after 0 and before 3
        assertTrue(order.indexOf(1) > order.indexOf(0));
        assertTrue(order.indexOf(2) > order.indexOf(0));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    @DisplayName("DAG with disconnected components")
    public void testTopologicalSortDisconnected() {
        // Two separate chains: 0->1 and 2->3
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(2, 3, 1.0);

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics metrics = new Metrics();
        List<Integer> order = topoSort.sort(graph, metrics);

        assertEquals(4, order.size());
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    @DisplayName("Single node graph")
    public void testTopologicalSortSingleNode() {
        Graph graph = new Graph(1);

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics metrics = new Metrics();
        List<Integer> order = topoSort.sort(graph, metrics);

        assertEquals(1, order.size());
        assertEquals(0, order.get(0));
    }

    @Test
    @DisplayName("Empty graph")
    public void testTopologicalSortEmptyGraph() {
        Graph graph = new Graph(0);

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics metrics = new Metrics();
        List<Integer> order = topoSort.sort(graph, metrics);

        assertEquals(0, order.size());
    }

    @Test
    @DisplayName("Graph with self-loop should throw exception")
    public void testTopologicalSortWithSelfLoop() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 1, 1.0); // Self-loop
        graph.addEdge(1, 2, 1.0);

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics metrics = new Metrics();

        assertThrows(IllegalStateException.class, () -> {
            topoSort.sort(graph, metrics);
        });
    }

    @Test
    @DisplayName("Graph with simple cycle should throw exception")
    public void testTopologicalSortWithCycle() {
        // Cycle: 0 -> 1 -> 2 -> 0
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(2, 0, 1.0);

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics metrics = new Metrics();

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            topoSort.sort(graph, metrics);
        });

        assertTrue(exception.getMessage().contains("cycle"));
    }

    @Test
    @DisplayName("Complex DAG with multiple paths")
    public void testTopologicalSortComplex() {
        // 0 -> 1 -> 3
        // 0 -> 2 -> 3
        // 1 -> 4
        // 2 -> 4
        // 3 -> 5
        // 4 -> 5
        Graph graph = new Graph(6);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(0, 2, 1.0);
        graph.addEdge(1, 3, 1.0);
        graph.addEdge(2, 3, 1.0);
        graph.addEdge(1, 4, 1.0);
        graph.addEdge(2, 4, 1.0);
        graph.addEdge(3, 5, 1.0);
        graph.addEdge(4, 5, 1.0);

        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics metrics = new Metrics();
        List<Integer> order = topoSort.sort(graph, metrics);

        assertEquals(6, order.size());

        // Verify all precedence constraints
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
        assertTrue(order.indexOf(1) < order.indexOf(4));
        assertTrue(order.indexOf(2) < order.indexOf(4));
        assertTrue(order.indexOf(3) < order.indexOf(5));
        assertTrue(order.indexOf(4) < order.indexOf(5));
    }
}