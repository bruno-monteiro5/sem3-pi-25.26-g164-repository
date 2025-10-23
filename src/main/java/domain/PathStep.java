package domain;

public class PathStep {
    private  int order;
    private  LineSegment lineSegment;

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
