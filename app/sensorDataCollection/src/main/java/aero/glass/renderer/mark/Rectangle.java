package aero.glass.renderer.mark;

/**
 * Rectangle used in overlap detection.
 */
public class Rectangle {

    public Interval x;
    public Interval y;

    public void set(double left, double top, double right, double bottom) {
        x.min = left;
        x.max = right;
        y.min = top;
        y.max = bottom;
    }

    /**
     * Inner class used by overlap detection.
     */
    public static class Interval {
        public double min, max;

        Interval(double x1, double x2) {
            if (x1 < x2) {
                min = x1;
                max = x2;
            } else {
                min = x2;
                max = x1;
            }
        }

        boolean intersect(Interval i) {
            if (min > i.max) {
                return false;
            }
            return max >= i.min;
        }
    }

    /**
     * Rectangle constructor fulfilling the contract of JSI rectangles.
     *
     * @param x1
     *            x coordinate of any corner
     * @param y1
     *            y coordinate of the same corner
     * @param x2
     *            x coordinate of the opposite corner
     * @param y2
     *            y coordinate of the opposite corner
     */
    public Rectangle(double x1, double y1, double x2, double y2) {
        x = new Interval(x1, x2);
        y = new Interval(y1, y2);
    }

    /**
     * Detect intersection of two rectangles.
     *
     * @param r
     *            the other rectangle
     * @return <code>true</code> if the intersection is non-empty
     */
    public boolean intersect(Rectangle r) {
        return x.intersect(r.x) && y.intersect(r.y);
    }
}
