package smartcity.graph.dagsp;

import smartcity.model.*;
import smartcity.metrics.Metrics;
import java.util.*;

/**
 * Shortest and longest paths in a DAG using dynamic programming.
 */
public class DAGShortestPath {

    /**
     * Computes shortest paths from source using topological order.
     */
    public Map<Integer, Double> shortestPaths(Graph graph, int source,
                                              List<Integer> topoOrder, Metrics metrics) {
        int n = graph.getVertices();
        double[] dist = new double[n];
        Arrays.fill(dist, Double.POSITIVE_INFINITY);
        dist[source] = 0;

        metrics.startTimer();

        for (int u : topoOrder) {
            if (dist[u] != Double.POSITIVE_INFINITY) {
                for (Edge edge : graph.getAdjacentEdges(u)) {
                    metrics.incrementRelaxations();
                    if (dist[u] + edge.weight < dist[edge.to]) {
                        dist[edge.to] = dist[u] + edge.weight;
                    }
                }
            }
        }

        metrics.stopTimer();

        Map<Integer, Double> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(i, dist[i]);
        }
        return result;
    }

    /**
     * Computes longest path (critical path) using negated weights.
     */
    public Map<Integer, Double> longestPaths(Graph graph, int source,
                                             List<Integer> topoOrder, Metrics metrics) {
        int n = graph.getVertices();
        double[] dist = new double[n];
        Arrays.fill(dist, Double.NEGATIVE_INFINITY);
        dist[source] = 0;

        metrics.startTimer();

        for (int u : topoOrder) {
            if (dist[u] != Double.NEGATIVE_INFINITY) {
                for (Edge edge : graph.getAdjacentEdges(u)) {
                    metrics.incrementRelaxations();
                    if (dist[u] + edge.weight > dist[edge.to]) {
                        dist[edge.to] = dist[u] + edge.weight;
                    }
                }
            }
        }

        metrics.stopTimer();

        Map<Integer, Double> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(i, dist[i]);
        }
        return result;
    }
}