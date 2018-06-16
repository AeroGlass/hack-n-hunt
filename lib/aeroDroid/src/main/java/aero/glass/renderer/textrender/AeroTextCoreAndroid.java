package aero.glass.renderer.textrender;

import android.content.res.AssetManager;
import android.util.Log;

import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.TextureIDReference;
import org.glob3.mobile.generated.Vector2F;

import java.util.Set;
import java.util.TreeMap;


/**
 * Created by premeczmatyas on 30/10/15.
 */
public class AeroTextCoreAndroid implements ITextRenderCore {

    private AssetManager assets;
    private TreeMap<EFontTypes, FontAtlasData> fonts = new TreeMap<EFontTypes, FontAtlasData>();
    private FontAtlasData selectedAtlas;

    public AeroTextCoreAndroid(AssetManager assets) {
        this.assets = assets;
    }

    //--Load Font--//
    // description
    //    this will load the specified font file, create a texture for the defined
    //    character range, and setup all required values used to render with it.
    // arguments:
    //    file - Filename of the font (.ttf, .otf) to use. In 'Assets' folder.
    //    size - Requested pixel size of font (height)
    //    padX, padY - Extra padding per character (X+Y Axis); to prevent overlapping characters.
    public void load(EFontTypes id, String file, int  fontColor, int backColor,
                     Integer shadowColor) {
        fonts.put(id, new FontAtlasData(id, file, 32,
                fontColor, backColor, shadowColor, assets));
        Log.i("TextCore", "Font: " + file + " loaded successfully.");
    }

    public void load(EFontTypes id, String file) {
        fonts.put(id, new FontAtlasData(id, file, 32,
                0xFFFFFFFF, 0x0000000, 0x00000000, assets));
        Log.i("TextCore", "Font: " + file + " loaded successfully.");
    }


    public void selectAtlas(EFontTypes id) {
        selectedAtlas = fonts.get(id);
    }


    private FontAtlasData getAtlas(EFontTypes fontId) {
        return fonts.get(fontId);
    }


    public Set<EFontTypes> getAtlases() {
        return fonts.keySet();
    }

    @Override
    public float getAtlasHeight(EFontTypes labelFontId) {
        return getAtlas(labelFontId).getCharHeight();
    }

    @Override
    public float getStringWidth(int[] chars, EFontTypes labelFontId) {
        return getAtlas(labelFontId).getTextLength(chars);
    }

    public float getTextLength(int[] chars) {
        return selectedAtlas.getTextLength(chars);
    }

    public float getCharHeight() {
        return selectedAtlas.getCharHeight();
    }


    public Vector2F getLetterAnchor(int charIndex) {
        return selectedAtlas.getLetterAnchor(charIndex);
    }

    public float getCharWidth(int charIndex)  {
        return selectedAtlas.getCharWidths(charIndex);
    }

    public TextureIDReference getTextureId(G3MRenderContext rc) {
        return selectedAtlas.getTextureId(rc);
    }

    @Override
    public float[] getTexCoords(int charIndex) {
        return selectedAtlas.getTexCoords(charIndex);
    }
}


