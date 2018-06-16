package aero.glass.unit;

import aero.glass.math.Versor;

import static aero.glass.unit.AltitudeReference.WGS84;
import static aero.glass.unit.LengthUnit.Meter;

/**
 * A plain java object holding an attitude heading and location info.
 * Created by zolta on 2018. 01. 22..
 */
public class AHLR {

    /**
     * Enumeration for heading references.
     */
    public enum HeadingReference {
        TRUE,
        MAGNETIC
    }

    private Location location = new Location(0.0, 0.0, 0.0, WGS84, Meter);
    private Versor orientation = new Versor();
    private float variation;

    public AHLR(final Location loc, final Versor ori, final HeadingReference headRef,
                final float var) {
        set(loc, ori, headRef, var);
    }

    public AHLR(final Location loc, final Versor ori, final HeadingReference headRef,
                final long utcTime) {
        set(loc, ori, headRef, utcTime);
    }

    public AHLR(final AHLR ahlr) {
        set(ahlr);
    }

    public final void set(final Location loc, final Versor ori, final HeadingReference headRef,
                          final float var) {
        location.set(loc);
        if (headRef == HeadingReference.TRUE) {
            orientation.set(ori);
        } else {
            orientation.set(new Versor(ori.roll(), ori.pitch(), ori.yaw() - var));
        }
        variation = var;
    }

    public final void set(final Location loc, final Versor ori, final HeadingReference headRef,
                          long utcTime) {
        set(loc, ori, headRef, getVariation(loc, utcTime));
    }

    public final void set(final AHLR ahlr) {
        location.set(ahlr.location);
        orientation.set(ahlr.orientation);
        variation = ahlr.variation;
    }

    public Versor getOrientation(final HeadingReference headRef) {
        if (headRef == HeadingReference.TRUE) {
            return new Versor(orientation);
        } else {
            return new Versor(orientation.roll(), orientation.pitch(),
                    orientation.yaw() + variation);
        }
    }

    public Location getLocation() {
        return new Location(location);
    }

    public float getVariation() {
        return variation;
    }

    private float getVariation(final Location loc, final long utcTime) {
        return new GeomagneticField((float) loc.getLatitude(), (float) loc.getLongitude(),
                (float) loc.getAltitude(WGS84, Meter), utcTime).getDeclination();
    }

    @Override
    public String toString() {
        return "AHLR{" + location + ", " + orientation + ", variation: " + variation + "}";
    }
}
