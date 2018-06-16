package aero.glass.renderer.aerohud;

import java.util.ArrayList;

/**
 * DebugRowSource interface provides debug messages for the Annunciator.
 * The Annunciator then either displays them or not, depending on user preferences.
 * Created by krisoft on 18/01/16.
 */
public interface DebugRowSource {

    /** Populates an array with debug messages to render.
     *  @param rows array to be populated with messages
     * */
    void getDebugRows(ArrayList<HUDMessageRow> rows);
}
