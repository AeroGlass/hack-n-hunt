package aero.glass.math;

/** Versor.
 *  quaternio class for represent orieantation sor every result of every calling must
 *  be eigen quaternio.
 */
public class Versor {

    private float q0 = 1;
    private float q1 = 0;
    private float q2 = 0;
    private float q3 = 0;

    private float[] temp = new float[9];

    private static final float EPS = 0.000001f;

    /**
     * Identity.
     * @return identity versor
     */
    public static Versor identity() {
        return new Versor(1.0f, 0.0f, 0.0f, 0.0f);
    }

    public Versor() {
        //fields has default value
    }

    /*
     * Don't use this constructor because, talking from experience speaking, the chance of mixing
     * the params up is too big. Instead  use the version with float real and Vec3D imaginary parts.
     */
    private Versor(float qq0, float qq1, float qq2, float qq3) {
        q0 = qq0;
        q1 = qq1;
        q2 = qq2;
        q3 = qq3;
    }

    public Versor(float rr, Vec3D vv) {
        q0 = rr;
        q1 = (float) vv.getX();
        q2 = (float) vv.getY();
        q3 = (float) vv.getZ();

        normalize();
    }

    public Versor(Versor v) {
        q0 = v.q0;
        q1 = v.q1;
        q2 = v.q2;
        q3 = v.q3;
    }

    public Versor(float roll, float pitch, float yaw) {
        setFromEuler((float) Math.toRadians(roll),
                (float) Math.toRadians(pitch),
                (float) Math.toRadians(yaw));

        normalize();
    }

    public void set(Versor v) {
        q0 = v.q0;
        q1 = v.q1;
        q2 = v.q2;
        q3 = v.q3;
    }

    public void set(float rr, Vec3D vv) {
        q0 = rr;
        q1 = (float) vv.getX();
        q2 = (float) vv.getY();
        q3 = (float) vv.getZ();

        normalize();
    }


    //TODO: remove these getters
    public float getQ0() {
        return q0;
    }

    public float getQ1() {
        return q1;
    }

    public float getQ2() {
        return q2;
    }

    public float getQ3() {
        return q3;
    }

    public void setFromEuler(float roll, float pitch, float yaw) {

        if (Math.abs(roll) < EPS && Math.abs(pitch) < EPS && Math.abs(yaw) < EPS) {
            q0 = 1;
            q1 = 0;
            q2 = 0;
            q3 = 0;
            return;
        }

        float angle = roll * 0.5f;
        float sinRoll = (float) Math.sin(angle);
        float cosRoll = (float) Math.cos(angle);

        angle = pitch * 0.5f;
        float sinPitch = (float) Math.sin(angle);
        float cosPitch = (float) Math.cos(angle);

        angle = yaw * 0.5f;
        float sinYaw = (float) Math.sin(angle);
        float cosYaw = (float) Math.cos(angle);

        //TODO: optimize
        q0 = cosYaw * cosPitch * cosRoll + sinYaw * sinPitch * sinRoll;
        q1 = cosYaw * cosPitch * sinRoll - sinYaw * sinPitch * cosRoll;
        q2 = cosYaw * sinPitch * cosRoll + sinYaw * cosPitch * sinRoll;
        q3 = sinYaw * cosPitch * cosRoll - cosYaw * sinPitch * sinRoll;

        normalize();
    }

    public void toEuler(float[] euler) {

        float test = q0 * q2 - q3 * q1;

        if (test < -0.4999f) {
            euler[0] = 0;
            euler[1] = (float) -(Math.PI * 0.5);
            euler[2] = (float) -(2 * Math.atan2(q3, q0));
            return;
        }

        if (test > 0.4999f) {
            euler[0] = 0;
            euler[1] = (float) (Math.PI * 0.5);
            euler[2] = (float) (2 * Math.atan2(q3, q0));
            return;
        }

        euler[0] = (float) Math.atan2(2 * (q0 * q1 + q2 * q3), 1 - 2 * (q1 * q1 + q2 * q2));
        euler[1] = (float) Math.asin(2 * test);
        euler[2] = (float) Math.atan2(2 * (q0 * q3 + q1 * q2), 1 - 2 * (q2 * q2 + q3 * q3));

    }

    private void normalize() {
        float len = (float) Math.sqrt(q0 * q0 + q1 * q1 + q2 * q2 + q3 * q3);

        len = Math.abs(len) < EPS ? 0 : 1 / len;

        q0 *= len;
        q1 *= len;
        q2 *= len;
        q3 *= len;
    }

