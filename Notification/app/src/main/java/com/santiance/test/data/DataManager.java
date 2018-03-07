package com.santiance.test.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.santiance.test.util.Constants;

/**
 * Created by saurabh.khare on 2018/03/07.
 */

public class DataManager {
    private static final DataManager _instance = new DataManager();

    private DataManager() {
    }

    public static DataManager getInstance() {
        return _instance;
    }

    /**
     * Save data to preferences
     * */
    public void saveLocation(Context context, float latitude, float longitude, float range) {
        SharedPreferences prefs = context.getSharedPreferences(getClass().getSimpleName(),
                        Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putFloat(Constants.POINT_LATITUDE_KEY, latitude);
        prefsEditor.putFloat(Constants.POINT_LONGITUDE_KEY, longitude);
        prefsEditor.putFloat(Constants.POINT_RANGE_KEY, range);
        prefsEditor.apply();
    }

    /**
     * get location from database
     * */
    public Location getSavedlocation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getClass().getSimpleName(),
                Context.MODE_PRIVATE);
        Location location = new Location("POINT_LOCATION");
        location.setLatitude(prefs.getFloat(Constants.POINT_LATITUDE_KEY, 0));
        location.setLongitude(prefs.getFloat(Constants.POINT_LONGITUDE_KEY, 0));

        return location;
    }

    public float getSavedRange(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getClass().getSimpleName(),
                Context.MODE_PRIVATE);
        return prefs.getFloat(Constants.POINT_RANGE_KEY, 0);
    }

    /**
     * In Range will be true -> when user enter into provided area
     * */
    public void saveInRangeInformation(Context context, boolean isInRange) {
        SharedPreferences prefs = context.getSharedPreferences(getClass().getSimpleName(),
                Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();
        prefsEditor.putBoolean(Constants.PREF_IN_RANGE, isInRange);
        prefsEditor.apply();
    }

    public boolean getInRangeInformation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getClass().getSimpleName(),
                Context.MODE_PRIVATE);
        return prefs.getBoolean(Constants.PREF_IN_RANGE, true);
    }

}
