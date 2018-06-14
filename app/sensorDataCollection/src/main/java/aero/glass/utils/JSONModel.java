package aero.glass.utils;

/** A class representing an obstacle. */
public class JSONModel {

    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private double elevation = Double.NaN;

    private double scale = Double.NaN;
    private double rotationPitch = Double.NaN;
    private double rotationHeading = Double.NaN;

    private double rotationRateHeading = Double.NaN;

    private String ident = "";

    private String jsonFileName = "";

    private JSONFileReader fileReader;

    /** Interface for file reading. */
    public interface JSONFileReader {
        String read(String filename);
        String getPrefix();
    }

    public JSONModel(JSONFileReader r) {
        fileReader = r;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double lat) {
        this.latitude = lat;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double lon) {
        this.longitude = lon;
    }

    public double getAltitudeInMeters() {
        return elevation;
    }

    public void setAltitudeInFeet(double alt) {
        this.elevation = alt;
    }

    public double getScale() {
        return scale;
    }

    public void setScale(double scl) {
        scale = scl;
    }

    public double getRotationHeading() {
        return rotationHeading;
    }

    public void setRotationHeading(double rotHeading) {
        rotationHeading = rotHeading;
    }

    public double getRotationPitch() {
        return rotationPitch;
    }

    public void setRotationPitch(double rotPitch) {
        rotationPitch = rotPitch;
    }

    public double getRotationRateHeading() {
        return rotationRateHeading;
    }

    public void setRotationRateHeading(double newHeadingRate) {
        rotationRateHeading = newHeadingRate;
    }

    public String getJsonFileName() {
        return jsonFileName;
    }

    public void setJsonFileName(String js) {
        jsonFileName = js;
    }

    public String getJson() {
        return fileReader.read(jsonFileName);
    }

    public String getPrefix() {
        return fileReader.getPrefix();
    }

    public String getIdent() {
        return this.ident;
    }

    public void setIdent(String newIdent) {
        this.ident = newIdent;
    }

}
