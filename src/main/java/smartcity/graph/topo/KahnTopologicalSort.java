package smartcity.graph.topo;

import smartcity.model.*;
import smartcity.metrics.Metrics;
import java.util.*;

/**
 * Kahn's algorithm for topological sorting.
 * Works only on DAGs.
 */
public class KahnTopologicalSort {

    public List<Integer> sort(Graph graph, Metrics metrics) {
        int n = graph.getVertices();
        int[] inDegree = new int[n];

        // Calculate in-degrees
        for (int v = 0; v < n; v++) {
            for (Edge edge : graph.getAdjacentEdges(v)) {
                inDegree[edge.to]++;
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
                metrics.incrementPushOperations();
            }
        }

        List<Integer> topoOrder = new ArrayList<>();
        metrics.startTimer();

        while (!queue.isEmpty()) {
            int v = queue.poll();
            metrics.incrementPopOperations();
            topoOrder.add(v);

            for (Edge edge : graph.getAdjacentEdges(v)) {
                inDegree[edge.to]--;
                if (inDegree[edge.to] == 0) {
                    queue.offer(edge.to);
                    metrics.incrementPushOperations();
                }
            }
        }

        metrics.stopTimer();

        if (topoOrder.size() != n) {
            throw new IllegalStateException("Graph contains a cycle!");
        }

        return topoOrder;
    }
}