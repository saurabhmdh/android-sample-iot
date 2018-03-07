package com.santiance.test.controllers.services;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;

import com.santiance.test.data.CacheManager;
import com.santiance.test.data.DataManager;
import com.santiance.test.util.Constants;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by saurabh.khare on 2018/03/07.
 */

public class StartUpService {
    private Context context;
    public void start(Context context) {
        this.context = context;
        new BackGroundExecutor(this).execute();
    }

    private static class BackGroundExecutor extends AsyncTask<Void, Void, Void> {
        private final WeakReference<StartUpService> weakReference;

        public BackGroundExecutor(StartUpService service) {
            this.weakReference = new WeakReference<>(service);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            StartUpService service = weakReference.get();
            //service can be null, when GC collected all weak references !!!
            if (service == null) {
                return null;
            }
            //Load location information & store in cache
            Location location = DataManager.getInstance().getSavedlocation(service.context);
            CacheManager.getInstance().put(Constants.CACHE_LOCATION, location, Location.class);

            //Load Range information
            Float range = DataManager.getInstance().getSavedRange(service.context);
            CacheManager.getInstance().put(Constants.POINT_RANGE_KEY, range, Float.class);

            //Load in-Range information
            Boolean inRange = DataManager.getInstance().getInRangeInformation(service.context);
            CacheManager.getInstance().put(Constants.PREF_IN_RANGE, inRange, Boolean.class);
            Timber.i("Startup service completed the work !!!");
            return null;
        }
    }
}
