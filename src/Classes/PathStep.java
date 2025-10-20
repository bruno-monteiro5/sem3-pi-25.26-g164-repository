package Classes;

public class PathStep {
    private final int order;
    private final LineSegment lineSegment;

    public PathStep(int order, LineSegment lineSegment) {
        this.order = order;
        this.lineSegment = lineSegment;
    }

    public int getOrder() {
        return order;
    }

    public LineSegment getLineSegment() {
        return lineSegment;
    }

}
