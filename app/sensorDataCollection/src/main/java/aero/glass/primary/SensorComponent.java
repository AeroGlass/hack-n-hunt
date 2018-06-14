package aero.glass.primary;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.LinkedList;
import java.util.List;

import aero.glass.math.Vec3D;
import aero.glass.math.Versor;
import aero.glass.unit.AHLR;
import aero.glass.unit.Location;

import static aero.glass.primary.SDCActivity.DEMO_MODE;
import static aero.glass.unit.AltitudeReference.WGS84;
import static aero.glass.unit.LengthUnit.Meter;
import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by zolta on 2018. 03. 13..
 */

public class SensorComponent implements SensorEventListener, LocationListener{
    private static final double ALTITUDE = 101.8;
    private static final int MAX_MEANING_WINDOWS_SIZE = 10;
    private static List<Location> DEMO_POS;

    private SensorManager mSensorManager;

    private Sensor rotvecSensor;
    private final G3MComponent g3MComponent;
    private final Versor coordComp;
    private float yawOffset = -90.f;
    private Versor yawComp;

    private SDCActivity activity;
    LinkedList<android.location.Location> gpsMeasurments = new LinkedList<android.location.Location>();

    private volatile Location location = null;

    public SensorComponent(SDCActivity a, G3MComponent g3m) {
        activity = a;
        mSensorManager = (SensorManager) activity.getSystemService(SENSOR_SERVICE);
        rotvecSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        g3MComponent = g3m;
        if (!DEMO_MODE) {
            LocationManager locationManager =
                    (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
        yawComp = new Versor(0, 0, yawOffset);
        if (getDeviceDefaultOrientation(activity) == Configuration.ORIENTATION_PORTRAIT) {
            Log.d("def ori", "Configuration.ORIENTATION_PORTRAIT");
            coordComp = new Versor();
            coordComp.setYAxisRot((float) Math.toRadians(90));
        } else {
            Log.d("def ori", "Configuration.ORIENTATION_LANDSCAPE");
            coordComp = new Versor();
            coordComp.setYAxisRot((float) Math.toRadians(90));
        }
    }

    public static void setDemoPos(List<Location> demoPos) {
        DEMO_POS = demoPos;
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] quaternion = new float[4];
            SensorManager.getQuaternionFromVector(quaternion, event.values);

            Versor ori = new Versor(quaternion[0],
                    new Vec3D(quaternion[1], quaternion[2], quaternion[3]));
            ori.mul(coordComp);

            Versor orientation = new Versor(yawComp);
            orientation.mul(ori);

            if (location != null) {
                g3MComponent.setAHLR(new AHLR(location, orientation, AHLR.HeadingReference.MAGNETIC,
                        System.currentTimeMillis()));
            } else if (DEMO_MODE && DEMO_POS != null && DEMO_POS.size() > 0) {
                final int index1 = (int) (System.currentTimeMillis() / 8000L) % DEMO_POS.size();
                final int index2 = (index1 != 0) ? index1 - 1 : DEMO_POS.size() - 1;
                double fact = (double) (System.currentTimeMillis() % 8000L) / 8000.0;
                double lat = DEMO_POS.get(index1).getLatitude() * fact
                        + DEMO_POS.get(index2).getLatitude() * (1.0f - fact);
                double lon = DEMO_POS.get(index1).getLongitude() * fact
                        + DEMO_POS.get(index2).getLongitude() * (1.0f - fact);
                g3MComponent.setAHLR(new AHLR(new Location(lat, lon, ALTITUDE, WGS84, Meter),
                        orientation, AHLR.HeadingReference.MAGNETIC, System.currentTimeMillis()));
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onResume() {
        mSensorManager.registerListener(this, rotvecSensor, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onLocationChanged(android.location.Location loc) {
        if (activity.activityStateComponent.isUrbanMode()) {
            gpsMeasurments.addLast(loc);
            if (gpsMeasurments.size() > MAX_MEANING_WINDOWS_SIZE) {
                gpsMeasurments.removeFirst();
            }
            double latMean = 0.0;
            double lonMean = 0.0;
            for (android.location.Location location : gpsMeasurments) {
                latMean += location.getLatitude();
                lonMean += location.getLongitude();
            }
            // TODO: replace with IMU GPS kalman filter
            latMean /= gpsMeasurments.size();
            lonMean /= gpsMeasurments.size();
            location = new Location(latMean, lonMean, ALTITUDE, WGS84, Meter);
        } else {
            location = new Location(loc.getLatitude(), loc.getLongitude(), ALTITUDE, WGS84, Meter);
            gpsMeasurments.clear();
        }
        Log.d("onLocationChanged", location.toString());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * Return the device natural orientation.
     */
    private int getDeviceDefaultOrientation(Context ctx) {

        WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

        Configuration config = ctx.getResources().getConfiguration();

        int rotation = windowManager.getDefaultDisplay().getRotation();

        boolean rot0x180Landscape =
                (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
                        && config.orientation == Configuration.ORIENTATION_LANDSCAPE;
        boolean rot90x270Portrait =
                (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270)
                        && config.orientation == Configuration.ORIENTATION_PORTRAIT;

        if (rot0x180Landscape || rot90x270Portrait) {
            return Configuration.ORIENTATION_LANDSCAPE;
        } else {
            return Configuration.ORIENTATION_PORTRAIT;
        }
    }

    synchronized void cage(float angle) {
        yawComp.setFromEuler(0.f, 0.f, (float) Math.toRadians(yawOffset + angle));
    }

    synchronized public void stopCage() {
        yawOffset = yawComp.yaw();
    }

    synchronized public void resetCage() {
        yawOffset = -90.f;
        yawComp = new Versor(0, 0, yawOffset);
    }
}
