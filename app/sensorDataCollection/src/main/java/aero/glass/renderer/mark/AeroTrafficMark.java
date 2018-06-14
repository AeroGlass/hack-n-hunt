package aero.glass.renderer.mark;

import org.glob3.mobile.generated.AltitudeMode;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.Planet;

import aero.glass.renderer.linerender.IPattern;
import aero.glass.renderer.linerender.RenderablePattern;
import aero.glass.renderer.linerender.RenderableTriStrip;
import aero.glass.renderer.textrender.EFontTypes;
import aero.glass.renderer.textrender.RenderableText2D;
import aero.glass.renderer.textrender.TextRenderer;

/**
 * Created by premeczmatyas on 14/01/16.
 */
public class AeroTrafficMark extends AeroMark {

    private double altDiff = 0.0;
    private float labelOffset;
    private boolean goingLeft;
    private boolean threat = false;

    public AeroTrafficMark(Geodetic3D positionIn, AltitudeMode altitudeModeIn, AeroMarkInfo li) {
        super(positionIn, altitudeModeIn, li);
        markInfo.type = AeroMarkType.TRAFFIC_MARK;
    }


    @Override
    protected void calcSize(int screenWidth) {
        widthToLeft = widthToRight = TextRenderer.getWidth(markInfo.getCharacters(),
                markInfo.labelFontSize,
                EFontTypes.TRAFFIC_FONT_ID, screenWidth) / 2;
        heightToTop = (int) labelOffset;
        heightToBottom = (int) (labelOffset + TextRenderer.getHeight(markInfo.labelFontSize,
                EFontTypes.TRAFFIC_FONT_ID, screenWidth));
    }


    public void update(String label, double altDif, double sqDist, boolean goingLef) {
        setLabel(label);
        setSquaredDistanceToCamera(sqDist);
        altDiff = altDif;
        goingLeft = goingLef;
    }

    private float clampTo(double number, double min, double max) {
        return (float) Math.min(Math.max(min, number), max);
    }

    // for testing purposes altitude differences are multiplied by 100!
    // the reason behind this is that you cant get close enough to the
    // object from the map view.
    /**
     * This function is used to tell what level of threat the target
     * aircraft creates.
     *
     * @param dist
     *            distance in NM between the current observer and the target
     *            aircraft
     * @param altDiff
     *            altitude difference between the current observer and the
     *            target aircraft
     * @return the level of threat the object
     */
    public static ETrafficThreatLevel trafficToThreatLevel(double dist, double altDiff) {

        if (dist < 4 && altDiff < 100000) {
            return ETrafficThreatLevel.TWL_THREAT;
        }
        if (dist < 6 && altDiff < 120000) {
            return ETrafficThreatLevel.TWL_INTRUDER;
        }
        if (dist < 20 && altDiff < 220000) {
            return ETrafficThreatLevel.TWL_PROXIMATE;
        }
        return ETrafficThreatLevel.TWL_OTHER;
    }


    @Override
    public float getLabelScreenPositionX() {
        return markPixel._x;
    }

    @Override
    public float getLabelScreenPositionY() {
        return markPixel._y + labelOffset * 1.1f + 20;
    }


