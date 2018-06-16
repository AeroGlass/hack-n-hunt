package aero.glass.renderer.mark;

import org.glob3.mobile.generated.AltitudeMode;
import org.glob3.mobile.generated.Camera;
import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.ILogger;
import org.glob3.mobile.generated.Matrix44D;
import org.glob3.mobile.generated.Planet;
import org.glob3.mobile.generated.Vector2F;
import org.glob3.mobile.generated.Vector3D;

import aero.glass.renderer.textrender.EFontTypes;
import aero.glass.renderer.textrender.RenderableText2D;
import aero.glass.renderer.textrender.TextRenderer;

/**
 * Created by premeczmatyas on 12/01/16.
 */
public class AeroMark implements Comparable<AeroMark> {

    private String name;
    protected Geodetic3D position;
    private final AltitudeMode altitudeMode;
    protected final AeroMarkInfo markInfo;
    private Vector3D cartesianPosition;
    private Geodetic3D positionWithSurfaceElevation = null;
    private Vector3D normalAtMarkPosition;
    private double currentSurfaceElevation = 0.0;
    private boolean enabled;

    // width from center position (markPixel) to left border
    protected int widthToLeft = -1;
    // width from center position (markPixel) to right border
    protected int widthToRight;
    // height from center position (markPixel) to top border
    protected int heightToTop;
    // height from center position (markPixel) to bottom border
    protected int heightToBottom;

    private long startTime = System.currentTimeMillis();
    protected double lifeTime = 0.0;

    // screen-space pivot point and occupied rectangle
    protected Vector2F markPixel = new Vector2F(0,0);
    protected boolean behindCamera = false;
    protected Rectangle realEstate = new Rectangle(0, 0, 0, 0);
    protected RenderableText2D renderableText = null;


    // constructor
    public AeroMark(Geodetic3D positionIn, AltitudeMode altitudeModeIn, AeroMarkInfo li) {
        // TODO : this is temporarily fix to show elements that are on the surface,
        //  this would need an elevation provider which is not available now.
        if (Double.isNaN(positionIn._height)) {
            position = Geodetic3D.fromDegrees(positionIn._latitude._degrees,
                    positionIn._longitude._degrees,
                    0.0);
        } else {
            position = positionIn;
        }

        altitudeMode = altitudeModeIn;
        markInfo = new AeroMarkInfo(li);

        // internal use
        cartesianPosition = null;
        normalAtMarkPosition = null;
        enabled = true;
    }



     public void updateDistance(Vector3D camPos, Planet p) {
        Vector3D mPos = getCartesianPosition(p);
        Vector3D sub = mPos.sub(camPos);
        setSquaredDistanceToCamera(sub.dot(sub));
    }


    public void setColor(Color color) {
        markInfo.labelFontColor = color;
    }


    public void updateScreenPos(boolean stereo, int screenWidth, Camera c, Planet p, long time) {

        lifeTime = (time - startTime) * 0.001;

        // numeric representations in global system of coordinates
        final Vector3D cartesianMarkPosition = getCartesianPosition(p);
        final Matrix44D m = c.getModelViewMatrix44D();

        // check if mark is behind us
        final double z = m._m30 * cartesianMarkPosition._x + m._m31
                * cartesianMarkPosition._y + m._m32 * cartesianMarkPosition._z
                + m._m33;

        behindCamera = z <= 0;

        markPixel = c.point2Pixel(getCartesianPosition(p));
        if (stereo) {
            markPixel = new Vector2F(markPixel._x / 2.0f, markPixel._y);
        }

        if (this instanceof AeroTrafficMark
                || widthToLeft == -1) {
            calcSize(screenWidth);
        }
    }



    public void updateRealEstate(Camera c, Planet p) {

        if (behindCamera) {
            realEstate.set(0, 0, 0, 0);
            return;
        }

        final double left = markPixel._x - widthToLeft;
        if (left > c.getViewPortWidth()) {
            realEstate.set(0, 0, 0, 0);
            return;
        }
        final double top = markPixel._y - heightToTop;
        if (top > c.getViewPortHeight()) {
            realEstate.set(0, 0, 0, 0);
            return;
        }
        final double right = markPixel._x + widthToRight;
        if (right < 0) {
            realEstate.set(0, 0, 0, 0);
            return;
        }
        final double bottom = markPixel._y + heightToBottom;
        if (bottom < 0) {
            realEstate.set(0, 0, 0, 0);
            return;
        }

        realEstate.set(left, top, right, bottom);
    }


