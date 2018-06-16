package aero.glass.renderer.textrender;

/**
 * Created by premeczmatyas on 17/08/16.
 * Provides an array of RenderableTexts
 */
public class RenderableTextPool {

    private RenderableText2D[] textPool;
    private int pos = 0;

    public RenderableTextPool(int maxcount, EFontTypes fontID, float size,
                              float anchorX, float anchorY) {
        textPool = new RenderableText2D[maxcount];
        for (int i = 0; i < maxcount; i++) {
            textPool[i] = new RenderableText2D("empty", fontID, 0f, 0f, size, anchorX, anchorY);
        }
    }

    public RenderableText2D next() {
        return textPool[pos++];
    }

    public void rewind() {
        pos = 0;
    }

    public RenderableText2D get(int index) {
        return textPool[index];
    }
}
