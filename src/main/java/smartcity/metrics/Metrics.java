package smartcity.metrics;

public class Metrics {
    private long dfsVisits = 0;
    private long edgesExplored = 0;
    private long pushOperations = 0;
    private long popOperations = 0;
    private long relaxations = 0;
    private long startTime = 0;
    private long endTime = 0;

    public void startTimer() {
        startTime = System.nanoTime();
    }

    public void stopTimer() {
        endTime = System.nanoTime();
    }

    public long getElapsedTimeNanos() {
        return endTime - startTime;
    }

    public double getElapsedTimeMillis() {
        return (endTime - startTime) / 1_000_000.0;
    }

    public void incrementDFSVisits() { dfsVisits++; }
    public void incrementEdgesExplored() { edgesExplored++; }
    public void incrementPushOperations() { pushOperations++; }
    public void incrementPopOperations() { popOperations++; }
    public void incrementRelaxations() { relaxations++; }

    public long getDfsVisits() { return dfsVisits; }
    public long getEdgesExplored() { return edgesExplored; }
    public long getPushOperations() { return pushOperations; }
    public long getPopOperations() { return popOperations; }
    public long getRelaxations() { return relaxations; }

    @Override
    public String toString() {
        return String.format(
                "Metrics{DFS visits=%d, edges=%d, push=%d, pop=%d, relaxations=%d, time=%.5fms}",
                dfsVisits, edgesExplored, pushOperations, popOperations, relaxations, getElapsedTimeMillis()
        );
    }
}