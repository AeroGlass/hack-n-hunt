package aero.glass.renderer;

import org.glob3.mobile.generated.AbstractMesh;
import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.GL;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.IFloatBuffer;
import org.glob3.mobile.generated.Vector3D;

/**
 * Mesh that is capable of rendering the whole dataset or only segments of it.
 * The mesh expects segments with the same vertex count.
 * Created by DrakkLord on 2017. 11. 15..
 */
public class SegmentedMesh extends AbstractMesh {
    private final int renderVerticesCount;
    private int elementVerticesCount;
    private int numberOfRenderableElements;

    private int renderCallIndexStart;
    private int renderCallElementCount;

    @SuppressWarnings("checkstyle:parameternumber")
    public SegmentedMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         float lineWidth, float pointSize, Color flatColor, IFloatBuffer colors,
                         float colorsIntensity, boolean depthTest, IFloatBuffer normals,
                         boolean polygonOffsetFill, float polygonOffsetFactor) {

        this(primitive, owner, center, vertices, lineWidth, pointSize, flatColor, colors,
             colorsIntensity, depthTest, normals, polygonOffsetFill, polygonOffsetFactor,
             0, 0);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public SegmentedMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         float lineWidth, float pointSize, Color flatColor, IFloatBuffer colors,
                         float colorsIntensity, boolean depthTest, IFloatBuffer normals,
                         boolean polygonOffsetFill) {

        this(primitive, owner, center, vertices, lineWidth, pointSize, flatColor, colors,
             colorsIntensity, depthTest, normals, polygonOffsetFill,
             0, 0, 0);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public SegmentedMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         float lineWidth, float pointSize, Color flatColor, IFloatBuffer colors,
                         float colorsIntensity, boolean depthTest, IFloatBuffer normals) {

        this(primitive, owner, center, vertices, lineWidth, pointSize, flatColor, colors,
             colorsIntensity, depthTest, normals, false, 0, 0, 0);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public SegmentedMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         float lineWidth, float pointSize, Color flatColor, IFloatBuffer colors,
                         float colorsIntensity, boolean depthTest, int elementVertexSize) {

        this(primitive, owner, center, vertices, lineWidth, pointSize, flatColor, colors,
             colorsIntensity, depthTest, null, false, 0, 0, elementVertexSize);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public SegmentedMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         float lineWidth, float pointSize, Color flatColor, IFloatBuffer colors,
                         float colorsIntensity) {

        this(primitive, owner, center, vertices, lineWidth, pointSize, flatColor, colors,
             colorsIntensity, true, null, false, 0, 0, 0);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public SegmentedMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         float lineWidth, float pointSize, Color flatColor, IFloatBuffer colors,
                         int elementVertexSize) {

        this(primitive, owner, center, vertices, lineWidth, pointSize, flatColor, colors,
             0.0f, true, null, false, 0, 0, elementVertexSize);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public SegmentedMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         float lineWidth, float pointSize, Color flatColor) {

        this(primitive, owner, center, vertices, lineWidth, pointSize, flatColor, null,
             0.0f, true, null, false, 0, 0, 0);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public SegmentedMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         float lineWidth, float pointSize) {

        this(primitive, owner, center, vertices, lineWidth, pointSize, null, null,
             0.0f, true, null, false, 0, 0, 0);
    }

    @SuppressWarnings("checkstyle:parameternumber")
    public SegmentedMesh(int primitive, boolean owner, Vector3D center, IFloatBuffer vertices,
                         float lineWidth, float pointSize, Color flatColor, IFloatBuffer colors,
                         float colorsIntensity, boolean depthTest, IFloatBuffer normals,
                         boolean polygonOffsetFill, float polygonOffsetFactor,
                         float polygonOffsetUnits, int elementVertexSize) {

        super(primitive, owner, center, vertices, lineWidth, pointSize, flatColor, colors,
              colorsIntensity, depthTest, normals, polygonOffsetFill, polygonOffsetFactor,
              polygonOffsetUnits);

        renderVerticesCount = vertices.size() / 3;

        elementVerticesCount = elementVertexSize;
        if (elementVerticesCount > renderVerticesCount) {
            elementVerticesCount = renderVerticesCount;
        }
        if (elementVerticesCount < 0) {
            elementVerticesCount = 0;
        }

        if (elementVerticesCount != 0) {
            numberOfRenderableElements = renderVerticesCount / elementVerticesCount;
        }
        if (numberOfRenderableElements <= 0) {
            numberOfRenderableElements = 1;
        }
    }

    public final int getRenderVerticesCount() {
        return renderVerticesCount;
    }

    public final int getRenderElementVerticesCount() {
        return elementVerticesCount;
    }

    public final int getNumberOfRenderableElements() {
        return numberOfRenderableElements;
    }

    public void render(G3MRenderContext rc, GLState parentGLState,
                       int rRenderIndexStart, int rRenderElementCount) {
        if (isEnable()) {
            renderCallIndexStart = rRenderIndexStart;
            renderCallElementCount = rRenderElementCount;
            rawRender(rc, parentGLState);
        }
    }

    protected void rawRender(G3MRenderContext rc) {
        final GL gl = rc.getGL();

        int dataOffset = renderCallIndexStart * elementVerticesCount;
        if (dataOffset > renderVerticesCount || dataOffset < 0) {
            dataOffset = 0;
        }

        int renderVertCount;
        if (renderCallElementCount > 0) {
            renderVertCount = renderCallElementCount * elementVerticesCount;
            if (renderVertCount <= 0 || renderVertCount + dataOffset > renderVerticesCount) {
                renderVertCount = renderVerticesCount - dataOffset;
            }
        } else {
            renderVertCount = renderVerticesCount - dataOffset;
        }

        gl.drawArrays(_primitive, dataOffset, renderVertCount, _glState, rc.getGPUProgramManager());
    }
}
