package com.example.mateusz.homesecurity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG="MainActivity";
    private Button loginButton;
    private Button createAccount;
    private Button getToken;
    private Button goToS3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




       /* Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("a605003a0c655a69bdf510387f141a495c79cd38")
                .clientKey("34a315a5adc38ce9566a14d2dd33d7e49240080e")
                .server("http://34.245.171.7:80/parse/")
                .build()
        );


        ParseObject testObj = new ParseObject("Test");

        testObj.put("test", "test2");
        testObj.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if (e == null){
                    Log.i("ParserServer", "All ok");
                }
                else {
                    Log.e("ParserServer", e.getMessage());
                }
            }
        });
*/


        Toast.makeText(this, "App started succesfully!", Toast.LENGTH_SHORT).show();
        Log.i("info", "Done creating the app");

        //check for storage access permissions
        //instance if mainactivity has to be passed on to permission dispatcher Android > 6 reqs
        final MainActivity temp = this;
        //MainActivityPermissionsDispatcher.readStorageWithPermissionCheck(temp);

        loginButton = (Button) findViewById(R.id.createAcc);
        createAccount = (Button) findViewById(R.id.login);
        getToken = (Button) findViewById(R.id.token);
        goToS3 = (Button) findViewById(R.id.toS3);

        //Check internet access
        if(!isNetworkAvailable()){
            //Create alertdialog
            AlertDialog.Builder Checkbuilder = new AlertDialog.Builder(MainActivity.this);
            Checkbuilder.setIcon(R.drawable.error);
            Checkbuilder.setTitle("Connection Error");
            Checkbuilder.setMessage("Check Internet Connection");
            //Builder Retry Button
            Checkbuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //Restart the Activity
                    Intent intent = getIntent();
                    finish();
                    startActivity(intent);
                }
            });

            Checkbuilder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            AlertDialog alert = Checkbuilder.create();
            alert.show();

        }
        else {

            loginButton.setOnClickListener(this);
            createAccount.setOnClickListener(this);
            getToken.setOnClickListener(this);
            goToS3.setOnClickListener(this);


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ParseUser.logOut();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.login:
                startActivity(new Intent(MainActivity.this, Login.class));
                break;
            case R.id.createAcc:
                startActivity(new Intent(MainActivity.this, CreateAccount.class));
                break;
            case R.id.token:
                String token = FirebaseInstanceId.getInstance().getToken();
                Log.i("info", token);
                Toast.makeText(MainActivity.this, token,Toast.LENGTH_SHORT).show();
                break;
            case R.id.toS3:
                startActivity(new Intent(MainActivity.this, S3Activity.class));
                break;


        }

    }


    private boolean isNetworkAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        //return true or false based on connection
        return activeNetworkInfo != null;

    }

}
