package aero.glass.renderer.textrender;

import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.Vector3D;

import java.util.Arrays;


/**
 * Created by premeczmatyas on 30/10/15.
 */
public abstract class RenderableText {

    protected int[] characters;

    private EFontTypes fontID;

    protected float size;

    protected float labelAnchorX;
    protected float labelAnchorY;
    private int length;

    public abstract double getSqDistanceFromCamera(Camera c, Planet p);

    public abstract float getScreenPositionX();
    public abstract float getScreenPositionY();


    @SuppressWarnings("PMD") // characters is stored directly to fight copy overhead, take care
    public RenderableText(int[] chars, float sizeIn,
                          float anchorX, float anchorY, EFontTypes font) {
        size = sizeIn;
        labelAnchorX = anchorX;
        labelAnchorY = anchorY;
        fontID = font;
        characters = chars;
        length = characters.length;
    }

    @SuppressWarnings("PMD")
    public void setCharacters(int[] chars) {
        characters = chars;
        length = characters.length;
    }

    public void setSize(float s) {
        size = s;
    }

    public void setAnchor(float x, float y) {
        labelAnchorX = x;
        labelAnchorY = y;
    }

    public static int[] getCharCodes(String t) {
        int len = t.length();
        int[] ret = new int[len];
        // get character codes
        for (int i = 0; i < len; i++) {
            ret[i] = t.charAt(i);
        }
        return ret;
    }


    // returns the index of the next empty location in the buffer
    public int addVertices(float[] buff, int indexIn, int screen,
                           float offsetX, float offsetY) {

        float dependentSize = TextRenderer.getDependableSize(size, screen);
        float letterHeight = TextRenderer.getTextRenderCore().getCharHeight()
                * dependentSize;
        float x = -getTextWidth() * labelAnchorX * dependentSize + offsetX;
        float y = getTextHeight() * labelAnchorY * dependentSize + offsetY;

        int index = indexIn;
        for (int i = 0; i < length; i++) {
            float letterWidth = TextRenderer.getTextRenderCore().getCharWidth(characters[i])
                    * dependentSize;
            float[] tex = TextRenderer.getTextRenderCore().getTexCoords(characters[i]);

            // 1.
            buff[index++] = x;
            buff[index++] = -y + letterHeight;
            buff[index++] = tex[0];
            buff[index++] = tex[1];

            // 2.
            buff[index++] = x;
            buff[index++] = -y;
            buff[index++] = tex[0];
            buff[index++] = tex[3];

            // 3.
            buff[index++] = x + letterWidth;
            buff[index++] = -y + letterHeight;
            buff[index++] = tex[2];
            buff[index++] = tex[1];

            // 4.
            buff[index++] = x + letterWidth;
            buff[index++] = -y;
            buff[index++] = tex[2];
            buff[index++] = tex[3];
            x += letterWidth;
        }
        return index;
    }


    public int getScreenSizeX(int screen) {
        return (int) (getTextWidth() * TextRenderer.getDependableSize(size, screen));
    }
    public int getScreenSizeY(int screen) {
        return (int) (getTextHeight() * TextRenderer.getDependableSize(size, screen));
    }

    public int vertexCount() {
        return 4 * length;
    }


    public EFontTypes getFontId() {
        return fontID;
    }

    public void setFontID(EFontTypes id) {
        fontID = id;
    }


    public float getWidth(int letterIndex) {
        return TextRenderer.getTextRenderCore().getCharWidth(characters[letterIndex]);
    }

    public float getTextWidth() {
        return TextRenderer.getTextRenderCore().getTextLength(characters);
    }

    public float getTextHeight() {
        return TextRenderer.getTextRenderCore().getCharHeight();
    }

    public int getLength() {
        return length;
    }

    public float getSize() {
        return size;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RenderableText
                && Arrays.equals(((RenderableText) other).characters, characters)
                && ((RenderableText) other).fontID.equals(fontID)
                && ((RenderableText) other).size == size
                && ((RenderableText) other).labelAnchorX == labelAnchorX
                && ((RenderableText) other).labelAnchorY == labelAnchorY;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + fontID.hashCode();
        result = 31 * result + Float.floatToIntBits(size);
        result = 31 * result + Float.floatToIntBits(labelAnchorX);
        result = 31 * result + Float.floatToIntBits(labelAnchorY);
        return result;
    }

    public abstract Vector3D getPosition(Planet planet);
}
