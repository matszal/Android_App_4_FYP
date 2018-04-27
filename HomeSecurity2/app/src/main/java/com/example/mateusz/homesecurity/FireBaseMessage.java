package com.example.mateusz.homesecurity;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FireBaseMessage extends FirebaseMessagingService  {

    private static final String TAG = "MsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        Log.d(TAG, "FROM"+ remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data: "+ remoteMessage.getData());
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                    .setContentTitle("SNS Message")
                    .setContentText(remoteMessage.getData().toString())
                    .setAutoCancel(true)
                    .setSound(notificationSound)
                    .setContentIntent(pendingIntent);

            NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.notify(0/*ID of notification*/, notifBuilder.build());
        }


    }

}
