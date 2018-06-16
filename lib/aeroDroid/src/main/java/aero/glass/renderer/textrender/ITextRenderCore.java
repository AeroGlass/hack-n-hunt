package aero.glass.renderer.textrender;

import org.glob3.mobile.generated.G3MRenderContext;
import org.glob3.mobile.generated.TextureIDReference;
import org.glob3.mobile.generated.Vector2F;

import java.util.Set;

/**
 * Created by premeczmatyas on 29/10/15.
 */
public interface ITextRenderCore {

    TextureIDReference getTextureId(G3MRenderContext rc);
    float[] getTexCoords(int charIndex);
    float getTextLength(int[] chars); // return length in pixel
    Vector2F getLetterAnchor(int charIndex);
    float getCharWidth(int charIndex);
    float getCharHeight();
    void selectAtlas(EFontTypes id);
    Set<EFontTypes> getAtlases();
    float getAtlasHeight(EFontTypes labelFontId);
    float getStringWidth(int[] chars, EFontTypes labelFontId);
}
