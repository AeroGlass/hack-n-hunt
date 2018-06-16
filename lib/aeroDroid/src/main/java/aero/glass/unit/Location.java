package aero.glass.unit;


import static aero.glass.unit.AltitudeReference.WGS84;
import static aero.glass.unit.LengthUnit.Meter;

/**
 * A geospatial location.
 */
public class Location {

    private double latitude;
    private double longitude;
    private double altitude;

    public Location(final double lat, final double lng, final double alt,
                    final AltitudeReference coordinateSystem, final LengthUnit altitudeUnit) {
        set(lat, lng, alt, coordinateSystem, altitudeUnit);
    }

    public Location(Location location) {
        set(location);
    }

    public final void set(final double lat, final double lng, final double alt,
                          final AltitudeReference altRef, final LengthUnit altitudeUnit) {
        latitude = lat;
        longitude = lng;
        altitude = WGS84.calcFrom(lat, lng, Meter.calcFrom(alt, altitudeUnit), altRef);
    }

    public final void set(final Location location) {
        latitude = location.latitude;
        longitude = location.longitude;
        altitude = location.altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude(final AltitudeReference reqAltRef, final LengthUnit reqAltUnit) {
        return reqAltUnit.calcFrom(reqAltRef.calcFrom(latitude, longitude, altitude, WGS84), Meter);
    }

    public static double degreeMinuteSecondToDecimalDegree(final double degree,
                                                           final double minute,
                                                           final double second) {
        return degree + (minute + second / 60) / 60;
    }

    public static double[] decimalDegreeToDegreeMinuteSecond(final double decimalDegree) {
        final double[] result = new double[3];
        result[0] = Math.floor(decimalDegree);
        final double residualM60 = (decimalDegree - result[0]) * 60;
        result[1] = Math.floor(residualM60);
        result[2] = (residualM60 - result[1]) * 60;
        return result;
    }

    @Override
    public String toString() {
        return "Location{lat: " + latitude + ", long: " + longitude + ", WGS84 alt: " + altitude
                + "}";
    }
}
