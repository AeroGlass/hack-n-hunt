package aero.glass.unit;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by premeczmatyas on 28/02/17.
 * Geoid model. Accuracy within 1m in at least 95% of cases.
 */
public class Geoid {
    private static boolean inited = false;
    protected static final short[] DATA = new short[181 * 360];
    protected static Geoid instance = null;


    protected Geoid() {
        // never call this explicitly, use init() and getInstance() instead
    }


    private float getRaw(int latIn, int lonIn) {
        int lat = latIn;
        int lon = lonIn;

        if (lat > 90) {
            lat = 180 - lat;
            lon += 180;
        } else if (lat < -90) {
            lat = -180 - lat;
            lon += 180;
        }
        lon = (lon + 360) % 360;
        return DATA[(90 - lat) * 360 + lon];
    }

    /**
     * Difference in meters between geoid and ellipsoid approximations of sea
     * level.
     *
     * @param lat
     *            latitude in degrees
     * @param lon
     *            longitude in degrees
     * @return difference in meters.
     */
    public double getSeparation(double lat, double lon) {
        double latFrac = lat - Math.floor(lat);
        double lonFrac = lon - Math.floor(lon);
        return 0.01 * ((getRaw((int) Math.floor(lat),
                (int) Math.floor(lon))
                * (1.0 - latFrac) + getRaw((int) Math.floor(lat) + 1,
                (int) Math.floor(lon)) * latFrac)
                * (1.0 - lonFrac) + (getRaw((int) Math.floor(lat),
                (int) Math.floor(lon) + 1)
                * (1.0 - latFrac) + getRaw((int) Math.floor(lat) + 1,
                (int) Math.floor(lon) + 1) * latFrac)
                * lonFrac);
    }

    public static Geoid getInstance() {
        if (instance == null) {
            throw new RuntimeException("DroidGeoid was not initialized before its first use!");
        }
        return instance;
    }

    public static void init(Context c) {
        if (!inited) {
            final byte[] twoBytes = new byte[2];
            try {
                InputStream is = c.getAssets().open("geoid.dat");
                for (int i = 0; i < DATA.length; i++) {
                    is.read(twoBytes);
                    DATA[i] = (short) ((twoBytes[0] << 8) | (twoBytes[1] & 0xFF));
                }
                is.close();
            } catch (IOException ex) {
                // never happens
                DATA[DATA.length - 1] = 0;
            }
            inited = true;
        }
        if (instance == null) {
            instance = new Geoid();
        }
    }
}
