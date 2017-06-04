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
    private int newID;
    private double newTime;
    private JSONObject dataToAdd;

    public LogHandling() {
        newLog = new JSONArray();
    }

    public JSONObject newEntry(int newID, double newTime) {
        this.newID = newID;
        this.newTime = newTime;
        try {
            dataToAdd = new JSONObject();
            dataToAdd.put(String.valueOf(newID), String.valueOf(newTime));
        } catch (NullPointerException | JSONException e) {
            Log.e("TAG that wtf JSON", Log.getStackTraceString(e));
        }
        return dataToAdd;
    }

}
