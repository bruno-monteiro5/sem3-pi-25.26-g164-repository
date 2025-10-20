package Classes;

import java.util.ArrayList;
import java.util.List;

public class Path {
    private final String id;
    private final List<PathStep> steps = new ArrayList<>();

    public Path(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<PathStep> getSteps() {
        return steps;
    }


    public void addStep(PathStep step) {
        steps.add(step);
    }

    public boolean continuosPercurse() {
        for (int i = 0; i < steps.size() - 1; i++) {
            LineSegment currentSegment = steps.get(i).getLineSegment();
            LineSegment nextSegment = steps.get(i + 1).getLineSegment();
            if (currentSegment != nextSegment) return false;
        }
        return true;
    }

    public Endpoint getStartEndpoint() {
        if (steps.isEmpty()) return null;
        return steps.get(0).getLineSegment().getStartPoint();
    }

    public Endpoint getEndEndpoint() {
        if (steps.isEmpty()) return null;
        return steps.get(steps.size() - 1).getLineSegment().getEndPoint();
    }


}
