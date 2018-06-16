package aero.glass.renderer.linerender;

import org.glob3.mobile.generated.Color;
//import org.glob3.mobile.generated.MutableVector2F;
//import org.glob3.mobile.generated.Vector2F;

/**
 * Created by premeczmatyas on 24/11/15.
 *
 * This class represents a collection of multi lines. All lines are drawn using the same color,
 * but their width can vary.
 *
 */
public class RenderablePattern extends IPattern {


    public RenderablePattern(Color color) {
        super(color);
    }


    public void addSegment(float[] segment, float width) {
        checkAllocation(segment.length * 8 - 16);
        addVertices(segment, width);
    }


/*
    private void addVertices(float[] line, float width) {

        float feather = 5.0f; // feather (fade out region) in pixel size;
        float dependentSize = width * PatternRenderer.screenHeight / 1000f + 2 * feather;
        float dependentWidth = dependentSize / feather;


        int len = line.length - 2;
        // normal vector of last line section
        Vector2F lastPerp = new Vector2F(0f, 0f);
        for (int i = 0; i < len; i += 2) {

            // direction vector of actual line section
            Vector2F dir1 = new Vector2F(line[i + 2] - line[i], line[i + 1] - line[i + 3]);
            // normal vector of actual line section
            Vector2F normal1 = new Vector2F(-dir1._y, dir1._x).div((float) dir1.length() / width);

            // calculate per point normals
            Vector2F pixelTangent;
            if (i == 0) {
                // special case for first point
                pixelTangent = normal1;
            } else {
                pixelTangent = lastPerp.add(normal1);
            }

            // calculate per point normals
            Vector2F pixelTangent2;
            Vector2F normal2;
            Vector2F dir2;
            if (i < len - 4) {
                // direction vector of next line section
                dir2 = new Vector2F(line[i + 4] - line[2], line[i + 3] - line[i + 5]);
                // normal vector of next line section
                normal2 = new Vector2F(-dir2._y, dir2._x).div((float) dir2.length() / width);
                pixelTangent2 = normal1.add(normal2);
            } else {
                // special case for last point
                pixelTangent2 = normal1;
                normal2 = normal1;
                dir2 = dir1;
            }
            lastPerp = normal1;

            // normalize tangents
            //pixelTangent = pixelTangent.div((float) pixelTangent.length() / dependentWidth);
            //pixelTangent2 = pixelTangent2.div((float) pixelTangent2.length() / dependentWidth);

            // calculate intersection point between first tangent and left side
            Vector2F delta1left = intersection(new Vector2F(0, 0), pixelTangent,
                    normal1, normal1.add(dir1));

            Vector2F delta1right = intersection(new Vector2F(0, 0), pixelTangent,
                    normal1.times(-1.0f), normal1.times(-1.0f).add(dir1));

            Vector2F delta2left = intersection(new Vector2F(0, 0), pixelTangent2,
                    normal1, normal1.add(dir2));

            Vector2F delta2right = intersection(new Vector2F(0, 0), pixelTangent2,
                    normal2.times(-1.0f), normal2.times(-1.0f).add(dir2));


            if (delta1left == null)
                delta1left =

            // emit vertices
            vertices[vertexIndex++] = line[i] - delta1left._x;
            vertices[vertexIndex++] = -line[i + 1] - delta1left._y;
            vertices[vertexIndex++] = 0.0f;
            vertices[vertexIndex++] = dependentWidth - 1.0f;
            vertices[vertexIndex++] = line[i] + delta1right._x;
            vertices[vertexIndex++] = -line[i + 1] + delta1right._y;
            vertices[vertexIndex++] = dependentWidth;
            vertices[vertexIndex++] = dependentWidth - 1.0f;
            vertices[vertexIndex++] = line[i + 2] - delta2left._x;
            vertices[vertexIndex++] = -line[i + 3] - delta2left._y;
            vertices[vertexIndex++] = 0.0f;
            vertices[vertexIndex++] = dependentWidth - 1.0f;
            vertices[vertexIndex++] = line[i + 2] + delta2right._x;
            vertices[vertexIndex++] = -line[i + 3] + delta2right._y;
            vertices[vertexIndex++] = dependentWidth;
            vertices[vertexIndex++] = dependentWidth - 1.0f;
        }
    }

    private Vector2F intersection(Vector2F p1, Vector2F p2, Vector2F p3, Vector2F p4) {
        // Store the values for fast access and easy
        // equations-to-code conversion
        float x1 = p1._x, x2 = p2._x, x3 = p3._x, x4 = p4._x;
        float y1 = p1._y, y2 = p2._y, y3 = p3._y, y4 = p4._y;

        float d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        // If d is zero, there is no intersection
        if (d == 0) return null;

        // Get the x and y
        float pre = (x1*y2 - y1*x2);
        float post = (x3*y4 - y3*x4);
        float x = ( pre * (x3 - x4) - (x1 - x2) * post ) / d;
        float y = ( pre * (y3 - y4) - (y1 - y2) * post ) / d;

        // Check if the x and y coordinates are within both lines
//        if ( x < min(x1, x2) || x > max(x1, x2) ||
//                x < min(x3, x4) || x > max(x3, x4) ) return NULL;
//        if ( y < min(y1, y2) || y > max(y1, y2) ||
//                y < min(y3, y4) || y > max(y3, y4) ) return NULL;

        // Return the point of intersection
        return new Vector2F(x, y);
    }
*/

    private void addVertices(float[] line, float width) {

        float feather = 1.4f; // feather (fade out region) in pixel size;
        float dependentSize = width * PatternRenderer.screenHeight / 1000f + 2 * feather;
        float dependentWidth = dependentSize / feather;
        int len = line.length - 2;

        for (int i = 0; i < len; i += 2) {
            // direction vector of actual line section
            float dirX = line[i + 2] - line[i];
            float dirY = line[i + 1] - line[i + 3];
            // create perpendicular width vector;
            float invLen = 1f / (float) Math.sqrt(dirX * dirX + dirY * dirY) * dependentSize;
            float perpX = -dirY * invLen;
            float perpY = dirX * invLen;
            vertices[vertexIndex++] = line[i] - perpX;
            vertices[vertexIndex++] = -line[i + 1] - perpY;
            vertices[vertexIndex++] = 0.0f;
            vertices[vertexIndex++] = dependentWidth - 1.0f;
            vertices[vertexIndex++] = line[i] + perpX;
            vertices[vertexIndex++] = -line[i + 1] + perpY;
            vertices[vertexIndex++] = dependentWidth;
            vertices[vertexIndex++] = dependentWidth - 1.0f;
            vertices[vertexIndex++] = line[i + 2] - perpX;
            vertices[vertexIndex++] = -line[i + 3] - perpY;
            vertices[vertexIndex++] = 0.0f;
            vertices[vertexIndex++] = dependentWidth - 1.0f;
            vertices[vertexIndex++] = line[i + 2] + perpX;
            vertices[vertexIndex++] = -line[i + 3] + perpY;
            vertices[vertexIndex++] = dependentWidth;
            vertices[vertexIndex++] = dependentWidth - 1.0f;
        }
    }
}
