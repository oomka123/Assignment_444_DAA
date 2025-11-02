package smartcity.model;

import java.util.*;

public class Graph {
    private final int vertices;
    private final List<List<Edge>> adjList;
    private final Map<Integer, String> taskNames;

    public Graph(int vertices) {
        this.vertices = vertices;
        this.adjList = new ArrayList<>(vertices);
        this.taskNames = new HashMap<>();
        for (int i = 0; i < vertices; i++) {
            adjList.add(new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, double weight) {
        adjList.get(from).add(new Edge(from, to, weight));
    }

    public void setTaskName(int vertex, String name) {
        taskNames.put(vertex, name);
    }

    public String getTaskName(int vertex) {
        return taskNames.getOrDefault(vertex, "Task" + vertex);
    }

    public int getVertices() { return vertices; }
    public List<Edge> getAdjacentEdges(int vertex) { return adjList.get(vertex); }
    public List<List<Edge>> getAdjList() { return adjList; }
}