package aero.glass.renderer.textrender;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.GLFormat;
import org.glob3.mobile.generated.TextureIDReference;
import org.glob3.mobile.generated.Vector2F;
import org.glob3.mobile.specific.Image_Android;


/**
 * Created by premeczmatyas on 04/11/15.
 */
public class FontAtlasData {

    //--Constants--//
    public static final int CHAR_START = 32;           // First Character (ASCII Code)
    public static final int CHAR_END = 255;            // Last Character (ASCII Code)
    public static final int CHAR_CNT = CHAR_END - CHAR_START + 2;
    // Character Count (Including Character to use for Unknown)
    public static final int CHAR_NONE = 32;            // Character to Use for Unknown (ASCII Code)
    public static final int CHAR_UNKNOWN = (CHAR_CNT - 1);  // Index of the Unknown Character

    private float[][] texCoordBuffers;
    private Vector2F[] letterAnchors;
    private Bitmap fontAtlas = null;
    private TextureIDReference textureId = null;
    private float charHeight;
    private final float[] charWidths;
    private int textureSize;
    private EFontTypes id;


    public static final int getCharArrayIndex(int index) {
        int c = index - FontAtlasData.CHAR_START;
        return (c > FontAtlasData.CHAR_CNT || c <= 0)
                ? FontAtlasData.CHAR_UNKNOWN
                : c;
    }

