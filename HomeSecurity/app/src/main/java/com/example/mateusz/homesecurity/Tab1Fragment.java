package com.example.mateusz.homesecurity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import android.widget.EditText;
import android.widget.TextView;
import java.io.UnsupportedEncodingException;
import static com.example.mateusz.homesecurity.MainActivity.LOG_TAG;
import static com.example.mateusz.homesecurity.MainActivity.mqttManager;

/**
 * Created by Mateusz on 25/03/2018.
 */

public class Tab1Fragment extends Fragment {
    private static final String TAG = "Tab1Fragment";

    //private Button btnTest1;
    private Button btnPublish;
    private Button btnClearAll;
    private TextView incomingText;
    private EditText outgoingText;

    private String id;
    private String endPoint;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.tab1_fragment, container, false);


        subscribe();
        btnPublish = (Button)view.findViewById(R.id.btnPublish);
        btnPublish.setOnClickListener(publishClick);

        btnClearAll = (Button)view.findViewById(R.id.btnClear);
        btnClearAll.setOnClickListener(clearAll);
        incomingText = (TextView)view.findViewById(R.id.incomingTxt);
        outgoingText = (EditText)view.findViewById(R.id.outgoingTxt);


        return view;
    }

    public void subscribe() {

        final String topic = "mytopic/iot/led";

        Log.d(LOG_TAG, "topic = " + topic);

        try {
            mqttManager.subscribeToTopic(topic, AWSIotMqttQos.QOS0, new AWSIotMqttNewMessageCallback() {
                    @Override
                    public void onMessageArrived(final String topic, final byte[] data) {
                        try {
                            String message = new String(data, "UTF-8");
                            Log.d(LOG_TAG, "Message arrived:");
                            Log.d(LOG_TAG, "   Topic: " + topic);
                            Log.d(LOG_TAG, " Message: " + message);

                            incomingText.append(message+"\n");

                        } catch (UnsupportedEncodingException e) {
                            Log.e(LOG_TAG, "Message encoding error.", e);
                        }
                    }
                });



            } catch (Exception e) {
                Log.e(LOG_TAG, "Subscription error.", e);
            }
        }

    View.OnClickListener publishClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String topic = "mytopic/iot/led";
            final String msg = outgoingText.getText().toString();

            try {
                mqttManager.publishString(msg, topic, AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }

        }
    };

    View.OnClickListener clearAll = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            incomingText.setText("");
            outgoingText.setText("");
        }
    };

}
