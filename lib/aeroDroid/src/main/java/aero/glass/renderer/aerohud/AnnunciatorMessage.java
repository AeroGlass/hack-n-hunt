package aero.glass.renderer.aerohud;

/**
 * Message to be displayed on the annunciator.
 * Created by DrakkLord on 2017. 01. 06..
 */
public class AnnunciatorMessage {
    private final String text;
    private final HUDMessageRow.HColor color;
    private final long timeoutAmount;
    private final HUDMessageRow r;

    private boolean timeoutSet;
    private long timeout;

    public AnnunciatorMessage(String textIn, HUDMessageRow.HColor colorIn, long timeoutIn) {
        text = textIn;
        color = colorIn;
        timeoutAmount = timeoutIn;
        r = new HUDMessageRow(color, text);
    }

    public String getText() {
        return text;
    }

    public HUDMessageRow.HColor getColor() {
        return color;
    }

    public boolean isTimeoutEnabled() {
        return timeoutAmount != 0;
    }

    public long getTimeout() {
        if (!timeoutSet) {
            timeoutSet = true;
            timeout = System.currentTimeMillis() + timeoutAmount;
        }
        return timeout;
    }

    public HUDMessageRow getRow() {
        return r;
    }
}
