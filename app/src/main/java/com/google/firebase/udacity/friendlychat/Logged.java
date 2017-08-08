package com.google.firebase.udacity.friendlychat;

/**
 * Created by Marshall on 13/06/2017.
 */


public class Logged {


    public Integer itemID;
    public Double timeSent;
    public Double timeReceived;
    public Double latency;
    public Double twoWayLatency;
    public Double receivedAt;
    public Double receiveTimeDifference;
    public Double timeThroughReceiver;

//    public ArrayList<Double> latencies;
//    public double average;
//    public double latencyMin;
//    public double latencyMax;


    public Logged() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)

    }

    public Logged(Integer itemID, Double timeSent, Double timeReceived, Double latency, Double twoWayLatency, Double receivedAt, Double receiveTimeDifference, Double timeThroughReceiver) {
        this.itemID = itemID;
        this.timeSent = timeSent;
        this.timeReceived = timeReceived;
        this.latency = latency;
        this.twoWayLatency = twoWayLatency;
        this.receivedAt = receivedAt;
        this.receiveTimeDifference = receiveTimeDifference;
        this.timeThroughReceiver = timeThroughReceiver;

    }
}