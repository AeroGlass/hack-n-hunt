package aero.glass.renderer;

import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.GL;
import org.glob3.mobile.generated.GLCullFace;
import org.glob3.mobile.generated.GLFeatureGroupName;
import org.glob3.mobile.generated.GeometryGLFeature;
import org.glob3.mobile.generated.IFloatBuffer;
import org.glob3.mobile.generated.INativeGL;
import org.glob3.mobile.generated.Vector3D;

/**
 * Mesh that only writes to the depth buffer.
 * Created by DrakkLord on 2016. 06. 17..
 */
public class DepthOnlyMesh extends SegmentedMesh {
    private final boolean cullFront;

    protected final void rawRender(G3MRenderContext rc) {
        final GL gl = rc.getGL();
        final INativeGL ngl = gl.getNative();

        ngl.colorMask(false, false, false, false);
        super.rawRender(rc);
        ngl.colorMask(true, true, true, true);
    }

    public DepthOnlyMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         boolean depthTest, boolean cullFrontIn) {
        super(primitive, owner, center, vertices, 1.0f, 1.0f, null, null, 1.f,
              depthTest, null,
              false, 0.0f, 0.0f, 0);
        cullFront = cullFrontIn;
        modifyGLState();
    }

    public DepthOnlyMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         boolean depthTest, boolean cullFrontIn, int elementVertexSize) {
        super(primitive, owner, center, vertices, 1.0f, 1.0f, null, null, 1.f,
              depthTest, null,
              false, 0.0f, 0.0f, elementVertexSize);
        cullFront = cullFrontIn;
        modifyGLState();
    }
    private void modifyGLState() {
        _glState.clearGLFeatureGroup(GLFeatureGroupName.NO_GROUP);

        // Polygon Offset - Cull and culled face - Depth test - Stride 0 - Not normalized
        // - Index 0 - Our buffer contains elements of 3
        // - The attribute is a float vector of 4 elements
        _glState.addGLFeature(
                new GeometryGLFeature(_vertices, 3, 0, false, 0, _depthTest, cullFront,
                                      GLCullFace.front(),
                                      _polygonOffsetFill, _polygonOffsetFactor, _polygonOffsetUnits,
                                      _lineWidth, true, _pointSize), false);
    }
}
