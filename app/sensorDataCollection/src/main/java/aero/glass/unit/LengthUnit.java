package aero.glass.unit;

/**
 * Created by zolta on 2018. 01. 19..
 */

public enum LengthUnit {
    Meter,
    Feet,
    Mile,
    Inch,
    KM,
    NM;

    public static final double FEET_IN_METER = 0.3048;
    public static final double MILE_IN_METER  = 1609.344;
    public static final double NM_IN_METER  = 1852;
    public static final double INCH_IN_METER  = 0.0254;

    public double calcFrom(final double value, final LengthUnit unit) {
        if (this == unit) {
            return value;
        }

        final double valueInMeter;
        switch (unit) {
            case Feet:
                valueInMeter = value * FEET_IN_METER;
                break;
            case Mile:
                valueInMeter = value * MILE_IN_METER;
                break;
            case Inch:
                valueInMeter = value * INCH_IN_METER;
                break;
            case KM:
                valueInMeter = value * 1000.0;
                break;
            case NM:
                valueInMeter = value * NM_IN_METER;
                break;
            case Meter:
            default:
                valueInMeter = value;
                break;
        }

        switch (this) {
            case Feet:
                return valueInMeter / FEET_IN_METER;
            case Mile:
                return valueInMeter / MILE_IN_METER;
            case Inch:
                return valueInMeter / INCH_IN_METER;
            case KM:
                return valueInMeter * 0.001;
            case NM:
                return valueInMeter / NM_IN_METER;
            case Meter:
            default:
                return valueInMeter;
        }
    }

    public String symbol() {
        switch (this) {
            case Feet:
                return "ft";
            case Mile:
                return "mile";
            case Inch:
                return "\"";
            case KM:
                return "km";
            case NM:
                return "NM";
            case Meter:
                return "m";
            default:
                return "";
        }
    }
}
