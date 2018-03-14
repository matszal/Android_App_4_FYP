package com.example.mateusz.homesecurity;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {

    private static String CUSTOMER_SPECIFIC_ENDPOINT1;
    private static String CUSTOMER_SPECIFIC_ENDPOINT2;
    private static String CUSTOMER_SPECIFIC_ENDPOINT3;

    TextView keysWindow;
    Button btnRead;

    File sdcard = new File(Environment.getExternalStorageDirectory()+ "/key.txt");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MainActivity temp = this;

        Toast.makeText(this, "App started succesfully!",
                Toast.LENGTH_SHORT).show();

        Log.i ("info", "Done creating the app");
        Toast.makeText(this, String.valueOf(sdcard), Toast.LENGTH_SHORT).show();

        keysWindow = (TextView) findViewById(R.id.keysViev);

        btnRead = (Button)findViewById(R.id.readKeysBtn);
        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivityPermissionsDispatcher.readStorageWithPermissionCheck(temp);

            }
        });

    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void readStorage() {
        StringBuilder sb = new StringBuilder();

        //Call this from onCreate as a new thread%%%%%%%%%%%%%%%%%%%%%to be implemented as next...
        try {
            BufferedReader br = new BufferedReader(new FileReader(sdcard));
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            br.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error While reading file", Toast.LENGTH_SHORT).show();
        }
        keysWindow.setText(sb);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void readStorageRationale(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("To read internal storage, enable Read Storage")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.proceed();
                    }
                })
                .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void sendMessageNever() {
        Toast.makeText(this, "You have denied permission", Toast.LENGTH_SHORT).show();
    }
}
