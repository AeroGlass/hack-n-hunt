package aero.glass.utils;

import org.glob3.mobile.generated.Angle;
import org.glob3.mobile.generated.ElevationDataProvider;
import org.glob3.mobile.generated.G3MWidget;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.TaitBryanAngles;

import aero.glass.math.Versor;
import aero.glass.unit.AHLR;
import aero.glass.unit.AltitudeReference;
import aero.glass.unit.LengthUnit;

/**
 * Created by zolta on 2018. 03. 18..
 */

public class G3MHelper {
    private static final double MIN_CAMERA_ALT = 10.0;

    /**
     * Sets camera position and orientation from an OrientationManager.
     *
     * @param ahlr orientation source with magnetic heading
     * @param g3mWidget the g3mWidget to use for setting the camera
     * @param dem the elevation data that limits the camera position
     */
    public static synchronized void setCamera(
            final AHLR ahlr,
            final G3MWidget g3mWidget, final ElevationDataProvider dem) {
        setCameraPosition(ahlr, g3mWidget, dem);
        setCameraOrientation(ahlr, g3mWidget);
    }


    /**
     * Sets camera position from an OrientationManager
     *
     * sets position only, heading+pitch+roll are unaffected!
     *
     * @param ahlr  orientation source with magnetic heading
     * @param g3mWidget the g3mWidget to use for setting the camera
     * @param dem the elevation data that limits the camera position
     */
    public static synchronized void setCameraPosition(
            final AHLR ahlr,
            final G3MWidget g3mWidget, final ElevationDataProvider dem) {

        final Geodetic3D pos = Geodetic3D.fromDegrees(ahlr.getLocation().getLatitude(),
                ahlr.getLocation().getLongitude(),
                ahlr.getLocation().getAltitude(AltitudeReference.WGS84, LengthUnit.Meter));

        if (pos.isNan()) {
            return;
        }

        // System.err.println("Camera pos, lat: " + pos._latitude + ", lon: " + pos._longitude
        //                   + ", elev: " + pos._height);

        // Minimum altitude is 2 meters above ground level if elevation map is
        // turned on
        // FIXME : does not work - g3m inconsistent coordinate system error
        // until then a fixed minimum of 1000 m (asl) is used
        if (dem != null) {

            final Geodetic3D p = new Geodetic3D(pos._latitude, pos._longitude,
                    Math.max(pos._height, MIN_CAMERA_ALT));

            // Note: don't simply use setCameraPosition, as that is broken and
            // will also mess up camera orientation...
            g3mWidget.getNextCamera().setGeodeticPositionStablePitch(p);
        } else {

            // Note: don't simply use setCameraPosition, as that is broken and
            // will also mess up camera orientation...
            final Geodetic3D p = new Geodetic3D(pos._latitude, pos._longitude,
                    Math.max(pos._height, MIN_CAMERA_ALT));

            g3mWidget.getNextCamera().setGeodeticPositionStablePitch(p);
        }
    }

    /**
     * Sets camera orientation from an OrientationManager
     *
     * sets orientation only, camera position is unaffected!
     *
     * @param ahlr providing orientation
     * @param g3mWidget the G3M widget to use for setting the orienatiotion
     */
    public static synchronized void setCameraOrientation(final AHLR ahlr,
                                                         final G3MWidget g3mWidget) {

        Versor orientation = ahlr.getOrientation(AHLR.HeadingReference.TRUE);
        double headingTrue = orientation.yaw();
        double pitch = orientation.pitch();
        double roll = orientation.roll();

        if (Double.isNaN(headingTrue) || Double.isNaN(pitch) || Double.isNaN(roll)) {
            return;
        }

        // System.err.println("setCameraOrientation: heading: " + headingTrue
        //                  + ", pitch: " + pitch + ", " + roll);

        setCameraHeadingPitchRoll(g3mWidget, headingTrue, pitch, roll);
    }

    private static void setCameraHeadingPitchRoll(final G3MWidget g3mWidget,
                                                  double heading, double pitch, double roll) {
        // Note that heading, pitch and roll are inverted!

        if (Double.isNaN(heading) || Double.isNaN(pitch) || Double.isNaN(roll)) {
            return;
        }

        // System.err.println("setCameraHeadingPitchRoll: heading: " + heading
        //         + ", pitch: " + pitch + ", " + roll + ", var: " + variation);

        //minus is necessary because HUD use this coordinate system
        g3mWidget.setCameraHeadingPitchRoll(Angle.fromDegrees(heading),
                Angle.fromDegrees(-pitch),
                Angle.fromDegrees(roll));
    }

    public static synchronized void resetCameraPitchRoll(
            final G3MWidget g3mWidget) {
        TaitBryanAngles hpr = g3mWidget.getCurrentCamera()
                .getHeadingPitchRoll();

        g3mWidget.setCameraHeadingPitchRoll(hpr._heading,
                Angle.fromDegrees(-6.0), Angle.fromDegrees(0.0));
    }
}
