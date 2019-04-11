public class Segment {
    public final double low;
    public final double high;

    /**
     * creates segment by left and right frontiers
     * @param x1 left frontier
     * @param x2 right frontier
     */
    Segment(double x1, double x2) {
        low = x1;
        high = x2;
    }
}

