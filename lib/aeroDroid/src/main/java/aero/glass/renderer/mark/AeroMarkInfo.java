package aero.glass.renderer.mark;

import org.glob3.mobile.generated.Color;

import aero.glass.renderer.textrender.RenderableText;

/**
 * Created by premeczmatyas on 12/01/16.
 * Information about the mark.
 */
public final class AeroMarkInfo {

    public static final int PRIORITY_TRAFFIC = 10;
    public static final int PRIORITY_WAYPOINT = 20;
    public static final int PRIORITY_AIRPORT = 30;
    public static final int PRIORITY_NAVAID = 40;
    public static final int PRIORITY_CITY = 50;
    public static final int PRIORITY_TOWN = 60;
    public static final int PRIORITY_VILLAGE = 70;
    public static final int PRIORITY_AIRWAY = 80;
    public static final int PRIORITY_OBJECT = 90;
    public AeroMarkType type = AeroMarkType.GENERAL_MARK;

    /**
     * The text the mark displays. Useless if the mark does not have label.
     */
    private String label;
    private int[] characters;

    public int priority = PRIORITY_TRAFFIC;
    /**
     * The minimum distance (in meters) to show the mark. If the camera is
     * further than this, the mark will not be displayed. Default value: 4.5e+06
     */
    public double minDistanceToCamera = 4.5e+06;

    /**
     * The font size of the text. Useless if the mark does not have label.
     * Default value: .8f
     */
    public float labelFontSize = .5f;

    /**
     * Distance of mark to camera in meters. This value is used for the ordering of the marks
     * before rendering. Defaults to farthest possible value.
     */
    public double squaredDistanceToCamera = Double.POSITIVE_INFINITY;


    public Color labelFontColor = Color.white();


    public AeroMarkInfo() {
        // Empty on purpose
    }

    public void setLabel(String t) {
        label = t;
        characters = RenderableText.getCharCodes(t);
    }

    @SuppressWarnings("PMD") // this array is exposed to reduce copy overhead
    public int[] getCharacters() {
        return characters;
    }

    /** Copy constructor.
     *  @param o the object which to copy from
     * */
    public AeroMarkInfo(AeroMarkInfo o) {
        label = o.label;
        characters = o.characters;
        minDistanceToCamera = o.minDistanceToCamera;
        labelFontSize = o.labelFontSize;
        labelFontColor = o.labelFontColor;
        squaredDistanceToCamera = o.squaredDistanceToCamera;
        priority = o.priority;
        type = o.type;
    }

    public String getLabel() {
        return label;
    }
}
