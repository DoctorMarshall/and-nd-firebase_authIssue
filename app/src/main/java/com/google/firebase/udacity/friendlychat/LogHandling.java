package com.google.firebase.udacity.friendlychat;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Marshall on 22/05/2017.
 */

public class LogHandling {
    JSONArray newLog = new JSONArray();
    JSONObject newEntry = new JSONObject();
    private String newID;
    private double newTime;
    private double receivedTime;
    private JSONObject dataToAdd;

    public LogHandling() {
        newLog = new JSONArray();

    }

    public JSONObject newEntry(double newTime, double receivedTime) {
        this.newTime = newTime;
        this.receivedTime = receivedTime;
        try {
            dataToAdd = new JSONObject();
            dataToAdd.put(" Log_sent", String.valueOf(newTime));
            dataToAdd.put("received", String.valueOf(receivedTime));
        } catch (NullPointerException | JSONException e) {
            Log.e("TAG that wtf JSON", Log.getStackTraceString(e));
        }
        return dataToAdd;
    }

}
