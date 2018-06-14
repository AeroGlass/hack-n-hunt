package aero.glass.renderer.test;

import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.DefaultRenderer;
import org.glob3.mobile.generated.G3MEventContext;
import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.GLFeatureID;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.ModelViewGLFeature;
import org.glob3.mobile.generated.MutableVector3D;

/**
 * Renderer that holds test shapes
 * Created by DrakkLord on 2015. 12. 03..
 */
public class TestShapesRenderer extends DefaultRenderer {

    private java.util.ArrayList<TestShape> shapes = new java.util.ArrayList<TestShape>();
    private GLState glState;
    private boolean enabled;

    public TestShapesRenderer() {
        glState = new GLState();
        _context = null;
        enabled = false;
    }

    public void setEnabled(boolean enable) {
        enabled = enable;
    }

    public void dispose() {
        final int shapesCount = shapes.size();
        for (int i = 0; i < shapesCount; i++) {
            TestShape shape = shapes.get(i);
            if (shape != null) {
                shape.dispose();
            }
        }
        glState._release();
        super.dispose();
    }

    private void updateGLState(G3MRenderContext rc) {
        final Camera camera = rc.getCurrentCamera();
        ModelViewGLFeature f = (ModelViewGLFeature)
                                                glState.getGLFeature(GLFeatureID.GLF_MODEL_VIEW);
        if (f == null) {
            glState.addGLFeature(new ModelViewGLFeature(camera), true);
        } else {
            f.setMatrix(camera.getModelViewMatrix44D());
        }
    }

    public final void addShape(TestShape shape) {
        shapes.add(shape);
    }

    public final void removeShape(TestShape shape) {
        int pos = -1;
        final int shapesSize = shapes.size();
        for (int i = 0; i < shapesSize; i++) {
            if (shapes.get(i) == shape) {
                pos = i;
                break;
            }
        }
        if (pos != -1) {
            shapes.remove(pos);
        }
    }

    public final void removeAllShapes() {
        removeAllShapes(true);
    }

    public final void removeAllShapes(boolean deleteShapes) {
        if (deleteShapes) {
            final int shapesCount = shapes.size();
            for (int i = 0; i < shapesCount; i++) {
                TestShape shape = shapes.get(i);
                if (shape != null) {
                    shape.dispose();
                }
            }
        }
        shapes.clear();
    }

    public final void onResizeViewportEvent(G3MEventContext ec, int width, int height) {
    }

    public final void render(G3MRenderContext rc, GLState glStateIn) {
        if (!enabled) {
            return;
        }

        MutableVector3D cameraPosition = new MutableVector3D();
        rc.getCurrentCamera().getCartesianPositionMutable(cameraPosition);

        updateGLState(rc);
        glState.setParent(glStateIn);

        final int shapesCount = shapes.size();
        for (int i = 0; i < shapesCount; i++) {
            TestShape shape = shapes.get(i);
            shape.render(rc, this.glState);
        }
    }

    public static TestShapesRenderer createTest() {
        final TestShapesRenderer r = new TestShapesRenderer();

        // shader test above LHBP
        r.addShape(
                new CustomShaderTestShape(Geodetic3D.fromDegrees(47.434203, 19.261951, 3500)));
        r.addShape(
                new GlRepeatTestShape(Geodetic3D.fromDegrees(47.434203, 19.321951, 3500)));
        return r;
    }
}
