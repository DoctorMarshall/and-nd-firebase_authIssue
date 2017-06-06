package com.google.firebase.udacity.friendlychat;

/**
 * Created by Marshall on 06/05/2017.
 */

public class GetTiming {

    long startTime = 0;

    public GetTiming() {
//        long startTime = System.nanoTime();
//        double seconds = startTime / 1000000000.0;
    }

    public long giveTiming() {
        long startTime = System.nanoTime();
        return startTime;
    }


}
