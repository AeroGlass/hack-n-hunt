package aero.glass.renderer.textrender;

import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.Vector3D;

/**
 * Created by premeczmatyas on 03/11/15.
 */
public class RenderableText3D extends RenderableText {

    private final Geodetic3D position;

    public RenderableText3D(String text, EFontTypes fontID, Geodetic3D pos,
                            float size, float anchorX, float anchorY) {
        super(getCharCodes(text), size, anchorX, anchorY, fontID);
        position = pos;
    }

    public RenderableText3D(String text, EFontTypes fontID, Geodetic3D pos,
                            float size) {
        // defaults position pivot to center of text
        this(text, fontID, pos, size, 0.5f, 0.5f);
    }

    @Override
    public double getSqDistanceFromCamera(Camera c, Planet p) {
        final Vector3D camPos = c.getCartesianPosition();
        return camPos.squaredDistanceTo(p.toCartesian(position));
    }

    @Override
    public float getScreenPositionX() {
        return 0;
    }

    @Override
    public float getScreenPositionY() {
        return 0;
    }


    @Override
    public Vector3D getPosition(Planet planet) {
        return planet.toCartesian(position);
    }
}
