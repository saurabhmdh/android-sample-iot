package com.santiance.test.view.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.santiance.test.R;
import com.santiance.test.data.CacheManager;
import com.santiance.test.data.DataManager;
import com.santiance.test.util.Constants;
import com.santiance.test.view.home.MainActivity;

import timber.log.Timber;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String ACTION_LOCATION_SERVICE = "com.santiance.test.service.location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        requestPermission();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        googleMap.setMyLocationEnabled(true);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.v("Latitude ", "location " + latLng.latitude + " " + latLng.longitude);
                showDialog( latLng);
            }
        });
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);

    }

    public void showDialog(final LatLng latLng) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Save your place");
        alertDialog.setMessage("Enter Radius in meter");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (!TextUtils.isEmpty(input.getText())) {
                    final Location location = new Location("new location");
                    location.setLatitude(latLng.latitude);
                    location.setLongitude(latLng.longitude);
                    String value = input.getText().toString();
                    float radius = Constants.DEFAULT_RADIUS;
                    try {
                        radius = Float.parseFloat(value);
                    } catch (NumberFormatException e) {
                        Timber.i("Its not a valid radius so send default radius");
                    }

                    CacheManager.getInstance().put(Constants.CACHE_LOCATION, location, Location.class);
                    CacheManager.getInstance().put(Constants.POINT_RANGE_KEY, radius, Float.class);
                    new SaveTask(getApplicationContext(), location, radius).execute();
                }

            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        alertDialog.show();
    }


    static class SaveTask extends AsyncTask<Void, Void, Void> {
        @SuppressLint("StaticFieldLeak")
        Context context;
        final Location location;
        final float radius;

        SaveTask(Context context, Location location, float radius) {
            this.context = context.getApplicationContext();
            this.location = location;
            this.radius = radius;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            DataManager.getInstance().saveLocation(context, (float)location.getLatitude(), (float) location.getLongitude(), radius);
            Timber.i("Data has been saved to database..");
            Intent intent = new Intent(ACTION_LOCATION_SERVICE);
            intent.setPackage(context.getPackageName());
            context.startService(intent);
            context = null;
            return null;
        }
    }
}
