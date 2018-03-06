package com.santiance.test.controllers.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.santiance.test.util.Constants;
import com.santiance.test.util.TimerCounter;
import com.santiance.test.view.home.MainActivity;


public class LocationService extends Service {
    private static final String PROX_ALERT_INTENT = "com.santiance.test.proximity.alert";
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    private LocationManager locationManager;
    private LocationChangeListener listener;
    private Location previousBestLocation = null;

    private static long MIN_LOCATION_TIME = 60 * 1000;
    private static long MIN_LOCATION_DISTANCE = 100;
    private TimerCounter timerCounter; //To check my service is running or not..
    private int counter = 0;

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

        Log.i("LocationService" , "onStartCommand");
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
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
        Log.i("LocationService", "onDestroy called hence restart needed");
        Intent broadcastIntent = new Intent("com.santiance.test.service.location");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        sendBroadcast(broadcastIntent);
        timerCounter.stopTimerTask();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("LocationService", "onTaskRemoved called..");
        Intent intent = new Intent(getApplicationContext(), LocationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 5000, pendingIntent);
        super.onTaskRemoved(rootIntent);
    }

    public class LocationChangeListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            Log.i("LocationService", "Location changed");
            if (isBetterLocation(loc, previousBestLocation)) {
                Intent intent = new Intent(PROX_ALERT_INTENT);
                intent.putExtra("Latitude", loc.getLatitude());
                intent.putExtra("Longitude", loc.getLongitude());
                intent.putExtra("Provider", loc.getProvider());
                sendBroadcast(intent);
//                LocationHelper.sendLocation(loc.getLatitude(), loc.getLongitude(),
//                        new MyPreferenceManager(App.context).getAuthToken());
                Log.i("LocationService", "Location : " + loc.getLatitude() + ":" + loc.getLongitude());
            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
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
        Log.i("LocationService", "onLowMemory()");
    }

//    public void onLocationChanged(Location location) {
//        Location pointLocation = retrievelocationFromPreferences();
//        float distance = location.distanceTo(pointLocation);
//        Toast.makeText(MainActivity.this,
//                "Distance from Point:"+distance, Toast.LENGTH_LONG).show();
//    }

    private Location retrievelocationFromPreferences() {
        SharedPreferences prefs = this.getSharedPreferences(getClass().getSimpleName(),
                        Context.MODE_PRIVATE);
        Location location = new Location("POINT_LOCATION");
        location.setLatitude(prefs.getFloat(Constants.POINT_LATITUDE_KEY, 0));
        location.setLongitude(prefs.getFloat(Constants.POINT_LONGITUDE_KEY, 0));
        return location;
    }
}

