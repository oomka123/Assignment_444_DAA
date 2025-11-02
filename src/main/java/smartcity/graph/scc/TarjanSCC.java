package smartcity.graph.scc;

import smartcity.model.*;
import java.util.*;

/**
 * Tarjan's algorithm for finding Strongly Connected Components.
 * Time complexity: O(V + E)
 */
public class TarjanSCC {
    private int[] ids;
    private int[] low;
    private boolean[] onStack;
    private Stack<Integer> stack;
    private int id;
    private int sccCount;
    private List<List<Integer>> sccs;

    public List<List<Integer>> findSCCs(Graph graph) {
        int n = graph.getVertices();
        ids = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        stack = new Stack<>();
        sccs = new ArrayList<>();
        Arrays.fill(ids, -1);
        id = 0;
        sccCount = 0;

        for (int i = 0; i < n; i++) {
            if (ids[i] == -1) {
                dfs(i, graph);
            }
        }

        return sccs;
    }

    private void dfs(int at, Graph graph) {
        stack.push(at);
        onStack[at] = true;
        ids[at] = low[at] = id++;

        for (Edge edge : graph.getAdjacentEdges(at)) {
            int to = edge.to;
            if (ids[to] == -1) {
                dfs(to, graph);
            }
            if (onStack[to]) {
                low[at] = Math.min(low[at], low[to]);
            }
        }

        // Found SCC root
        if (ids[at] == low[at]) {
            List<Integer> scc = new ArrayList<>();
            while (true) {
                int node = stack.pop();
                onStack[node] = false;
                low[node] = ids[at];
                scc.add(node);
                if (node == at) break;
            }
            sccs.add(scc);
            sccCount++;
        }
    }

    public int getSCCCount() { return sccCount; }
}