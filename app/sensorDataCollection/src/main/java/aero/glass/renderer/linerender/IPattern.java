package aero.glass.renderer.linerender;

import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.FlatColorGLFeature;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.IFactory;
import org.glob3.mobile.generated.IFloatBuffer;
import org.glob3.mobile.generated.Vector2F;

/**
 * Created by premeczmatyas on 31/01/16.
 */
public class IPattern {

    private IFloatBuffer vertBuffer = null;
    protected float[] vertices = null;
    protected int maxVertexIndex = 32;
    protected int vertexIndex = 0;
    protected Color color = Color.newFromRGBA(0f, 0.8f, 0f, 1f);
    private SmoothPatternGeometryGLFeature geo2Dfeature = null;
    private FlatColorGLFeature colorFeature = null;
    protected boolean blending = color._alpha != 1.0f;

    protected IPattern(Color color) {
        this.color = color;
        this.blending = color._alpha != 1.0f;
    }


    protected void checkAllocation(int segmentSize) {
        if (vertices == null) {
            vertices = new float[maxVertexIndex];
        }

        // copy all vertex data into bigger array if needed
        if (segmentSize + vertexIndex > maxVertexIndex) {
            maxVertexIndex = segmentSize + vertexIndex + 256;
            float[] newArray = new float[maxVertexIndex];
            System.arraycopy(vertices, 0, newArray, 0, vertices.length);
            dispose();
            vertices = newArray;
        }
    }


    public SmoothPatternGeometryGLFeature getGeometry() {

        if (vertBuffer == null) {
            vertBuffer = IFactory.instance().createFloatBuffer(maxVertexIndex);
            if (geo2Dfeature != null) {
                geo2Dfeature._release();
                geo2Dfeature = null;
            }
        }
        vertBuffer.put(vertices);
        vertBuffer.rewind();

        // create GL feature
        if (geo2Dfeature == null) {
            geo2Dfeature = new SmoothPatternGeometryGLFeature(vertBuffer);
        }
        return geo2Dfeature;
    }


    public void addColorToGLState(GLState state) {
        if (colorFeature == null) {
            colorFeature = new FlatColorGLFeature(color, true);
        }
        state.addGLFeature(colorFeature, true);
    }


    public void dispose() {
        if (vertBuffer != null) {
            vertBuffer.dispose();
            vertBuffer = null;
        }
        if (geo2Dfeature != null) {
            geo2Dfeature._release();
        }
        if (colorFeature != null) {
            colorFeature._release();
        }
    }


    public void reset() {
        vertexIndex = 0;
    }


    public int vertexCount() {
        return vertexIndex / 4;
    }


    public static void rotate(double alfa, Vector2F center, float[] target) {
        float cos = (float) Math.cos(alfa);
        float sin = (float) Math.sin(alfa);

        for (int i = 0; i < target.length; i += 2) {
            float x = target[i] - center._x;
            float y = target[i + 1] - center._y;
            target[i] = center._x + x * cos - y * sin;
            target[i + 1] = center._y + x * sin + y * cos;
        }
    }

    public static void rotate(double alfa, float[] target) {
        float cos = (float) Math.cos(alfa);
        float sin = (float) Math.sin(alfa);
        for (int i = 0; i < target.length; i += 2) {
            float x = target[i];
            target[i] = x * cos - target[i + 1] * sin;
            target[i + 1] = x * sin + target[i + 1] * cos;
        }
    }


    public static void translate(Vector2F dir, float[] target) {
        for (int i = 0; i < target.length; i += 2) {
            target[i] += dir._x;
            target[i + 1] += dir._y;
        }
    }
    public static void translate(float dirX, float dirY, float[] target) {
        for (int i = 0; i < target.length; i += 2) {
            target[i] += dirX;
            target[i + 1] += dirY;
        }
    }

}
