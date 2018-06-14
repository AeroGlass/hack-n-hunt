package aero.glass.renderer.aerohud;

import java.util.ArrayList;

import aero.glass.primary.G3MComponent;
import aero.glass.renderer.linerender.IPattern;
import aero.glass.renderer.textrender.RenderableText;

/**
 * Created by premeczmatyas on 21/11/15.
 */
public abstract class AeroHUDElement {

    protected ArrayList<RenderableText> texts = new ArrayList<RenderableText>();
    protected final float posY;
    protected final float posX;
    // desired aspect ratio for this element
    protected float sizeX;
    protected float sizeY;
    protected float aspectRatio;

    //
    // screen position calculators with 0,0 as top,left corner of area
    //
    protected float xCalc(float x) {
        return posX + sizeX * x;
    }
    protected float yCalc(float y) {
        return posY + sizeY * y;
    }
    //
    // screen position calculators with 0,0 as middle of area
    //
    protected float xCalc1(float x) {
        return posX + sizeX * (x + 0.5f);
    }
    protected float yCalc1(float y) {
        return posY + sizeY * (y + 0.5f);
    }
    //
    // aspect ratio corrected screen position calculators with 0,0 as middle point of area
    //
    protected float xCalcA(float x) {
        if (aspectRatio > 1.0f) {
            return xCalc1(x / aspectRatio);
        } else {
            return xCalc1(x);
        }
    }

    protected float yCalcA(float y) {
        if (aspectRatio > 1.0f) {
            return yCalc1(y);
        } else {
            return yCalc1(y / aspectRatio);
        }
    }


    public AeroHUDElement(float posX, float posY, float sizeX, float sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.aspectRatio = sizeX / sizeY;
        this.posX = posX;
        this.posY = posY;
    }

    public ArrayList<RenderableText> getTexts() {
        return texts;
    }

    public void clearTexts() {
        texts.clear();
    }

    public abstract void dispose();
}
