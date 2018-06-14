package aero.glass.unit;

/**
 * Created by zolta on 2018. 01. 19..
 */
public enum AltitudeReference {
    WGS84,
    AMSL;
    // TODO: AGL

    public double calcFrom(final double lat, final double lng, final double alt,
                           final AltitudeReference altRef) {
        if (this == altRef) {
            return alt;
        }

        final double wgs84Alt;
        switch (altRef) {
            case AMSL:
                wgs84Alt = alt - Geoid.getInstance().getSeparation(lat, lng);
                break;
            case WGS84:
            default:
                wgs84Alt = alt;
                break;
        }

        switch (this) {
            case AMSL:
                return wgs84Alt + Geoid.getInstance().getSeparation(lat, lng);
            case WGS84:
            default:
                return wgs84Alt;
        }
    }

    public static AltitudeReference fromString(final String s) {
        if (WGS84.toString().equals(s)) {
            return WGS84;
        } else if (AMSL.toString().equals(s)) {
            return AMSL;
        } else {
            throw new IllegalArgumentException("Unknown altitude reference type : " + s);
        }
    }
}
