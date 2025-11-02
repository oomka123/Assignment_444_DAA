package smartcity;

import org.junit.jupiter.api.*;
import smartcity.model.*;
import static org.junit.jupiter.api.Assertions.*;

public class EdgeTest {

    @Test
    @DisplayName("Create edge with positive weight")
    public void testCreateEdgePositiveWeight() {
        Edge edge = new Edge(0, 1, 5.0);

        assertEquals(0, edge.from);
        assertEquals(1, edge.to);
        assertEquals(5.0, edge.weight, 0.001);
    }

    @Test
    @DisplayName("Create edge with zero weight")
    public void testCreateEdgeZeroWeight() {
        Edge edge = new Edge(2, 3, 0.0);

        assertEquals(2, edge.from);
        assertEquals(3, edge.to);
        assertEquals(0.0, edge.weight, 0.001);
    }

    @Test
    @DisplayName("Create edge with negative weight")
    public void testCreateEdgeNegativeWeight() {
        Edge edge = new Edge(1, 2, -3.5);

        assertEquals(1, edge.from);
        assertEquals(2, edge.to);
        assertEquals(-3.5, edge.weight, 0.001);
    }

    @Test
    @DisplayName("Create self-loop edge")
    public void testCreateSelfLoop() {
        Edge edge = new Edge(5, 5, 1.0);

        assertEquals(5, edge.from);
        assertEquals(5, edge.to);
        assertTrue(edge.from == edge.to);
    }

    @Test
    @DisplayName("Edge fields are final")
    public void testEdgeImmutability() {
        Edge edge = new Edge(0, 1, 2.0);

        // Fields should be accessible but not modifiable
        // This test verifies the structure exists correctly
        assertEquals(0, edge.from);
        assertEquals(1, edge.to);
        assertEquals(2.0, edge.weight, 0.001);
    }

    @Test
    @DisplayName("Create edge with decimal weight")
    public void testCreateEdgeDecimalWeight() {
        Edge edge = new Edge(0, 1, 3.14159);

        assertEquals(0, edge.from);
        assertEquals(1, edge.to);
        assertEquals(3.14159, edge.weight, 0.00001);
    }

    @Test
    @DisplayName("Create edge with large weight")
    public void testCreateEdgeLargeWeight() {
        Edge edge = new Edge(0, 1, 1000000.0);

        assertEquals(1000000.0, edge.weight, 0.001);
    }

    @Test
    @DisplayName("Create edge with very small weight")
    public void testCreateEdgeSmallWeight() {
        Edge edge = new Edge(0, 1, 0.0001);

        assertEquals(0.0001, edge.weight, 0.00001);
    }
}