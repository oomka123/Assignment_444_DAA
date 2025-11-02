package smartcity.util;

import com.google.gson.*;
import java.io.*;
import java.util.*;

public class DataGenerator {

    public static void generateDataset(String filename, int nodes, double density,
                                       boolean includeCycles, int numCycles) {
        JsonObject root = new JsonObject();
        JsonArray edges = new JsonArray();
        Random rand = new Random();

        // Graph parameters
        root.addProperty("directed", true);
        root.addProperty("n", nodes);
        root.addProperty("source", 0);
        root.addProperty("weight_model", "edge");

        // Edge generator
        int maxEdges = (int) (nodes * (nodes - 1) * density);
        Set<String> added = new HashSet<>();

        if (includeCycles) {
            for (int i = 0; i < numCycles; i++) {
                int cycleSize = rand.nextInt(4) + 2;
                int start = rand.nextInt(nodes - cycleSize);
                for (int j = 0; j < cycleSize; j++) {
                    JsonObject e = new JsonObject();
                    e.addProperty("u", start + j);
                    e.addProperty("v", start + ((j + 1) % cycleSize));
                    e.addProperty("w", rand.nextInt(10) + 1);
                    edges.add(e);
                    added.add((start + j) + "-" + (start + ((j + 1) % cycleSize)));
                }
            }
        }

        while (edges.size() < maxEdges) {
            int u = rand.nextInt(nodes);
            int v = rand.nextInt(nodes);
            if (u != v && !added.contains(u + "-" + v)) {
                JsonObject e = new JsonObject();
                e.addProperty("u", u);
                e.addProperty("v", v);
                e.addProperty("w", rand.nextInt(15) + 1);
                edges.add(e);
                added.add(u + "-" + v);
            }
        }

        root.add("edges", edges);

        try (FileWriter writer = new FileWriter(filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        // Small datasets
        generateDataset("data/small/tasks_small_1.json", 6, 0.3, true, 1);
        generateDataset("data/small/tasks_small_2.json", 8, 0.4, false, 0);
        generateDataset("data/small/tasks_small_3.json", 10, 0.2, true, 2);

        // Medium datasets
        generateDataset("data/medium/tasks_medium_1.json", 15, 0.2, true, 2);
        generateDataset("data/medium/tasks_medium_2.json", 18, 0.3, true, 3);
        generateDataset("data/medium/tasks_medium_3.json", 20, 0.15, false, 0);

        // Large datasets
        generateDataset("data/large/tasks_large_1.json", 30, 0.1, true, 4);
        generateDataset("data/large/tasks_large_2.json", 40, 0.08, true, 5);
        generateDataset("data/large/tasks_large_3.json", 50, 0.06, false, 0);
    }
}