package com.google.firebase.udacity.friendlychat;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Marshall on 13/06/2017.
 */

public class AnalyzeData {

    public ArrayList<Double> latencies;
    public ArrayList<Double> twoWayLatencies;
    public double average;
    public double latencyMin;
    public double latencyMax;
    public static ArrayList<Double> phone2ReceivedDifferences = new ArrayList<>();


    public AnalyzeData() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)

    }

    public double getAverage(ArrayList<Double> latencies) {

        if (latencies == null || latencies.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Double latency : latencies) {
            sum += latency;
        }
        average = sum / latencies.size();

        return average;
    }

    public double getMin(ArrayList<Double> latencies) {
        if (latencies == null || latencies.isEmpty()) {
            return 0;
        }
        latencyMin = Collections.min(latencies);
        return latencyMin;
    }

    public double getMax(ArrayList<Double> latencies) {
        if (latencies == null || latencies.isEmpty()) {
            return 0;
        }
        latencyMax = Collections.max(latencies);
        return latencyMax;
    }

    //extension


    public double getReceivedTimeDifference(ArrayList<Double> latencies) {
        if (latencies == null || latencies.isEmpty()) {
            return 0;
        }

        double diff = 0;
        double sum = 0;
        double averageLatency = 0;
        int differences = 0;

        for (int i = 0; i < latencies.size() - 1; ++i) {
            diff = Math.abs(latencies.get(i) - latencies.get(i + 1));
            if (diff > 0.5) {
                sum = sum + diff;
                phone2ReceivedDifferences.add(diff);
                differences++;
            }
        }
        averageLatency = sum / differences;
        return averageLatency;
    }

}
