package domain.railway;

import java.util.ArrayList;
import java.util.List;

public class Path {
    private int id;
    private String name;
    private List<PathEndpoint> endpoints;

    public Path(int id, String name) {
        this.id = id;
        this.name = name;
        this.endpoints = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public List<PathEndpoint> getEndpoints() { return endpoints; }

    public void addEndpoint(PathEndpoint pathEndpoint) {
        this.endpoints.add(pathEndpoint);
    }

    /**
     * Returns the list of endpoints ordered by sequence
     */
    public List<PathEndpoint> getOrderedEndpoints() {
        return endpoints.stream()
                .sorted((e1, e2) -> Integer.compare(e1.getSequence(), e2.getSequence()))
                .toList();
    }

    /**
     * Returns only the endpoints where the train must stop
     */
    public List<PathEndpoint> getStopEndpoints() {
        return endpoints.stream()
                .filter(PathEndpoint::isMustStop)
                .sorted((e1, e2) -> Integer.compare(e1.getSequence(), e2.getSequence()))
                .toList();
    }

    @Override
    public String toString() {
        return String.format("Path[id=%d, name=%s, endpoints=%d]",
                id, name, endpoints.size());
    }

    /**
     * Internal class to represent an endpoint in a path
     */
    public static class PathEndpoint {
        private int sequence;
        private Endpoint endpoint;
        private boolean mustStop;

        public PathEndpoint(int sequence, Endpoint endpoint, boolean mustStop) {
            this.sequence = sequence;
            this.endpoint = endpoint;
            this.mustStop = mustStop;
        }

        public int getSequence() { return sequence; }
        public Endpoint getEndpoint() { return endpoint; }
        public boolean isMustStop() { return mustStop; }

        @Override
        public String toString() {
            return String.format("%d. %s%s",
                    sequence,
                    endpoint.getName(),
                    mustStop ? " [STOP]" : " [PASS]");
        }
    }
}