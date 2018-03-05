package com.santiance.test.view.home;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.santiance.test.R;
import com.santiance.test.databinding.ActivityMainBinding;
import com.santiance.test.view.map.MapsActivity;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //Add a listview which will listen from viewmodel


        //TODO: launching intent to add location infiormation to db.

        launchAddLocationScreen();
    }

    private void launchAddLocationScreen() {
        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
        startActivity(intent);
        finish();
    }
}
