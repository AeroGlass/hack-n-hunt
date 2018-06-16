package aero.glass.renderer.textrender;

import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.DefaultRenderer;
import org.glob3.mobile.generated.G3MContext;
import org.glob3.mobile.generated.G3MEventContext;
import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.GLFeatureGroupName;
import org.glob3.mobile.generated.GLFeatureID;
import org.glob3.mobile.generated.GLPrimitive;
import org.glob3.mobile.generated.GLState;
import org.glob3.mobile.generated.IFactory;
import org.glob3.mobile.generated.IFloatBuffer;
import org.glob3.mobile.generated.IShortBuffer;
import org.glob3.mobile.generated.ViewportExtentGLFeature;

import java.util.ArrayList;

/**
 * Created by premeczmatyas on 30/11/15.
 */
public class TextRenderer extends DefaultRenderer {

    private static ITextRenderCore textRenderCore = null;

    // texts to be rendered in the current frame
    private ArrayList<RenderableText> mTexts = new ArrayList<RenderableText>();
    private int screenWidth;
    private int screenHeight;
    private GLState glState;

    private int[] vertexCount = new int[EFontTypes.values().length];
    private int[] maxVertexCount = new int[EFontTypes.values().length];

    // float arrays to hold vertex data before putting into buffer
    private float[][] floatArrays = new float[EFontTypes.values().length][];
    private IFloatBuffer[] vert2Dtex2D = new IFloatBuffer[EFontTypes.values().length];
    private boolean[] dirty = new boolean[EFontTypes.values().length];

    // buffer to hold index data (0-1-2, 1-3-2, ...)
    private int maxIndices = 0;
    private IShortBuffer indices = null;

    // custom shader GLFeature
    private static TextCustomShaderGLFeature textFeature = new TextCustomShaderGLFeature();



    public static void setTextRenderCore(ITextRenderCore t) {
        textRenderCore = t;
    }

    public static boolean isTextRenderCoreAvailable() {
        return textRenderCore != null;
    }


    public TextRenderer() {
        _context = null;
        glState = new GLState();
        generateIndiceBuffer(128);
        for (int i = 0; i < EFontTypes.values().length; i++) {
            maxVertexCount[i] = 0;
            vertexCount[i] = 0;
            vert2Dtex2D[i] = null;
            floatArrays[i] = null;
            dirty[i] = false;
        }
    }

    public static ITextRenderCore getTextRenderCore() {
        return textRenderCore;
    }


    /*private void sortTextsByDistance(final Camera camera, final Planet planet) {
        Collections.sort(mTexts, new Comparator<RenderableText>() {
                    @Override
                    public int compare(RenderableText o1, RenderableText o2) {
                        return Double.compare(o1.getSqDistanceFromCamera(camera, planet),
                                o2.getSqDistanceFromCamera(camera, planet));
                    }
                }
        );
    }*/



    public void render(G3MRenderContext rc, GLState glstate) {
        glState.setParent(glstate);
        int textLen = mTexts.size();
        if (!isTextRenderCoreAvailable() || textLen == 0) {
            return;
        }

        Camera camera = rc.getCurrentCamera();
        if (glState.getGLFeature(GLFeatureID.GLF_VIEWPORT_EXTENT) == null) {
            glState.clearGLFeatureGroup(GLFeatureGroupName.NO_GROUP);
            glState.addGLFeature(
                    new ViewportExtentGLFeature(camera, rc.getViewMode()), false);
        }

      //  sortTextsByDistance(cam, planet);

        for (EFontTypes atlas : EFontTypes.values()) {
            int atlasIndex = atlas.ordinal();

            // skip empty atlas
            if (!dirty[atlasIndex]) {
                continue;
            }
            dirty[atlasIndex] = false;
            textRenderCore.selectAtlas(atlas);
            // force recreation of buffers if max number of vert2Dtex2D is exceeded
            if (vertexCount[atlasIndex] > maxVertexCount[atlasIndex]) {
                dispose();
                maxVertexCount[atlasIndex] = vertexCount[atlasIndex];
                floatArrays[atlasIndex] = new float[maxVertexCount[atlasIndex] * 4];
            }

            // create buffers for atlas
            if (vert2Dtex2D[atlasIndex] == null) {
                vert2Dtex2D[atlasIndex] =
                        IFactory.instance().createFloatBuffer(maxVertexCount[atlasIndex] * 4);
                floatArrays[atlasIndex] = new float[maxVertexCount[atlasIndex] * 4];
            }

            // generate vertex array
            int vIndex = generateVertexArray(atlas, atlasIndex);

            // draw only if there is anything to draw with this atlas
            if (vIndex > 0) {

                // put all vertex data into a buffer
                vert2Dtex2D[atlasIndex].put(floatArrays[atlasIndex]);
                vert2Dtex2D[atlasIndex].rewind();
                // setup GL state for text
                GLState textState = new GLState();
                textState.setParent(glState);
                textState.addGLFeature(textFeature, true);

                TextGeometryGLFeature tFeat = new TextGeometryGLFeature(
                        vert2Dtex2D[atlasIndex],
                        textRenderCore.getTextureId(rc).getID());

                textState.addGLFeature(tFeat, true);

                int visibleIndexCount = vIndex * 3 / 8;
                if (visibleIndexCount > maxIndices || indices == null) {
                    generateIndiceBuffer(visibleIndexCount);
                }

                // draw
                rc.getGL().drawElements(GLPrimitive.triangles(), indices, visibleIndexCount,
                        textState, rc.getGPUProgramManager());
            }
            vertexCount[atlasIndex] = 0;
        }
        mTexts.clear();
    }


