package smartcity;

import org.junit.jupiter.api.*;
import smartcity.graph.scc.*;
import smartcity.model.*;
import smartcity.metrics.Metrics;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class SCCTest {

    @Test
    public void testSimpleCycle() {
        Graph graph = new Graph(3);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(2, 0, 1.0);

        TarjanSCC scc = new TarjanSCC();
        Metrics metrics = new Metrics();
        List<List<Integer>> components = scc.findSCCs(graph, metrics);

        assertEquals(1, components.size());
        assertEquals(3, components.get(0).size());
    }

    @Test
    public void testDAG() {
        Graph graph = new Graph(4);
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(1, 3, 1.0);

        TarjanSCC scc = new TarjanSCC();
        Metrics metrics = new Metrics();
        List<List<Integer>> components = scc.findSCCs(graph, metrics);

        assertEquals(4, components.size());
    }

    @Test
    public void testMultipleSCCs() {
        Graph graph = new Graph(6);
        // First SCC: 0-1-2-0
        graph.addEdge(0, 1, 1.0);
        graph.addEdge(1, 2, 1.0);
        graph.addEdge(2, 0, 1.0);
        // Second SCC: 3-4-3
        graph.addEdge(3, 4, 1.0);
        graph.addEdge(4, 3, 1.0);
        // Connection and isolated node
        graph.addEdge(2, 3, 1.0);

        TarjanSCC scc = new TarjanSCC();
        Metrics metrics = new Metrics();
        List<List<Integer>> components = scc.findSCCs(graph, metrics);

        assertEquals(3, components.size());
    }
}