    public void updateIcon(Planet p,
                           RenderablePattern dirPattern,
                           RenderablePattern threatPattern,
                           RenderableTriStrip altitudePattern,
                           int screenWidth, int screenHeight,
                           double camRoll
                           ) {



        // early out if not visible
        if (markPixel._x < -labelOffset
                || markPixel._y < -labelOffset
                || markPixel._x > screenWidth + labelOffset
                || markPixel._y > screenHeight + labelOffset) {
            return;
        }


        double distance = Math.sqrt(getSquaredDistanceToCamera()) / 1852.0;
        labelOffset = clampTo((120 - distance) / 80, 0.0, 1.0)
                * TextRenderer.getDependableSize(20.0f, screenHeight);

        threat = trafficToThreatLevel(Math.abs(distance), Math.abs(altDiff))
                == ETrafficThreatLevel.TWL_THREAT;

        RenderablePattern target = threat ? threatPattern : dirPattern;
        if (markPixel != null) {

            float[] squareLine = {
                    0f, -labelOffset,
                    labelOffset, 0f,
                    0f, labelOffset,
                    -labelOffset, 0f,
                    0f, -labelOffset
            };
           // IPattern.rotate(-c.getRoll()._radians, squareLine);
            IPattern.translate(markPixel, squareLine);
            target.addSegment(squareLine, labelOffset / 20f);

            if (threat) {
                float danger = labelOffset + ((float) lifeTime % 1f)
                    * TextRenderer.getDependableSize(15.0f, screenHeight);

                float[] squareDanger = {
                        0f, -danger,
                        danger, 0f,
                        0f, danger,
                        -danger, 0f,
                        0f, -danger
                };
             //   IPattern.rotate(-c.getRoll()._radians, squareDanger);

                IPattern.translate(markPixel, squareDanger);
                target.addSegment(squareDanger, .6f);
            } else {
                float[] arrow = {
                        labelOffset * .75f, -labelOffset / 2f,
                        labelOffset * 1.25f, 0f,
                        labelOffset * .75f, labelOffset / 2f,
                };
               // IPattern.rotate(-c.getRoll()._radians, arrow);
                IPattern.translate(
                        markPixel._x + labelOffset * 0.15f
                                + (float) Math.cos(lifeTime * 2) * labelOffset * .25f,
                        markPixel._y,
                        arrow);



                if (goingLeft) {
                    IPattern.rotate(Math.PI - camRoll, markPixel, arrow);
                } else {
                    IPattern.rotate(-camRoll, markPixel, arrow);
                }
                target.addSegment(arrow, labelOffset / 28f);
            }


            if (altDiff < -1000) {
                // generate altitude difference marker
                float[] altitude = {
                        labelOffset * .8f, 0f,
                        -labelOffset * .8f, 0f,
                        0f, labelOffset * .8f,
                        -labelOffset * .8f, 0f,
                };
              //  IPattern.rotate(-c.getRoll()._radians, altitude);
                IPattern.translate(markPixel, altitude);
                altitudePattern.addStrip(altitude);

            } else if (altDiff > 1000) {
                // generate altitude difference marker
                float[] altitude = {
                        labelOffset * .8f, 0f,
                        0f, -labelOffset * .8f,
                        -labelOffset * .8f, 0f,
                        0f, -labelOffset * .8f,
                };
              //  IPattern.rotate(-c.getRoll()._radians, altitude);
                IPattern.translate(markPixel, altitude);
                altitudePattern.addStrip(altitude);

            } else {
                // generate altitude difference marker
                float[] altitude = {
                        -labelOffset * .7f, -labelOffset * .1f,
                        -labelOffset * .7f, labelOffset * .1f,
                        labelOffset * .7f, -labelOffset * .1f,
                        labelOffset * .7f, labelOffset * .1f,
                };
               // IPattern.rotate(-c.getRoll()._radians, altitude);
                IPattern.translate(markPixel, altitude);
                altitudePattern.addStrip(altitude);
            }
        }
    }

    @Override
    public RenderableText2D getRenderableText() {
        if (renderableText == null) {
            renderableText = new RenderableText2D(
                    markInfo.getCharacters(),
                    threat ? EFontTypes.THREAT_FONT_ID : EFontTypes.TRAFFIC_FONT_ID,
                    getLabelScreenPositionX(),
                    getLabelScreenPositionY(),
                    getFontSize(),
                    .5f, .5f
            );
        } else {
            renderableText.setPosition(getLabelScreenPositionX(), getLabelScreenPositionY());
            renderableText.setCharacters(markInfo.getCharacters());
        }

        return renderableText;
    }

}
