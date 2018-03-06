package com.santiance.test.view.alert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.santiance.test.R;
import com.santiance.test.view.home.MainActivity;

public class AlertDetails extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_ACCESS_LOCATION = 0;
    private static final String[] PERMISSIONS_LIST = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_details);
        checkPermissions();
    }

    private void checkPermissions() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            proceedToNextStep();
        } else {
            requestPermission();
        }
    }

    private void proceedToNextStep() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, PERMISSIONS_LIST,
                PERMISSION_REQUEST_ACCESS_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if (grantResults.length == PERMISSIONS_LIST.length) {
                boolean allPermissionGranted = true;
                for (int i =0; i < grantResults.length; ++i) {
                    allPermissionGranted &= (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }
                if (allPermissionGranted) {
                    proceedToNextStep();
                } else {
                    permissionsNotGranted();
                }
            } else {
                permissionsNotGranted();
            }
        }
    }

    private boolean checkPermission(String permission) {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void permissionsNotGranted() {
        Toast.makeText(this, R.string.permission_denied_message, Toast.LENGTH_LONG).show();
        finish();
    }
}
