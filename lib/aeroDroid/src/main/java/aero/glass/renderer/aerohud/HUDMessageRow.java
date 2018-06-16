package aero.glass.renderer.aerohud;

/**
 * A message row used by the HUD's annunciator panel.
 *
 * @author DrakkLord
 */
public class HUDMessageRow {
    public final HColor color;
    protected final String text;

    /** Basic HUD color enumerator. */
    public enum HColor {
        CYAN,
        MAGENTA,
        RED,
        GREEN;
    };

    public HUDMessageRow(HColor color, String textParam) {
        this.color = color;
        text = textParam;
    }

    public String getMessage() {
        return text;
    }

    public HColor getColor() {
        return color;
    }
}
