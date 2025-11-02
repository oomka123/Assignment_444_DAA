package smartcity.util;

import com.google.gson.*;
import smartcity.model.*;
import java.io.*;
import java.nio.file.*;

/**
 * Loads graph from JSON file with format:
 * {
 *   "directed": true,
 *   "n": 8,
 *   "edges": [{"u": 0, "v": 1, "w": 3}, ...],
 *   "source": 0,
 *   "weight_model": "edge"
 * }
 */
public class GraphLoader {

    public static GraphData loadFromJson(String filepath) {
        try {
            String content = Files.readString(Paths.get(filepath));
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();

            int n = root.get("n").getAsInt();
            boolean directed = root.get("directed").getAsBoolean();
            int source = root.has("source") ? root.get("source").getAsInt() : 0;
            String weightModel = root.has("weight_model") ?
                    root.get("weight_model").getAsString() : "edge";

            Graph graph = new Graph(n);

            // Load edges
            JsonArray edges = root.getAsJsonArray("edges");
            for (JsonElement edgeElement : edges) {
                JsonObject edge = edgeElement.getAsJsonObject();
                int u = edge.get("u").getAsInt();
                int v = edge.get("v").getAsInt();
                double w = edge.get("w").getAsDouble();
                graph.addEdge(u, v, w);
            }

            return new GraphData(graph, source, weightModel, directed);

        } catch (IOException e) {
            throw new RuntimeException("Failed to load graph from " + filepath, e);
        }
    }

    /**
     * Container for graph metadata
     */
    public static class GraphData {
        public final Graph graph;
        public final int source;
        public final String weightModel;
        public final boolean directed;

        public GraphData(Graph graph, int source, String weightModel, boolean directed) {
            this.graph = graph;
            this.source = source;
            this.weightModel = weightModel;
            this.directed = directed;
        }
    }
}