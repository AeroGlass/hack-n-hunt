package aero.glass.math;

/**
 * Mutable real vector for efficient and accurate linear calculations.
 */
public class Vec {
    protected final double[] coord;

    /**
     * Array constructor.
     *
     * @param c coordinate array
     */
    protected Vec(final double[] c) {
        coord = new double[c.length];
        System.arraycopy(c, 0, coord, 0, c.length);
    }

    /**
     * Copy constructor.
     *
     * @param c vector to copy
     */
    public Vec(final Vec c) {
        int d = c.coord.length;
        coord = new double[d];
        System.arraycopy(c.coord, 0, coord, 0, d);
    }

    /** Copy function, assumes that Vectors are coming from the same type!
     * @param c vector to copy from
     * */
    public void copy(final Vec c) {
        System.arraycopy(c.coord, 0, coord, 0, c.coord.length);
    }

    @SuppressWarnings("PMD.AvoidArrayLoops")
    public void setFromFloat(float[] values) {
        if (coord.length != values.length) {
            throw new IllegalArgumentException("vector size not euqal");
        }

        for (int i = 0; i < values.length; i++) {
            coord[i] = values[i];
        }
    }

    /**
     * Zero vector constructor.
     *
     * @param d number of dimensions
     */
    public Vec(final int d) {
        coord = new double[d];
    }

    /**
     * Unit vector constructor.
     *
     * @param d number of dimensions
     * @param a index of unit dimension. Must be between 0 and <code>d - 1</code>, inclusive.
     */
    public Vec(final int d, final int a) {
        this(d);
        coord[a] = 1.0;
    }

    /**
     * Shorthand notation for zero vectors. Pure syntactic sugar.
     *
     * @param d number of dimensions
     * @return zero vector of length <code>d</code>
     */
    public static Vec zero(final int d) {
        return new Vec(d);
    }

    /**
     * Shorthand notation for unit vectors. Pure syntactic sugar.
     *
     * @param d number of dimensions
     * @param a index of unit dimension. Must be between 0 and <code>d - 1</code>, inclusive.
     * @return unit vector of length <code>d</code> along axis <code>a</code>
     */
    public static Vec unit(final int d, final int a) {
        return new Vec(d, a);
    }

    /**
     * Add another vector.
     *
     * @param b vector to add. Must be the same length as this one.
     * @return this vector, after update
     */
    protected Vec add(final Vec b) {
        int d = coord.length;
        while (d-- > 0) {
            coord[d] += b.coord[d];
        }
        return this;
    }

    /**
     * Subtract another vector.
     *
     * @param b vector to subtract. Must be the same length as this one.
     * @return this vector, after update;
     */
    protected Vec sub(final Vec b) {
        int d = coord.length;
        while (d-- > 0) {
            coord[d] -= b.coord[d];
        }
        return this;
    }

    /**
     * Linear scaling by a real factor.
     *
     * @param s scaling factor
     * @return this vector, after update
     */
    protected Vec scale(final double s) {
        int d = coord.length;
        while (d-- > 0) {
            coord[d] *= s;
        }
        return this;
    }

    /**
     * Coordinate-wise scaling by a (cyclic) array of real numbers.
     *
     * @param s arry of scaling factors
     * @return this vector, after update
     */
    protected Vec scale(final double[] s) {
        int d = coord.length;
        while (d-- > 0) {
            coord[d] *= s[d % s.length];
        }
        return this;
    }

    /**
     * Inner product with another vector. Leaves vector unchanged.
     *
     * @param o other vector. Must be of same length as this one.
     * @return inner product
     */
    public double dot(final Vec o) {
        int d = coord.length;
        double dot = 0.0;
        while (d-- > 0) {
            dot += coord[d] * o.coord[d];
        }
        return dot;
    }

    /**
     * @return Pythagorean norm (a.k.a. absolute value, magnitude) of this vector.
     */
    public double abs() {
        return Math.sqrt(dot(this));
    }

    /**
     * Normalization.
     *
     * @return this vector after normalization: a unit vector pointing in the same direction.
     */
    public Vec unit() {
        return scale(1.0 / abs());
    }

    /**
     * Renormalization. Fixing up unit vectors after numerical perturbations.
     *
     * @return this vector after renormalization.
     * @see #unit()
     */
    public Vec renormalize() {
        return scale(1.5 - 0.5 * dot(this));
    }

    /**
     * Invert this vector. Same magnitude, opposite direction.
     *
     * @return this vector after update
     */
    protected Vec inv() {
        int d = coord.length;
        while (d-- > 0) {
            coord[d] = -coord[d];
        }
        return this;
    }

    /**
     * Indexed coordinate.
     *
     * @param i coordinate index
     * @return indexed coordinate value
     */
    public double get(int i) {
        return coord[i];
    }
}
