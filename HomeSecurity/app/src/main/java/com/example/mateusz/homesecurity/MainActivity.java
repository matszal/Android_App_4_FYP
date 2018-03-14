package com.example.mateusz.homesecurity;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String CUSTOMER_SPECIFIC_ENDPOINT1;
    private static String CUSTOMER_SPECIFIC_ENDPOINT2;
    private static String CUSTOMER_SPECIFIC_ENDPOINT3;

    File sdcard = Environment.getExternalStorageDirectory();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toast.makeText(this, "App started succesfully!",
                Toast.LENGTH_SHORT).show();

        //Log.i ("info", "*Done creating the app");
        Toast.makeText(this, String.valueOf(sdcard), Toast.LENGTH_SHORT).show();

    }

    public void readKeysClick (View v) {




        String path = "ket.txt";
        //String[] stringArr;
        TextView keysWindow = (TextView) findViewById(R.id.keysViev);
        Toast.makeText(this, String.valueOf(sdcard+" "+path), Toast.LENGTH_SHORT).show();
        File file = new File(sdcard, path);
        List<String> text = new ArrayList<String>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.add(line);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(this, String.valueOf(text), Toast.LENGTH_SHORT).show();
        int numOfElements = text.size();

        String[] stringArr = new String[0];
        for (int i = 0; i < numOfElements; i++) {
            stringArr = text.toArray(new String[i]);
        }
        String test = stringArr[0];
        CUSTOMER_SPECIFIC_ENDPOINT2 = "test";//stringArr[1];
        //CUSTOMER_SPECIFIC_ENDPOINT3 = stringArr[2];

        //Log.i("info", "%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + String.valueOf(test));

        //keysWindow.setText(String.valueOf(CUSTOMER_SPECIFIC_ENDPOINT2));


    }
}