    public void filter(Versor v, float fact) {
        final float d1 = (q0 - v.q0) * (q0 - v.q0) + (q1 - v.q1) * (q1 - v.q1)
                + (q2 - v.q2) * (q2 - v.q2) + (q3 - v.q3) * (q3 - v.q3);
        final float d2 = (q0 + v.q0) * (q0 + v.q0) + (q1 + v.q1) * (q1 + v.q1)
                + (q2 + v.q2) * (q2 + v.q2) + (q3 + v.q3) * (q3 + v.q3);
        if (d1 < d2) {
            q0 = v.q0 * fact + q0 * (1 - fact);
            q1 = v.q1 * fact + q1 * (1 - fact);
            q2 = v.q2 * fact + q2 * (1 - fact);
            q3 = v.q3 * fact + q3 * (1 - fact);
        } else {
            q0 = -v.q0 * fact + q0 * (1 - fact);
            q1 = -v.q1 * fact + q1 * (1 - fact);
            q2 = -v.q2 * fact + q2 * (1 - fact);
            q3 = -v.q3 * fact + q3 * (1 - fact);

        }
        normalize();
    }


    /**
      * Conversion to orthonormal 3D rotation matrix.
      *
      * @return roll angle in rad
    */
    public float rollRad() {

        float test = q0 * q2 - q3 * q1;

        if (test < -0.499999999f) {
            return 0;
        }

        if (test > 0.499999999f) {
            return 0;
        }

        return (float) Math.atan2(2 * (q0 * q1 + q2 * q3), 1 - 2 * (q1 * q1 + q2 * q2));
    }

    /**
     * Conversion to orthonormal 3D rotation matrix.
     *
     * @return roll angle in degrees
     */
    public float roll() {
        return (float) Math.toDegrees(rollRad());
    }

    /**
     * Rotation around lateral (X) axis.
     *
     * @return pitch angle in degrees
     */

    /**
     * Rotation around lateral (X) axis.
     *
     * @return pitch angle in rad
     */

    public float pitchRad() {

        float test = q0 * q2 - q3 * q1;

        if (test < -0.499999999f) {
            return (float) -(Math.PI * 0.5);
        }

        if (test > 0.499999999f) {
            return (float) (Math.PI * 0.5);
        }

        return (float) Math.asin(2 * test);

    }

    public float pitch() {
        return (float) Math.toDegrees(pitchRad());
    }

    /**
     * Rotation around vertical (Y) axis.
     *
     * @return yaw angle in radians
     */
    public float yawRad() {

        float test = q0 * q2 - q3 * q1;

        if (test < -0.499999999f) {
            return (float) -(2 * Math.atan2(q3, q0));
        }

        if (test > 0.499999999f) {
            return (float) (2 * Math.atan2(q3, q0));
        }

        return (float) Math.atan2(2 * (q0 * q3 + q1 * q2), 1 - 2 * (q2 * q2 + q3 * q3));
    }

    /**
     * Rotation around vertical (Y) axis.
     *
     * @return yaw angle in degrees
     */
    public float yaw() {
        return (float) Math.toDegrees(yawRad());
    }

    /** toString implementation for better representation. */
    @Override
    public String toString() {
        String rollStr = String.format("%.02f", roll());
        String pitchStr = String.format("%.02f", pitch());
        String yawStr = String.format("%.02f", yaw());

        return String.format(
                "Versor( roll: %7s  pitch: %7s  yaw: %7s )",
                rollStr, pitchStr, yawStr
        );
    }

    public void mul(Versor v) {
        float tq0 = v.q0 * q0 - v.q1 * q1 - v.q2 * q2 - v.q3 * q3;
        float tq1 = v.q0 * q1 + v.q1 * q0 - v.q2 * q3 + v.q3 * q2;
        float tq2 = v.q0 * q2 + v.q1 * q3 + v.q2 * q0 - v.q3 * q1;
        float tq3 = v.q0 * q3 - v.q1 * q2 + v.q2 * q1 + v.q3 * q0;

        q0 = tq0;
        q1 = tq1;
        q2 = tq2;
        q3 = tq3;
    }

    public void invert() {
        q1 = -q1;
        q2 = -q2;
        q3 = -q3;
    }

    public void toRotVect(float[] res) {
        float cosa = q0;
        float sina = (float) Math.sqrt(q1 * q1 + q2 * q2 + q3 * q3);
        float angle = (float)  (2 * Math.atan2(sina, cosa));
        float div = Math.abs(sina) < EPS ? 0 : angle / sina;
        res[0] = q1 * div;
        res[1] = q2 * div;
        res[2] = q3 * div;
    }

    public void setFromRotVect(float[] rv) {
        float angle = (float) Math.sqrt(rv[0] * rv[0] + rv[1] * rv[1] + rv[2] * rv[2]);
        float sina = (float) Math.sin(angle * 0.5);
        float cosa = (float) Math.cos(angle * 0.5);
        q0 = cosa;
        float div = Math.abs(angle) < EPS ? 0 : 1 / angle;
        q1 = rv[0] * div * sina;
        q2 = rv[1] * div * sina;
        q3 = rv[2] * div * sina;
    }

    public void setFromRotVect(Vec3D rv) {
        temp[0] = (float) rv.getX();
        temp[1] = (float) rv.getY();
        temp[2] = (float) rv.getZ();
        setFromRotVect(temp);
    }

