package com.santiance.test.util;

import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by saurabh.khare on 2018/03/06.
 */

public class TimerCounter {
    int counter;

    private Timer timer;
    private TimerTask timerTask;

    public void startTimer(int counter) {
        this.counter = counter;

        // set a new timer
        timer = new Timer();

        // initialize the timer task's job
        initializeTimerTask();

        // schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 1000);
    }

    // it sets the timer to print the counter every x seconds
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i("TimerCounter", "in timer ++++ " + (counter++));
            }
        };
    }

    public void stopTimerTask() {
        // stop the timer, if it's not already null
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
