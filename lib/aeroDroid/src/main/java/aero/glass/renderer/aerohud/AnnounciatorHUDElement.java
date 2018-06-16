package aero.glass.renderer.aerohud;

import org.glob3.mobile.generated.Vector2F;

import java.util.ArrayList;
import java.util.ListIterator;

import aero.glass.renderer.textrender.EFontTypes;
import aero.glass.renderer.textrender.RenderableText2D;
import aero.glass.renderer.textrender.TextRenderer;

/**
 * This is the HUD element which renders the display messages.
 * Created by krisoft on 14/01/16.
 */
public class AnnounciatorHUDElement extends AeroHUDElement {

    private final Annunciator annunciator;
    private final ArrayList<HUDMessageRow> rows = new ArrayList<HUDMessageRow>();
    private Vector2F anchorPoint;

    private static final float FONT_SIZE = 1f;

    public AnnounciatorHUDElement(float sizeX, float sizeY, Annunciator an) {
        super(0f, 0f, sizeX, sizeY);
        annunciator = an;
        anchorPoint = new Vector2F(1f, 0f); // bottom right point of text
    }

    public void update() {
        clearTexts();

        annunciator.getAnnunciatorRows(rows);

        float posX = xCalc(1f);
        float posY = yCalc(1f);
        for (ListIterator<HUDMessageRow> i = rows.listIterator(rows.size()); i.hasPrevious();) {
            HUDMessageRow row = i.previous();

            EFontTypes font;
            switch (row.getColor()) {
                case CYAN:
                    font = EFontTypes.DEBUG_CYAN_FONT_ID;
                    break;
                case GREEN:
                    font = EFontTypes.DEBUG_GREEN_FONT_ID;
                    break;
                case MAGENTA:
                    font = EFontTypes.DEBUG_MAGENTA_FONT_ID;
                    break;
                case RED:
                    font = EFontTypes.DEBUG_RED_FONT_ID;
                    break;
                default:
                    throw new RuntimeException("No font with this color!");
            }

            texts.add(new RenderableText2D(
                    row.getMessage(),
                    font,
                    posX,
                    posY,
                    FONT_SIZE,
                    anchorPoint._x,
                    anchorPoint._y
            ));
            posY -= TextRenderer.getHeight(
                    FONT_SIZE,
                    font,
                    (int) this.sizeX
            );
        }
    }

    @Override
    public void dispose() {
    }
}
