package com.santiance.test.view.home;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.santiance.test.R;
import com.santiance.test.databinding.ActivityMainBinding;
import com.santiance.test.view.map.MapsActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final String ACTION_LOCATION_SERVICE = "com.santiance.test.service.location";

    private Button savePointButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //Starting service

        Intent intent = new Intent(ACTION_LOCATION_SERVICE);
        intent.setPackage(getPackageName());
        startService(intent);

        launchAddLocationScreen();

        savePointButton = (Button) findViewById(R.id.save_point_button);
        savePointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saveProximityAlertPoint();
            }
        });

    }


    private void launchAddLocationScreen() {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }
}
