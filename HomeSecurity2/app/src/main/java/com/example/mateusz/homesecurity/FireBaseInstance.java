package com.example.mateusz.homesecurity;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FireBaseInstance extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseInsIDService";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshtoken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "New token"+refreshtoken);
    }
}