    public FontAtlasData(EFontTypes idIn, String file, int size,
                         int fontColor, int backgroundColor,
                         Integer shadowColor, AssetManager assets) {

        charWidths = new float[CHAR_CNT];
        texCoordBuffers = new float[CHAR_CNT][];
        letterAnchors = new Vector2F[CHAR_CNT];
        id = idIn;
        // load the font and setup paint instance for drawing
        Typeface tf = Typeface.createFromAsset(assets, file);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(size);
        paint.setFakeBoldText(true);
        paint.setColor(fontColor);
        paint.setTypeface(tf);
        if (shadowColor != null) {
            paint.setShadowLayer(1f, 2f, 2f, shadowColor);
        }

        // get font metrics
        Paint.FontMetrics fm = paint.getFontMetrics();
        float fontHeight = (float) Math.ceil(Math.abs(fm.bottom) + Math.abs(fm.top));
        float fontDescent = (float) Math.ceil(Math.abs(fm.descent));

        // determine the width of each character (including unknown character)
        // also determine the maximum character width
        char[] s = new char[2];                         // Create Character Array
        float charWidthMax = charHeight = 0.0f;
        float[] w = new float[2];                       // Working Width Value
        int cnt = 0;                                    // Array Counter
        for (char c = CHAR_START; c <= CHAR_END; c++)  {  // FOR Each Character
            s[0] = c;                                    // Set Character
            paint.getTextWidths(s, 0, 1, w);           // Get Character Bounds
            charWidths[cnt] = w[0] + 2;                      // Get Width
            if (charWidths[cnt] > charWidthMax) {       // IF Width Larger Than Max Width
                charWidthMax = charWidths[cnt];           // Save New Max Width
            }
            cnt++;                                       // Advance Array Counter
        }
        s[0] = CHAR_NONE;                               // Set Unknown Character
        paint.getTextWidths(s, 0, 1, w);              // Get Character Bounds
        charWidths[cnt] = w[0] + 2;                         // Get Width
        if (charWidths[cnt] > charWidthMax) {         // IF Width Larger Than Max Width
            charWidthMax = charWidths[cnt];
        }

        // set character height to font height
        charHeight = fontHeight;                        // Set Character Height
        int fontPadX = 0;
        int fontPadY = 0;

        // find the maximum size, validate, and setup cell sizes
        int cellWidth = (int) charWidthMax + (2 * fontPadX);  // Set Cell Width
        int cellHeight = (int) charHeight + (2 * fontPadY);  // Set Cell Height
        int maxSize = cellWidth > cellHeight ? cellWidth : cellHeight;

        // set texture size based on max font size (width or height)
        // NOTE: these values are fixed, based on the defined characters. when
        // changing start/end characters (CHAR_START/CHAR_END) this will need adjustment too!
        if (maxSize <= 24) {
            textureSize = 256;
        } else if (maxSize <= 40) {
            textureSize = 512;
        } else if (maxSize <= 80) {
            textureSize = 1024;
        } else {
            textureSize = 2048;
        }

        // create an empty bitmap (alpha only)
        Bitmap bitmap = Bitmap.createBitmap(textureSize, textureSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        bitmap.eraseColor(backgroundColor);

        // render each of the characters to the canvas (ie. build the font map)
        float x = fontPadX;                             // Set Start Position (X)
        float y = (cellHeight - 1) - fontDescent - fontPadY;  // Set Start Position (Y)
        for (char c = CHAR_START; c <= CHAR_END; c++)  {  // FOR Each Character
            s[0] = c;                                    // Set Character to Draw
            canvas.drawText(s, 0, 1, x, y, paint);     // Draw Character
            x += cellWidth;                              // Move to Next Character
            if ((x + cellWidth - fontPadX) > textureSize)  {  // IF End of Line Reached
                x = fontPadX;                             // Set X for New Row
                y += cellHeight;                          // Move Down a Row
            }
        }
        s[0] = CHAR_NONE;                               // Set Character to Use for NONE
        canvas.drawText(s, 0, 1, x, y, paint);        // Draw Character


        // setup the array of character texture regions
        x = 0;
        y = 0;
        float texSize = (float) textureSize;
        for (int c = 0; c < CHAR_CNT; c++)  {

            float u1 = (x) / texSize;
            float u2 = u1 + charWidths[c] / texSize;
            float v1 = y / texSize;
            float v2 = v1 + (cellHeight - 1) / texSize;

            texCoordBuffers[c] = new float[4];
            texCoordBuffers[c][0] = u1;
            texCoordBuffers[c][1] = v1;
            texCoordBuffers[c][2] = u2;
            texCoordBuffers[c][3] = v2;
            letterAnchors[c] = new Vector2F(u1, v1);

            // move to next char
            x += cellWidth;
            if (x + cellWidth > textureSize)  {
                x = 0;
                y += cellHeight;
            }
        }
        fontAtlas = bitmap;
    }

    public final float getCharHeight() {
        return charHeight;
    }

    public final float getCharWidths(int charIndex) {
        int c = getCharArrayIndex(charIndex);
        return charWidths[c];
    }

    public final int getTextureSize() {
        return textureSize;
    }


    @SuppressWarnings("PMD")  // exposing texCoordBuffer is to prevent unnecessary array copy
    public final float[] getTexCoords(int charIndex) {
        int c = getCharArrayIndex(charIndex);
        return texCoordBuffers[c];
    }

    public final Vector2F getLetterAnchor(int charIndex) {
        int c = getCharArrayIndex(charIndex);
        return letterAnchors[c];
    }

    public final float getTextLength(int[] chars) {
        float len = 0.0f;
        int strLen = chars.length;
        for (int i = 0; i < strLen; i++)  {
            int c = chars[i] - FontAtlasData.CHAR_START;
            if (c > FontAtlasData.CHAR_CNT || c <= 0) {
                c = FontAtlasData.CHAR_UNKNOWN;
            }
            len += (charWidths[c]);
        }
        return len;
    }

    public void initTexture(G3MRenderContext rc) {
        textureId = rc.getTexturesHandler().getTextureIDReference(
                new Image_Android(fontAtlas, null),
                GLFormat.rgba(), "font_atlas_" + id, false);
    }

    public final TextureIDReference getTextureId(G3MRenderContext rc) {
        if (textureId == null) {
            initTexture(rc);
        }
        return textureId;
    }
}
