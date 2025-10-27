package domain.railway;

import java.util.List;

public class TravelTimeResult {
    private Line line;
    private Locomotive locomotive;
    private List<SegmentTime> segmentTimes;
    private double totalTimeMinutes;
    private double totalDistanceKm;

    public TravelTimeResult(Line line, Locomotive locomotive,
                            List<SegmentTime> segmentTimes) {
        this.line = line;
        this.locomotive = locomotive;
        this.segmentTimes = segmentTimes;
        this.totalTimeMinutes = segmentTimes.stream()
                .mapToDouble(SegmentTime::getTimeMinutes)
                .sum();
        this.totalDistanceKm = segmentTimes.stream()
                .mapToDouble(st -> st.getSegment().getLength() / 1000.0)
                .sum();
    }

    public Line getLine() { return line; }
    public Locomotive getLocomotive() { return locomotive; }
    public List<SegmentTime> getSegmentTimes() { return segmentTimes; }
    public double getTotalTimeMinutes() { return totalTimeMinutes; }
    public double getTotalDistanceKm() { return totalDistanceKm; }

    public static class SegmentTime {
        private LineSegment segment;
        private int speedUsed;
        private double timeMinutes;

        public SegmentTime(LineSegment segment, int speedUsed, double timeMinutes) {
            this.segment = segment;
            this.speedUsed = speedUsed;
            this.timeMinutes = timeMinutes;
        }

        public LineSegment getSegment() { return segment; }
        public int getSpeedUsed() { return speedUsed; }
        public double getTimeMinutes() { return timeMinutes; }
    }
}