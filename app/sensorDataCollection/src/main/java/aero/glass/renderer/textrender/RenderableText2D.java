package aero.glass.renderer.textrender;

import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.Vector3D;

/**
 * Created by premeczmatyas on 03/11/15.
 */
public class RenderableText2D extends RenderableText {

    private float positionX;
    private float positionY;
    private Vector3D position3 = null;

    public RenderableText2D(int[] characters, EFontTypes fontID, float posX,
                            float posY, float size, float anchorX, float anchorY) {
        super(characters, size, anchorX, anchorY, fontID);
        positionX = posX;
        positionY = posY;
    }

    public RenderableText2D(String text, EFontTypes fontID, float posX,
                            float posY, float size, float anchorX, float anchorY) {
        this(getCharCodes(text), fontID, posX, posY, size, anchorX, anchorY);
    }

    @Override
    public double getSqDistanceFromCamera(Camera c, Planet p) {
        return 0;
    }

    @Override
    public float getScreenPositionX() {
        return positionX;
    }
    @Override
    public float getScreenPositionY() {
        return positionY;
    }

    @Override
    public Vector3D getPosition(Planet planet) {
        if (position3 == null) {
            position3 = new Vector3D(positionX, positionY, 0.0);
        }
        return position3;
    }

    public void setPosition(float x, float y) {
        positionX = x;
        positionY = y;
        position3 = null;
    }
}
