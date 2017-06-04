package com.google.firebase.udacity.friendlychat;

import android.util.Log;

/**
 * Created by Marshall on 06/05/2017.
 */

public class GetTiming {

    long startTime = 0;

    public GetTiming() {
        long startTime = System.nanoTime();
        double seconds = startTime / 1000000000.0;
        Log.d("NanoTest", "Received start time: " + Long.toString(startTime));
        Log.d("Seconds test", "Received start time in seconds: " + Double.toString(seconds));


    }


}
