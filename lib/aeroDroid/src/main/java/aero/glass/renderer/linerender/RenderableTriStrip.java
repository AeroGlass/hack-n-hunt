package aero.glass.renderer.linerender;

import org.glob3.mobile.generated.Color;

/**
 * Created by premeczmatyas on 31/01/16.
 */
public class RenderableTriStrip extends IPattern {


    public RenderableTriStrip(Color color) {
       super(color);
    }

    public void addStrip(float[] segment) {
        checkAllocation(segment.length * 2 + 8);
        addVertices(segment);
    }


    private void addVertices(float[] line) {
        int len = line.length;

        if (vertexIndex != 0) {
            // repeat first vertex
            vertices[vertexIndex++] = line[0];
            vertices[vertexIndex++] = -line[1];
            vertices[vertexIndex++] = 1.0f;
            vertices[vertexIndex++] = 2.0f;
        }
        for (int i = 0; i < len; i += 2) {
            vertices[vertexIndex++] = line[i];
            vertices[vertexIndex++] = -line[i + 1];
            vertices[vertexIndex++] = 1.0f;
            vertices[vertexIndex++] = 2.0f;
        }

        // repeat last
        vertices[vertexIndex++] = line[len - 2];
        vertices[vertexIndex++] = -line[len - 1];
        vertices[vertexIndex++] = 1.0f;
        vertices[vertexIndex++] = 2.0f;
    }
}
