package aero.glass.renderer;

import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.GLCullFace;
import org.glob3.mobile.generated.GLFeatureGroupName;
import org.glob3.mobile.generated.GeometryGLFeature;
import org.glob3.mobile.generated.IFloatBuffer;
import org.glob3.mobile.generated.Vector3D;

/**
 * Created by DrakkLord on 2016. 06. 22..
 */
public class DirectMeshWithCullFace extends SegmentedMesh {

    private final int cullFace;

    public DirectMeshWithCullFace(int primitive, boolean owner, Vector3D center,
                                  IFloatBuffer vertices, float lineWidth, Color flatColor,
                                  boolean depthTest) {
        super(primitive, owner, center, vertices, lineWidth, 1.0f, flatColor, null,
              1.0f, depthTest, null, true);
        cullFace = GLCullFace.front();
        modifyGLState();
    }

    public DirectMeshWithCullFace(int primitive, boolean owner, Vector3D center,
                                  IFloatBuffer vertices, IFloatBuffer colors,
                                  boolean depthTest, int cullFaceIn) {
        super(primitive, owner, center, vertices, 1.0f, 1.0f, Color.white(), colors,
              1.0f, depthTest, null, true);
        cullFace = cullFaceIn;
        modifyGLState();
    }

    public DirectMeshWithCullFace(int primitive, IFloatBuffer vertices, IFloatBuffer colors,
                                  boolean depthTest, int cullFaceIn,
                                  float polygonOffsetFactor, float polygonOffsetUnits) {
        super(primitive, true, Vector3D.zero, vertices, 1.0f, 1.0f,
                Color.white(), colors,
              1.0f, depthTest, null, true,
                polygonOffsetFactor, polygonOffsetUnits, 0);
        cullFace = cullFaceIn;
        modifyGLState();
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public DirectMeshWithCullFace(int primitive, IFloatBuffer vertices, IFloatBuffer colors,
                                  boolean depthTest, int cullFaceIn,
                                  float polygonOffsetFactor, float polygonOffsetUnits,
                                  int elementVertexSize) {
        super(primitive, true, Vector3D.zero, vertices, 1.0f, 1.0f,
                Color.white(), colors,
              1.0f, depthTest, null, true,
                polygonOffsetFactor, polygonOffsetUnits, elementVertexSize);
        cullFace = cullFaceIn;
        modifyGLState();
    }

    private void modifyGLState() {
        _glState.clearGLFeatureGroup(GLFeatureGroupName.NO_GROUP);

        // Polygon Offset - Cull and culled face - Depth test - Stride 0 - Not normalized
        // - Index 0 - Our buffer contains elements of 3
        // - The attribute is a float vector of 4 elements
        _glState.addGLFeature(
                new GeometryGLFeature(_vertices, 3, 0, false, 0, _depthTest, true,
                                      cullFace,
                                      _polygonOffsetFill, _polygonOffsetFactor, _polygonOffsetUnits,
                                      _lineWidth, true, _pointSize), false);
    }
}
