package aero.glass.renderer.aerohud;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This is the model which stores, and represents what needs to be rendered on the annunciator.
 * Created by krisoft on 14/01/16.
 */
public class Annunciator {

    /** Constant which shows how long the sensor mode should be displayed, in miliseconds. */
    public static final long SENSOR_MODE_DISPLAY_MILISECONDS = 4000L;

    /** Default timeout for display messages. */
    public static final long DEFAULT_MESSAGE_DISPLAY_MS = 5000L;


    private boolean noLocationWarning = false;
    private boolean noOrientationWarning = false;
    private boolean doubleImageWaring = false;
    /**
     * If showBlackout is true it means the display (everything except Annunciator messages)
     * should be hidden when either the noLocation or the noOrientation warnings are active.
     */
    private boolean showBlackout = false;

    private boolean timestampVisible = false;

    /**
     * Rows pre-created, to save on memory alocations and per-frame garbage-collection.
     */
    private HUDMessageRow noLocationRow = new HUDMessageRow(
            HUDMessageRow.HColor.RED,
            "No Location"
    );
    private HUDMessageRow noOrientationRow = new HUDMessageRow(
            HUDMessageRow.HColor.RED,
            "No Orientation"
    );
    private HUDMessageRow doubleImageRow = new HUDMessageRow(
            HUDMessageRow.HColor.RED,
            "DOUBLE IMAGE (reconnect magnetic cable)"
    );

    /**
     * Timestamp of the moment when the sensor mode message should expire.
     */
    private Date sensorDisplayExpiration = null;
    /**
     * The sensor mode message. null if there were no message yet, or it has expired.
     */
    private HUDMessageRow sensorRow = null;

    private HUDMessageRow timeStampRow = null;

    /**
     * Source where we can query general debug messages from.
     */
    private DebugRowSource debugRowSource;
    /**
     * If showDebug is true it means we should query debugRowSource, and display those messages.
     */
    private boolean showDebug = false;

    private HashMap<Integer, AnnunciatorMessage> messages
                                                    = new HashMap<Integer, AnnunciatorMessage>();

    public void addMessage(int groupID, String text, HUDMessageRow.HColor color, long timeoutMs) {
        messages.put(groupID,
                     new AnnunciatorMessage(text, color, timeoutMs));
    }

    /**
     * Setter for no location warning.
     * @param value true means the app has no location data
     */
    public void setNoLocationWarning(boolean value) {
        noLocationWarning = value;
    }

    /**
     * Setter for no orientation warning.
     * @param value true means the app has no orientation data
     */
    public void setNoOrientatioWarning(boolean value) {
        noOrientationWarning = value;
    }

    /**
     * Setter for blackout preference.
     * @param value true enables blackout (but it only blacks out when either no location
     *              or no orientation warings are active)
     */
    public void setShowBlackout(boolean value) {
        showBlackout = value;

    }

    /**
     * Setter for double image warning.
     * @param value true means the double image detection happened and not reseted
     */
    public void setDoubleImageWarning(boolean value) {
        doubleImageWaring = value;
    }

    /**
     * Setter for debug row source.
     * @param s the source
     */
    public void setDebugRowSource(DebugRowSource s) {
        debugRowSource = s;
    }

    /**
     * Setter for debug preference.
     * @param value if true then it renders debug messages from debug row source
     */
    public void setShowDebug(boolean value) {
        showDebug = value;

    }

    /**
     * Way to notify the Annunciator about sensor mode change.
     * @param newName name of the sensor mode
     */
    public void setSensorMode(String newName) {
        sensorRow = new HUDMessageRow(
                HUDMessageRow.HColor.GREEN,
                String.format("Sensor mode: %s", newName)
        );
        sensorDisplayExpiration = new Date(
                System.currentTimeMillis() + SENSOR_MODE_DISPLAY_MILISECONDS
        );
    }

    public void setTimeStamp(long timeStamp, boolean show) {
        timeStampRow = new HUDMessageRow(HUDMessageRow.HColor.GREEN,
                "TimeStamp: " + Long.toString(timeStamp));
        timestampVisible = show;
    }

    // methods below used for display

    /**
     * Called by BlackoutHUDElement. This is where the blackout logic should be implemented.
     * @return true when the blackout box should be drawn, false otherwise
     */
    public boolean shouldBlackout() {
        return (noLocationWarning || noOrientationWarning) && showBlackout;
    }

    private void removeExpiredMessages() {
        final long nowt = System.currentTimeMillis();
        for (Iterator<Map.Entry<Integer, AnnunciatorMessage>> it
             = messages.entrySet().iterator(); it.hasNext();) {

            final Map.Entry<Integer, AnnunciatorMessage> e = it.next();
            if (e.getValue().isTimeoutEnabled() && nowt >= e.getValue().getTimeout()) {
                it.remove();
            }
        }
    }

    public void removeMessage(int groupID) {
        messages.remove(groupID);
    }

    /**
     * Called by AnnounciatorHUDElement. Populates an array with messages to display.
     * @param rows the array to be populated with messages with.
     */
    public void getAnnunciatorRows(ArrayList<HUDMessageRow> rows) {
        rows.clear();
        if (noLocationWarning) {
            rows.add(noLocationRow);
        }
        if (noOrientationWarning) {
            rows.add(noOrientationRow);
        }
        if (doubleImageWaring) {
            rows.add(doubleImageRow);
        }
        // check if sensor display has expired
        Date now = new Date();
        if (sensorDisplayExpiration != null && sensorDisplayExpiration.before(now)) {
            // sensorDisplay expired
            sensorRow = null;
            sensorDisplayExpiration = null;
        }
        if (sensorRow != null) {
            rows.add(sensorRow);
        }

        removeExpiredMessages();
        for (AnnunciatorMessage am : messages.values()) {
            rows.add(am.getRow());
        }

        if (timeStampRow != null && timestampVisible) {
            rows.add(timeStampRow);
        }

        if (showDebug && debugRowSource != null) {
            debugRowSource.getDebugRows(rows);
        }
    }
}