    public void setYAxisRot(float alpha) {
        float[] matr = new float[9];
        matr[0] =  (float) Math.cos(alpha); matr[1] =  0; matr[2] =  (float) Math.sin(alpha);
        matr[3] =  0; matr[4] =  1; matr[5] =  0;
        matr[6] =  (float) -Math.sin(alpha); matr[7] =  0; matr[8] =  (float) Math.cos(alpha);

        setFromRotMat3x3(matr);
    }

    public void getRotMat4x4(float[] rotMat) {
        rotMat[0] = 1 - 2 * (q2 * q2 + q3 * q3);
        rotMat[1] = 2 * (q1 * q2 - q0 * q3);
        rotMat[2] = 2 * (q0 * q2 + q1 * q3);
        rotMat[3] = 0;

        rotMat[4] = 2 * (q1 * q2 + q0 * q3);
        rotMat[5] = 1 - 2 * (q1 * q1 + q3 * q3);
        rotMat[6] = 2 * (q2 * q3 - q0 * q1);
        rotMat[7] = 0;

        rotMat[8] = 2 * (q1 * q3 - q0 * q2);
        rotMat[9] = 2 * (q0 * q1 + q2 * q3);
        rotMat[10] = 1 - 2 * (q1 * q1 + q2 * q2);
        rotMat[11] = 0;

        rotMat[12] = 0;
        rotMat[13] = 0;
        rotMat[14] = 0;
        rotMat[15] = 1;
    }

    public void getRotMat3x3(float[] rotMat) {
        rotMat[0] = 1 - 2 * (q2 * q2 + q3 * q3);
        rotMat[1] = 2 * (q1 * q2 - q0 * q3);
        rotMat[2] = 2 * (q0 * q2 + q1 * q3);

        rotMat[3] = 2 * (q1 * q2 + q0 * q3);
        rotMat[4] = 1 - 2 * (q1 * q1 + q3 * q3);
        rotMat[5] = 2 * (q2 * q3 - q0 * q1);


        rotMat[6] = 2 * (q1 * q3 - q0 * q2);
        rotMat[7] = 2 * (q0 * q1 + q2 * q3);
        rotMat[8] = 1 - 2 * (q1 * q1 + q2 * q2);
    }

    public void setFromRotMat3x3(float[] m) {
        float t = m[0] + m[4] + m[8] + 1f;

        if (t > 0f) {
            final float s = 0.5f / (float) Math.sqrt(t);
            q0 = 0.25f / s;
            q1 = (m[7] - m[5]) * s;
            q2 = (m[2] - m[6]) * s;
            q3 = (m[3] - m[1]) * s;
        } else if (m[0] > m[4] && m[0] > m[8]) {
            final float s = 0.5f / (float) Math.sqrt(1.0f + m[0] - m[4] - m[8]);
            q0 = (m[7] - m[5]) * s;
            q1 = 0.25f / s;
            q2 = (m[3] + m[1]) * s;
            q3 = (m[2] + m[6]) * s;
        } else if (m[4] > m[8]) {
            final float s = 0.5f / (float) Math.sqrt(1.0f + m[4] - m[0] - m[8]);
            q0 = (m[2] - m[6]) * s;
            q1 = (m[6] + m[1]) * s;
            q2 = 0.25f / s;
            q3 = (m[7] + m[5]) * s;
        } else {
            final float s = 0.5f / (float) Math.sqrt(1.0f + m[8] - m[0] - m[4]);
            q0 = (m[3] - m[1]) * s;
            q1 = (m[2] + m[6]) * s;
            q2 = (m[7] + m[5]) * s;
            q3 = 0.25f / s;
        }
    }

    public void setFromRotMat4x4(float[] m) {
        temp[0] = m[0];
        temp[1] = m[1];
        temp[2] = m[2];
        temp[3] = m[4];
        temp[4] = m[5];
        temp[5] = m[6];
        temp[6] = m[8];
        temp[7] = m[9];
        temp[8] = m[10];
        setFromRotMat3x3(temp);
    }

    public void getAxisAngle(float[] axisAngle) {
        toRotVect(temp);
        float angle = (float) Math.sqrt(temp[0] * temp[0] + temp[1] * temp[1] + temp[2] * temp[2]);
        float div = Math.abs(angle) < EPS ? 0 : 1 / angle;
        axisAngle[3] = angle;
        axisAngle[0] = temp[0] * div;
        axisAngle[1] = temp[1] * div;
        axisAngle[2] = temp[2] * div;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Versor)) {
            return false;
        }
        final Versor comp = (Versor) o;
        return Math.abs(q0 - comp.getQ0()) <= EPS
                && Math.abs(q1 - comp.getQ1()) <= EPS
                && Math.abs(q2 - comp.getQ2()) <= EPS
                && Math.abs(q3 - comp.getQ3()) <= EPS;
    }

    @Override
    public final int hashCode() {
        throw new InternalError("hashCode not designed");
    }
}