    private int generateVertexArray(EFontTypes atlas, int atlasIndex) {
        int vIndex = 0;
        int textLen = mTexts.size();
        for (int ti = 0; ti < textLen; ti++) {
            RenderableText text = mTexts.get(ti);

            if (text.getFontId().equals(atlas)) {
                float screenPosX = text.getScreenPositionX();
                float screenPosY = text.getScreenPositionY();

                // early out if not visible
                int height = text.getScreenSizeY(screenHeight);
                int sizeX = text.getScreenSizeX(screenHeight);
                if (screenPosX < -sizeX || screenPosX > screenWidth + sizeX
                        || screenPosY < -height || screenPosY > screenHeight + height) {
                    continue;
                }

                // add vertex2D and texcoord 2D in one interleaved buffer
                vIndex = text.addVertices(floatArrays[atlasIndex], vIndex, screenHeight,
                        screenPosX, screenPosY);
            }
        }
        return vIndex;
    }


    private void generateIndiceBuffer(int indexCount) {
        if (indices != null) {
            indices.dispose();
        }
        indices = IFactory.instance().createShortBuffer(indexCount);
        int index = 0;
        for (short i = 0; i < indexCount / 6 * 4; i += 4) {
            indices.put(index++, i);
            indices.put(index++, (short) (i + 1));
            indices.put(index++, (short) (i + 2));
            indices.put(index++, (short) (i + 1));
            indices.put(index++, (short) (i + 3));
            indices.put(index++, (short) (i + 2));
        }
        indices.rewind();
        maxIndices = indexCount;
    }


    public void dispose() {
        for (int i = 0; i < EFontTypes.values().length; i++) {
            if (vert2Dtex2D[i] != null) {
                vert2Dtex2D[i].dispose();
                vert2Dtex2D[i] = null;
            }
        }

        if (indices != null) {
            indices.dispose();
            indices = null;
        }
    }


    public void addText(RenderableText text) {
        mTexts.add(text);
        int o = text.getFontId().ordinal();
        vertexCount[o] += text.vertexCount();
        dirty[o] = true;
    }


    public void addAllText(ArrayList<RenderableText> texts) {
        if (texts != null) {
            for (RenderableText text : texts) {
                addText(text);
            }
        }
    }


    public final void onResizeViewportEvent(G3MEventContext ec, int width, int height) {
        glState.clearGLFeatureGroup(GLFeatureGroupName.NO_GROUP);
        screenHeight = height;
        screenWidth = width;
    }


    public final void onResume(G3MContext context) {
        _context = context;
    }


    public static int getWidth(int[] chars, float size, EFontTypes labelFontId, int screen) {
        if (isTextRenderCoreAvailable()) {
            return (int) (textRenderCore.getStringWidth(
                    chars, labelFontId) * getDependableSize(size, screen) + 1f);
        }
        return 0;
    }


    public static int getHeight(float size, EFontTypes labelFontId, int screen) {
        if (isTextRenderCoreAvailable()) {
            return (int) (textRenderCore.getAtlasHeight(
                    labelFontId) * getDependableSize(size, screen) + 1f);
        }
        return 0;
    }


    public static float getDependableSize(float size, int screen) {
        return size / 700f * screen;
    }

}
