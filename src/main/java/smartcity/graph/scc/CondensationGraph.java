package smartcity.graph.scc;

import smartcity.model.*;
import java.util.*;

/**
 * Builds condensation graph from SCCs.
 * Each SCC becomes a single vertex in the DAG.
 */
public class CondensationGraph {

    public static Graph buildCondensation(Graph original, List<List<Integer>> sccs) {
        int sccCount = sccs.size();
        Graph condensation = new Graph(sccCount);

        // Map each vertex to its SCC index
        int[] vertexToSCC = new int[original.getVertices()];
        for (int i = 0; i < sccs.size(); i++) {
            for (int vertex : sccs.get(i)) {
                vertexToSCC[vertex] = i;
            }
            condensation.setTaskName(i, "SCC" + i + "(size=" + sccs.get(i).size() + ")");
        }

        // Add edges between different SCCs
        Set<String> addedEdges = new HashSet<>();
        for (int v = 0; v < original.getVertices(); v++) {
            int fromSCC = vertexToSCC[v];
            for (Edge edge : original.getAdjacentEdges(v)) {
                int toSCC = vertexToSCC[edge.to];
                if (fromSCC != toSCC) {
                    String edgeKey = fromSCC + "->" + toSCC;
                    if (!addedEdges.contains(edgeKey)) {
                        condensation.addEdge(fromSCC, toSCC, edge.weight);
                        addedEdges.add(edgeKey);
                    }
                }
            }
        }

        return condensation;
    }
}