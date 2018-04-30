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
            String str = remoteMessage.getData().toString();
            int length = str.length();
            Log.e(TAG, "Message data: "+str.substring(9, length-1));
            String url = str.substring(9, length-1);


            //Intent intent = new Intent(this, MainActivity.class);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setData(Uri.parse(url));
            //startActivity(intent);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                    .setContentTitle("Home Security Notification")
                    .setContentText(remoteMessage.getData().toString())
                    .setAutoCancel(true)
                    .setSound(notificationSound)
                    .setContentIntent(pendingIntent);

            NotificationManager notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifyManager.notify(0/*ID of notification*/, notifBuilder.build());
        }


    }

}