    @Override
    public int compareTo(AeroMark o) {
        return markInfo.priority == o.markInfo.priority ? Double
                .compare(getSquaredDistanceToCamera(),
                        o.getSquaredDistanceToCamera())
                : markInfo.priority - o.markInfo.priority;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AeroMark) {
            return markInfo.priority == ((AeroMark) o).markInfo.priority
                    && getSquaredDistanceToCamera() == ((AeroMark) o).getSquaredDistanceToCamera()
                    && markInfo.getLabel() == ((AeroMark) o).markInfo.getLabel()
                    && markInfo.type == ((AeroMark) o).markInfo.type
                    && position.equals(((AeroMark) o).position);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return markInfo.getLabel().hashCode() ^ markInfo.type.hashCode();
    }

    /**
     * Occupied real estate in the field of view.
     *
     * @return occupied rectangle or <code>null</code>, if invisible
     */
    public Rectangle getRealEstate() {
        return realEstate;
    }

    protected Vector3D getNormalAtMarkPosition(Planet planet) {
        if (normalAtMarkPosition == null) {
            normalAtMarkPosition = new Vector3D(
                    planet.geodeticSurfaceNormal(
                            getCartesianPosition(planet)));
        }
        return normalAtMarkPosition;
    }


    public int getPriority() {
        return markInfo.priority;
    }


    public void setPriority(int prio) {
        markInfo.priority = prio;
    }

    public void setPosition(Geodetic3D positionn) {
        if (altitudeMode == AltitudeMode.RELATIVE_TO_GROUND) {
            ILogger.instance()
                .logWarning(
                        "Position change with _altitudeMode == RELATIVE_TO_GROUND not supported");
        }
        if (!positionn.isEquals(position)) {
            if (position != null) {
                position.dispose();
            }
            position = positionn;
            cartesianPosition = null;
            positionWithSurfaceElevation = null;
        }
    }

    protected void calcSize(int screenWidth) {
        widthToLeft = widthToRight = TextRenderer.getWidth(
                markInfo.getCharacters(),
                markInfo.labelFontSize,
                EFontTypes.LABEL_FONT_ID,
                screenWidth) / 2;
        heightToTop = heightToBottom = TextRenderer.getHeight(
                markInfo.labelFontSize,
                EFontTypes.LABEL_FONT_ID,
                screenWidth) / 2;
    }

    public final Vector3D getCartesianPosition(Planet planet) {
        if (cartesianPosition == null) {

            double altitude = position._height;
            if (altitudeMode == AltitudeMode.RELATIVE_TO_GROUND) {
                altitude += currentSurfaceElevation;
            }

            if (positionWithSurfaceElevation == null) {
                positionWithSurfaceElevation = new Geodetic3D(
                        position._latitude, position._longitude, altitude);
            }

            cartesianPosition = planet.toCartesian(positionWithSurfaceElevation);
        }
        return cartesianPosition;
    }


    public final boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enab) {
        enabled = enab;
    }


    public Geodetic3D getPosition() {
        return position;
    }

    public float getLabelScreenPositionX() {
        return markPixel._x;
    }
    public float getLabelScreenPositionY() {
        return markPixel._y;
    }

    public final String getLabel() {
        return markInfo.getLabel();
    }

    public final double getSquaredDistanceToCamera() {
        return markInfo.squaredDistanceToCamera;
    }

    public void setSquaredDistanceToCamera(double sqDist) {
        markInfo.squaredDistanceToCamera = sqDist;
    }


    public float getFontSize() {
        return markInfo.labelFontSize;
    }

    public void setLabel(String label) {
        markInfo.setLabel(label);
    }

    public AeroMarkType getType() {
        return markInfo.type;
    }

    public void invalidateRenderableText(){
        renderableText = null;
    }

    public RenderableText2D getRenderableText() {

        if (renderableText == null) {
            renderableText = new RenderableText2D(
                    markInfo.getCharacters(),
                    EFontTypes.LABEL_FONT_ID,
                    getLabelScreenPositionX(),
                    getLabelScreenPositionY(),
                    getFontSize(),
                    .5f, .5f
            );
        } else {
            renderableText.setPosition(getLabelScreenPositionX(), getLabelScreenPositionY());
        }

        return renderableText;
    }

    public void dispose() {
    }

    public boolean touched() {
        return false;
    }

    public boolean isBehindCamera() {
        return behindCamera;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
