package aero.glass;

import android.content.Context;
import android.content.SharedPreferences;

import aero.glass.primary.ActivityStateBaseComponent;
import aero.glass.primary.AeroActivity;

/**
 * Contains the state of the activity and saves/loads it.
 * Created by DrakkLord on 2015. 10. 13..
 */
public class ActivityStateComponent extends ActivityStateBaseComponent{

    public static final String PREFERENCES_NAME = AeroActivity.class.toString();

    protected ActivityStateComponent(AeroActivity ca) {
        super(ca);
    }

    /** Called when the preferences should be loaded and stored for later use. */
    public void load() {
        super.load();
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
    }

    /** Called when the currently stored preferences should be saved for later use. */
    protected void save() {
        super.save();
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME,
                                                                Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.apply();
    }

}
