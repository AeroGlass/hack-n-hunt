package aero.glass.primary;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import aero.glass.unit.LengthUnit;

import static aero.glass.unit.LengthUnit.Meter;

/**
 * Contains the state of the activity and saves/loads it.
 * Created by DrakkLord on 2015. 10. 13..
 */
public class ActivityStateComponent {
    protected Activity activity;

    public static final String PREFERENCES_NAME = AeroActivity.class.toString();

    private static final String PREFS_LOOK_AHEAD = "look_ahead" ;
    private static final String PREFS_URBAN_MODE = "urban_mode" ;
    private static final String PREFS_LENGTH_UNIT = "length_unit" ;
    private static final String PREFS_LAST_ROUTE = "last_route" ;
    private static final String PREFS_LAST_CNP = "last_cnp" ;

    private boolean lookAhead;
    private boolean urbanMode;
    private LengthUnit lengthUnit;
    private String lastRoute;
    private int lastCNP;

    protected ActivityStateComponent(AeroActivity ca) {
        activity = ca;
    }

    /** Called when the preferences should be loaded and stored for later use. */
    protected void load() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        lookAhead = prefs.getBoolean(PREFS_LOOK_AHEAD, true);
        urbanMode = prefs.getBoolean(PREFS_URBAN_MODE, false);
        lengthUnit = LengthUnit.valueOf(prefs.getString(PREFS_LENGTH_UNIT, String.valueOf(Meter)));
        lastRoute = prefs.getString(PREFS_LAST_ROUTE, null);
        lastCNP = prefs.getInt(PREFS_LAST_CNP, 0);
    }

    /** Called when the currently stored preferences should be saved for later use. */
    protected void save() {
        SharedPreferences prefs = activity.getSharedPreferences(PREFERENCES_NAME,
                                                                Context.MODE_PRIVATE);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PREFS_LOOK_AHEAD, lookAhead);
        edit.putBoolean(PREFS_URBAN_MODE, urbanMode);
        edit.putString(PREFS_LENGTH_UNIT, lengthUnit.toString());
        edit.putString(PREFS_LAST_ROUTE, lastRoute);
        edit.putInt(PREFS_LAST_CNP, lastCNP);
        edit.apply();
    }

    public synchronized boolean isLookAhead() {
        return lookAhead;
    }

    public synchronized void setLookAhead(boolean lookAhead) {
        this.lookAhead = lookAhead;
    }

    public synchronized boolean isUrbanMode() {
        return urbanMode;
    }

    public synchronized void setUrbanMode(boolean urbanMode) {
        this.urbanMode = urbanMode;
    }

    public synchronized LengthUnit getLengthUnit() {
        return lengthUnit;
    }

    public synchronized void setLengthUnit(LengthUnit lengthUnit) {
        this.lengthUnit = lengthUnit;
    }

    public synchronized String getLastRoute() {
        return lastRoute;
    }

    public synchronized void setLastRoute(String lastRoute) {
        this.lastRoute = lastRoute;
    }

    public synchronized int getLastCNP() {
        return lastCNP;
    }

    public synchronized void setLastCNP(int lastCNP) {
        this.lastCNP = lastCNP;
    }
}
