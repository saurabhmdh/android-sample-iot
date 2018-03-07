package com.santiance.test.controllers.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;

import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.santiance.test.data.CacheManager;
import com.santiance.test.data.DataManager;
import com.santiance.test.util.Constants;
import com.santiance.test.util.TimerCounter;

import timber.log.Timber;


public class LocationService extends IntentService {
    private static final String PROX_ALERT_INTENT = "com.santiance.test.proximity.alert";
    private static final int TIME_DELTA_MINUTES = 1000 * 60 * 2;

    private LocationManager locationManager;
    private LocationChangeListener listener;
    private Location previousBestLocation = null;

    private static long MIN_LOCATION_TIME = 60 * 1000;
    private static long MIN_LOCATION_DISTANCE = 100;
    private TimerCounter timerCounter; //To check my service is running or not..
    private int counter = 0;

    public LocationService() {
        super("LocationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        listener = new LocationChangeListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        timerCounter = new TimerCounter();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return START_STICKY;
        }
        Criteria criteria = getCriteria();
        String provider = locationManager.getBestProvider(criteria, true);

        if (provider != null) {
            locationManager.requestLocationUpdates(provider, MIN_LOCATION_TIME,
                    MIN_LOCATION_DISTANCE, listener);
        } else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_LOCATION_TIME, MIN_LOCATION_DISTANCE, listener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_LOCATION_TIME, MIN_LOCATION_DISTANCE, listener);
        }
        timerCounter.startTimer(counter);

        Timber.i( "onStartCommand");
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Timber.i("onHandled Intent");
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_DELTA_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TIME_DELTA_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        Timber.i("onDestroy called hence restart needed");
        Intent broadcastIntent = new Intent("com.santiance.test.service.location");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        sendBroadcast(broadcastIntent);
        timerCounter.stopTimerTask();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Timber.i("onTaskRemoved called..");
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    public class LocationChangeListener implements LocationListener {

        public void onLocationChanged(final Location currentLocation) {
            Timber.i("Location changed");
            if (isBetterLocation(currentLocation, previousBestLocation)) {

                //Check if its in range or out of range
                Location savedLocation = CacheManager.getInstance().get(Constants.CACHE_LOCATION, Location.class);
                Float distance = currentLocation.distanceTo(savedLocation); //Approx on meter
                Float savedRadius = CacheManager.getInstance().get(Constants.POINT_RANGE_KEY, Float.class);

                Timber.i("Saved location: Latitude = " + savedLocation.getLatitude() + " Longitude = " + savedLocation.getLongitude());
                Timber.i("Current location: Latitude = " + currentLocation.getLatitude() + " Longitude = " + currentLocation.getLongitude());

                Timber.i("Distance between saved point & current " + distance);
                Timber.i("Saved radius " + savedRadius);

                if (distance.compareTo(savedRadius) > 0) { //Out of range
                    Boolean inRange = CacheManager.getInstance().get(Constants.PREF_IN_RANGE, Boolean.class);
                    if (inRange) {
                        updateCheckInProcess(false);
                    }
                } else { //in Range
                    Boolean inRange = CacheManager.getInstance().get(Constants.PREF_IN_RANGE, Boolean.class);
                    if (!inRange) {
                        updateCheckInProcess(true);
                    }
                }

            }
        }

        public void onProviderDisabled(String provider) {
            Timber.i("GPS disabled");
        }


        public void onProviderEnabled(String provider) {
            Timber.i("GPS enabled");
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }

    /** To save power */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        return criteria;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Timber.i( "onLowMemory()");
    }

    public void updateCheckInProcess(boolean status) {
        //Update Cache
        CacheManager.getInstance().put(Constants.PREF_IN_RANGE, status, Boolean.class);
        //Change inRange value to out of range
        DataManager.getInstance().saveInRangeInformation(getApplicationContext(), status);
        //send notification
        sendNotificationIntent(status);

        Timber.i("Checkin process done !!!");
    }

    public void sendNotificationIntent(boolean inRange) {
        Intent intent = new Intent(PROX_ALERT_INTENT);
        Bundle extraData = new Bundle();
        extraData.putBoolean(Constants.BUNDLE_IN_RANGE, inRange);
        intent.putExtras(extraData);
        sendBroadcast(intent);
    }

}

