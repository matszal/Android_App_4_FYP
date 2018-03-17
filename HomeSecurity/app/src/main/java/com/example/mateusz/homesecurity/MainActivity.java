package com.example.mateusz.homesecurity;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.amazonaws.regions.Regions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity {
    static final String LOG_TAG = MainActivity.class.getCanonicalName();

    //Toast.makeText(this, String.valueOf(sdcard), Toast.LENGTH_SHORT).show();

    private static String CUSTOMER_SPECIFIC_ENDPOINT;       //cse
    private static String COGNITO_POOL_ID;                  //cp_id
    private static String AWS_IOT_POLICY_NAME;              //policy
    private static Regions MY_REGION = Regions.EU_WEST_1;
    private static String KEYSTORE_NAME;                    //key_name
    private static String KEYSTORE_PASSWORD;                //key_pass;
    private static String CERTIFICATE_ID;                   //cert_id

    private String fileName = "/credentials_app.json";

    TextView keysWindow;
    Button btnRead;
    Button btn2;

    File sdcard = new File(Environment.getExternalStorageDirectory() + fileName);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MainActivity temp = this;

        Toast.makeText(this, "App started succesfully!",
                Toast.LENGTH_SHORT).show();

        Log.i("info", "Done creating the app");


        keysWindow = (TextView) findViewById(R.id.keysViev);



        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                readStorage();
            }
        });
        t1.start();


    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void readStorage() {

        try {
            FileInputStream fis = new FileInputStream((sdcard));
            String jsonStr = null;

            try{
                FileChannel fc = fis.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                jsonStr = Charset.defaultCharset().decode(bb).toString();
            }catch(Exception e){
                Toast.makeText(this, "exception after reading json object!", Toast.LENGTH_SHORT).show();
            }
            finally {
                fis.close();
            }

            JSONObject jsonObj = new JSONObject(jsonStr);
            JSONObject data = jsonObj.getJSONObject("credentials");

            CUSTOMER_SPECIFIC_ENDPOINT = data.getString("cse");
            COGNITO_POOL_ID = data.getString("cp_id");
            AWS_IOT_POLICY_NAME = data.getString("policy");
            KEYSTORE_NAME = data.getString("key_name");
            KEYSTORE_PASSWORD = data.getString("key_pass");
            CERTIFICATE_ID = data.getString("cert_id");
            keysWindow.setText(CUSTOMER_SPECIFIC_ENDPOINT);

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error While reading file", Toast.LENGTH_SHORT).show();
        } catch (JSONException e){
            Toast.makeText(getApplicationContext(), "JSON exception", Toast.LENGTH_SHORT).show();
        }

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