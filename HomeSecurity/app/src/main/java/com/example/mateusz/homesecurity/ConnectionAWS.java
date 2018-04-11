package com.example.mateusz.homesecurity;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

public class ConnectionAWS extends IntentService {

    private final static String TAG = "ConnectionAWS";

    public ConnectionAWS() {
        super("ConnectionAWS");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        //my service will execute here
        Log.i(TAG, "in connectionAWS class");
    }
}
