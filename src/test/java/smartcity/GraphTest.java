package smartcity;

import org.junit.jupiter.api.*;
import smartcity.model.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class GraphTest {

    @Test
    @DisplayName("Create empty graph")
    public void testCreateEmptyGraph() {
        Graph graph = new Graph(5);

        assertEquals(5, graph.getVertices());
        for (int i = 0; i < 5; i++) {
            assertTrue(graph.getAdjacentEdges(i).isEmpty());
        }
    }

    @Test
    @DisplayName("Add single edge")
    public void testAddSingleEdge() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 5.0);

        List<Edge> edges = graph.getAdjacentEdges(0);
        assertEquals(1, edges.size());

        Edge edge = edges.get(0);
        assertEquals(0, edge.from);
        assertEquals(1, edge.to);
        assertEquals(5.0, edge.weight, 0.001);
    }

    @Test
    @DisplayName("Add multiple edges from same vertex")
    public void testAddMultipleEdges() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(0, 2, 2.0);
        graph.addEdge(0, 3, 3.0);

        List<Edge> edges = graph.getAdjacentEdges(0);
        assertEquals(3, edges.size());
    }

    @Test
    @DisplayName("Edges are directed")
    public void testDirectedEdges() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1.0);

        assertEquals(1, graph.getAdjacentEdges(0).size());
        assertEquals(0, graph.getAdjacentEdges(1).size()); // No reverse edge
    }

    @Test
    @DisplayName("Allow parallel edges")
    public void testParallelEdges() {
        Graph graph = new Graph(2);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(0, 1, 2.0);

        List<Edge> edges = graph.getAdjacentEdges(0);
        assertEquals(2, edges.size());
    }

    @Test
    @DisplayName("Allow self-loops")
    public void testSelfLoop() {
        Graph graph = new Graph(2);
        graph.addEdge(0, 0, 1.0);

        List<Edge> edges = graph.getAdjacentEdges(0);
        assertEquals(1, edges.size());

        Edge edge = edges.get(0);
        assertEquals(0, edge.from);
        assertEquals(0, edge.to);
    }

    @Test
    @DisplayName("Set and get task names")
    public void testTaskNames() {
        Graph graph = new Graph(3);
        graph.setTaskName(0, "Start");
        graph.setTaskName(1, "Process");
        graph.setTaskName(2, "End");

        assertEquals("Start", graph.getTaskName(0));
        assertEquals("Process", graph.getTaskName(1));
        assertEquals("End", graph.getTaskName(2));
    }

    @Test
    @DisplayName("Default task names")
    public void testDefaultTaskNames() {
        Graph graph = new Graph(3);

        assertEquals("Task0", graph.getTaskName(0));
        assertEquals("Task1", graph.getTaskName(1));
        assertEquals("Task2", graph.getTaskName(2));
    }

    @Test
    @DisplayName("Adjacency list is mutable")
    public void testAdjacencyListMutable() {
        Graph graph = new Graph(2);
        graph.addEdge(0, 1, 1.0);

        List<Edge> edges1 = graph.getAdjacentEdges(0);
        assertEquals(1, edges1.size());

        graph.addEdge(0, 1, 2.0);

        List<Edge> edges2 = graph.getAdjacentEdges(0);
        assertEquals(2, edges2.size());
    }

    @Test
    @DisplayName("Edge weights can be negative")
    public void testNegativeWeights() {
        Graph graph = new Graph(2);
        graph.addEdge(0, 1, -5.0);

        Edge edge = graph.getAdjacentEdges(0).get(0);
        assertEquals(-5.0, edge.weight, 0.001);
    }

    @Test
    @DisplayName("Zero vertex graph")
    public void testZeroVertexGraph() {
        Graph graph = new Graph(0);
        assertEquals(0, graph.getVertices());
    }
}