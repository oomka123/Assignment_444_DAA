package smartcity;

import smartcity.graph.scc.*;
import smartcity.graph.topo.*;
import smartcity.graph.dagsp.*;
import smartcity.model.*;
import smartcity.metrics.*;
import smartcity.util.*;

import java.io.File;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String filepath;

        if (args.length < 1) {
            System.out.println("Select a data set:");
            System.out.println("1. small");
            System.out.println("2. medium");
            System.out.println("3. large");
            System.out.print("Your choice (1-3): ");
            int sizeChoice = scanner.nextInt();

            String sizeFolder;
            switch (sizeChoice) {
                case 1: sizeFolder = "small"; break;
                case 2: sizeFolder = "medium"; break;
                case 3: sizeFolder = "large"; break;
                default:
                    System.out.println("Wrong choice. Small is used.");
                    sizeFolder = "small";
            }

            System.out.print("Enter the file number (1-3): ");
            int fileNum = scanner.nextInt();

            filepath = String.format("data/%s/tasks_%s_%d.json", sizeFolder, sizeFolder, fileNum);
            File file = new File(filepath);
            if (!file.exists()) {
                System.out.println("File not found: " + filepath);
                return;
            }
        } else {
            filepath = args[0];
        }

//        String filepath = args[0];
        System.out.println("=================================================");
        System.out.println("Loading graph from: " + filepath);
        System.out.println("=================================================\n");

        // Load graph
        GraphLoader.GraphData data = GraphLoader.loadFromJson(filepath);
        Graph graph = data.graph;
        int source = data.source;

        System.out.println("Graph properties:");
        System.out.println("  Vertices (n): " + graph.getVertices());
        System.out.println("  Directed: " + data.directed);
        System.out.println("  Weight model: " + data.weightModel);
        System.out.println("  Source node: " + source);

        // Count edges
        int edgeCount = 0;
        for (int i = 0; i < graph.getVertices(); i++) {
            edgeCount += graph.getAdjacentEdges(i).size();
        }
        System.out.println("  Edges: " + edgeCount);
        System.out.println();

        // 1. Find SCCs
        System.out.println("=== STRONGLY CONNECTED COMPONENTS ===");
        TarjanSCC sccAlgo = new TarjanSCC();
        Metrics sccMetrics = new Metrics();
        List<List<Integer>> sccs = sccAlgo.findSCCs(graph, sccMetrics);

        System.out.println("Found " + sccs.size() + " SCC(s):");
        for (int i = 0; i < sccs.size(); i++) {
            List<Integer> scc = sccs.get(i);
            System.out.print("  SCC " + i + ": " + scc);
            if (scc.size() > 1) {
                System.out.println(" <- CYCLE DETECTED (size=" + scc.size() + ")");
            } else {
                System.out.println();
            }
        }
        System.out.println("\nMetrics: " + sccMetrics);
        System.out.println();

        // 2. Build condensation
        System.out.println("=== CONDENSATION GRAPH ===");
        Graph condensation = CondensationGraph.buildCondensation(graph, sccs);
        System.out.println("Condensation has " + condensation.getVertices() +
                " vertices (meta-nodes)");

        // Count condensation edges
        int condEdgeCount = 0;
        for (int i = 0; i < condensation.getVertices(); i++) {
            condEdgeCount += condensation.getAdjacentEdges(i).size();
        }
        System.out.println("Condensation has " + condEdgeCount + " edges\n");

        // 3. Topological sort
        System.out.println("=== TOPOLOGICAL SORT ===");
        KahnTopologicalSort topoSort = new KahnTopologicalSort();
        Metrics topoMetrics = new Metrics();

        try {
            List<Integer> topoOrder = topoSort.sort(condensation, topoMetrics);
            System.out.println("Topological order (SCC indices): " + topoOrder);
            System.out.println("Metrics: " + topoMetrics);
            System.out.println();

            // Map source to its SCC
            int sourceSCC = -1;
            for (int i = 0; i < sccs.size(); i++) {
                if (sccs.get(i).contains(source)) {
                    sourceSCC = i;
                    break;
                }
            }

            // 4. Shortest paths
            System.out.println("=== DAG SHORTEST PATHS ===");
            System.out.println("Source node: " + source + " (in SCC " + sourceSCC + ")");
            DAGShortestPath dagSP = new DAGShortestPath();
            Metrics spMetrics = new Metrics();
            Map<Integer, Double> shortestDist =
                    dagSP.shortestPaths(condensation, sourceSCC, topoOrder, spMetrics);

            System.out.println("\nShortest distances from source:");
            for (int sccIdx : topoOrder) {
                double dist = shortestDist.get(sccIdx);
                if (dist != Double.POSITIVE_INFINITY) {
                    System.out.printf("  To SCC %d: %.2f (contains nodes: %s)%n",
                            sccIdx, dist, sccs.get(sccIdx));
                }
            }
            System.out.println("\nMetrics: " + spMetrics);
            System.out.println();

            // 5. Longest path (critical path)
            System.out.println("=== DAG LONGEST PATH (Critical Path) ===");
            Metrics lpMetrics = new Metrics();
            Map<Integer, Double> longestDist =
                    dagSP.longestPaths(condensation, sourceSCC, topoOrder, lpMetrics);

            double maxDist = Double.NEGATIVE_INFINITY;
            int criticalEnd = -1;

            System.out.println("\nLongest distances from source:");
            for (int sccIdx : topoOrder) {
                double dist = longestDist.get(sccIdx);
                if (dist != Double.NEGATIVE_INFINITY) {
                    System.out.printf("  To SCC %d: %.2f (contains nodes: %s)%n",
                            sccIdx, dist, sccs.get(sccIdx));
                    if (dist > maxDist) {
                        maxDist = dist;
                        criticalEnd = sccIdx;
                    }
                }
            }

            System.out.println("\n*** CRITICAL PATH ***");
            System.out.printf("  Length: %.2f%n", maxDist);
            System.out.printf("  Ends at SCC %d (nodes: %s)%n",
                    criticalEnd, sccs.get(criticalEnd));
            System.out.println("\nMetrics: " + lpMetrics);

        } catch (IllegalStateException e) {
            System.out.println("ERROR: " + e.getMessage());
            System.out.println("Cannot perform topological sort on a graph with cycles!");
            System.out.println("The condensation graph should be a DAG, but cycles were found.");
        }

        System.out.println("\n=================================================");
        System.out.println("Analysis complete!");
        System.out.println("=================================================");
    }
}