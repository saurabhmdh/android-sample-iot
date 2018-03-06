package com.santiance.test.controllers.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.santiance.test.controllers.services.LocationService;

/**
 * Created by saurabh.khare on 2018/03/06.
 */

public class LocationServiceRestartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("RestartBroadcast", " onReceive");
        context.startService(new Intent(context, LocationService.class));
        Log.i("LocationService", " calling start again..");
    }
}
