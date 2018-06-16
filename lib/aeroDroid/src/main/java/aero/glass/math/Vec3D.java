package aero.glass.math;


/**
 * A 3D vector with x, y, z coordinates.
 */
public class Vec3D extends Vec {

    /**
     * Create 3D vector from coordinates.
     *
     * @param x coordinate
     * @param y coordinate
     * @param z coordinate
     */
    public Vec3D(final double x, final double y, final double z) {
        super(new double[] {x, y, z });
    }

    /**
     * Copy constructor.
     *
     * @param o other vector
     */
    public Vec3D(final Vec3D o) {
        super(o);
    }

    /**
     * Zero vector.
     */
    public Vec3D() {
        super(3);
    }

    /**
     * Unit vector.
     *
     * @param c axis index
     */
    public Vec3D(final int c) {
        super(3, c);
    }

    /**
     * Zero vector. Syntactic sugar.
     *
     * @return zero vector
     */
    public static Vec3D zero() {
        return new Vec3D();
    }

    /**
     * Unit vector. Syntactic sugar.
     *
     * @param a axis index
     * @return Unit vector along axis <code>a</code>
     */
    public static Vec3D axis(final int a) {
        return new Vec3D(a);
    }

    public void set(double x, double y, double z) {
        coord[0] = x;
        coord[1] = y;
        coord[2] = z;
    }

    public Vec3D add(Vec3D o) {
        super.add(o);
        return this;
    }

    public Vec3D sub(Vec3D o) {
        super.sub(o);
        return this;
    }

    @Override
    public Vec3D scale(double s) {
        super.scale(s);
        return this;
    }

    @Override
    public Vec3D scale(double[] s) {
        super.scale(s);
        return this;
    }

    @Override
    public Vec3D inv() {
        super.inv();
        return this;
    }

    public double getX() {
        return coord[0];
    }

    public double getY() {
        return coord[1];
    }

    public double getZ() {
        return coord[2];
    }

    /**
     * Cross product. Does not alter this vector.
     *
     * @param o other vector
     * @return cross product of this and the other vector
     */
    public Vec3D cross(final Vec3D o) {
        return new Vec3D(getY() * o.getZ() - getZ() * o.getY(),
                         getZ() * o.getX() - getX() * o.getZ(),
                         getX() * o.getY() - getY() * o.getX());
    }

    public void scalarMul(float s) {
        coord[0] *= s;
        coord[1] *= s;
        coord[2] *= s;
    }

    /**
     * @see Vec#unit()
     */
    @Override
    public Vec3D unit() {
        super.unit();
        return this;
    }

    public double len() {
        return Math.sqrt(getX() * getX() + getY() * getY() + getZ() * getZ());
    }

    public void normalize() {
        double div = len() < 0.000001 ? 0 : 1 / len();
        coord[0] *= div;
        coord[1] *= div;
        coord[2] *= div;
    }
}
