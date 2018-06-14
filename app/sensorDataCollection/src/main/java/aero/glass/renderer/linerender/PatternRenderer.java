package aero.glass.renderer.linerender;

import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.DefaultRenderer;
import org.glob3.mobile.generated.G3MEventContext;
import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.GLFeatureGroupName;
import org.glob3.mobile.generated.GLFeatureID;
import org.glob3.mobile.generated.GLPrimitive;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.IFactory;
import org.glob3.mobile.generated.IShortBuffer;
import org.glob3.mobile.generated.ViewportExtentGLFeature;

import java.util.ArrayList;

import aero.glass.renderer.test.SimpleShaderGLFeature;

/**
 * Created by premeczmatyas on 24/11/15.
 */
public class PatternRenderer extends DefaultRenderer {
    private final ArrayList<IPattern> patterns
            = new ArrayList<IPattern>();

    public static int screenWidth;
    public static int screenHeight;

    private final GLState glState;

    // index buffer used for all patterns
    private IShortBuffer indices = null;
    private int maxIndices;
    private final boolean stereo;

    // custom shader GLFeature
    private static final SimpleShaderGLFeature SMOOTH_FEATURE =
            new SimpleShaderGLFeature("smooth-pattern");


    public PatternRenderer(boolean stereo) {
        _context = null;
        glState = new GLState();
        generateIndiceBuffer(1024);
        this.stereo = stereo;
    }


    public void updateGLState(Camera cam, G3MRenderContext rc) {
        if (glState.getGLFeature(GLFeatureID.GLF_VIEWPORT_EXTENT) == null) {
            glState.clearGLFeatureGroup(GLFeatureGroupName.NO_GROUP);
            glState.addGLFeature(
                    new ViewportExtentGLFeature(cam, rc.getViewMode()), false);
        }
    }


    private void generateIndiceBuffer(int indexCount) {
        if (indices != null) {
            indices.dispose();
        }
        indices = IFactory.instance().createShortBuffer(indexCount);
        int index = 0;
        for (short i = 0; i < indexCount / 6 * 4; i += 4) {
            // CCW
            indices.put(index++, i);
            indices.put(index++, (short) (i + 2));
            indices.put(index++, (short) (i + 1));
            indices.put(index++, (short) (i + 2));
            indices.put(index++, (short) (i + 3));
            indices.put(index++, (short) (i + 1));

        }
        indices.rewind();
        maxIndices = indexCount;
    }



    public void render(G3MRenderContext rc, GLState glstate) {

        int patternNum = patterns.size();
        if (patternNum == 0) {
            return;
        }

        Camera cam = rc.getCurrentCamera();
        updateGLState(cam, rc);

        glState.setParent(glstate);
        for (int ti = patternNum - 1; ti >= 0; ti--) {
            IPattern pattern = patterns.get(ti);
            if (pattern.vertexCount() > 0) {
                // setup GL state for line
                GLState patternState = new GLState();
                patternState.setParent(glState);
                // setup smooth pattern shader
                patternState.addGLFeature(SMOOTH_FEATURE, true);
                // get geometry
                SmoothPatternGeometryGLFeature geo2D = pattern.getGeometry();
                patternState.addGLFeature(geo2D, false);
                // setup color
                pattern.addColorToGLState(patternState);

                int indexCount = pattern.vertexCount() * 3 / 2;
                if (indexCount > maxIndices) {
                    generateIndiceBuffer(indexCount);
                }

                // draw
                if (pattern instanceof RenderablePattern) {
                    rc.getGL().drawElements(GLPrimitive.triangles(), indices, indexCount,
                            patternState, rc.getGPUProgramManager());
//                    rc.getGL().drawArrays(GLPrimitive.triangleStrip(), 0, pattern.vertexCount(),
//                            patternState, rc.getGPUProgramManager());

                } else if (pattern instanceof RenderableTriStrip) {

                    rc.getGL().drawArrays(GLPrimitive.triangleStrip(), 0, pattern.vertexCount(),
                            patternState, rc.getGPUProgramManager());
                }
            }
        }
    }



    public void addPattern(IPattern pat) {
        patterns.add(pat);
    }


    public void addAllPatterns(ArrayList<IPattern> lines) {
        if (lines != null) {
            patterns.addAll(lines);
        }
    }


    public void removePattern(IPattern pattern) {
        pattern.dispose();
        patterns.remove(pattern);
    }


    public void removeAllPatterns(boolean dispose) {
        if (dispose) {
            for (IPattern pat : patterns) {
                pat.dispose();
            }
        }
        patterns.clear();
    }


    public void onResizeViewportEvent(G3MEventContext ec, int width, int height) {
        glState.clearGLFeatureGroup(GLFeatureGroupName.NO_GROUP);
        screenWidth = stereo ? width / 2 : width;
        screenHeight = height;
    }
}
