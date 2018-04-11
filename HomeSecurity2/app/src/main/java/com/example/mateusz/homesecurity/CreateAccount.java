package com.example.mateusz.homesecurity;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class CreateAccount extends BaseActivity {

    private EditText userName;
    private EditText userPassword;
    private Button createAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        userName = (EditText) findViewById(R.id.userAccountName);
        userPassword = (EditText) findViewById(R.id.userPassword);
        createAccount = (Button) findViewById(R.id.createAccountButton);

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

    }

    private void createAccount(){
        final String name = userName.getText().toString();
        final String password = userPassword.getText().toString();

        if (name.equals("") || password.equals("")){
            final AlertDialog.Builder dialog = new AlertDialog.Builder(CreateAccount.this);
            dialog.setTitle("Empty fields");
            dialog.setMessage("Please complete the form");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
        else{

            ParseUser user = new ParseUser();
            user.setUsername(name);
            user.setPassword(password);

            //set custom property
            user.put("city", "Galway");

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null){
                        createAccount.setEnabled(false);
                        userName.setEnabled(false);
                        userPassword.setEnabled(false);

                        //log them in
                        logUserIn(name, password);
                    }
                }
            });
        }

    }

    private void logUserIn(String name, String password) {
        if (!name.equals("") || !password.equals("")){
            ParseUser.logInInBackground(name, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e == null){
                        Log.v("User logged in", user.getUsername());
                        startActivity(new Intent(CreateAccount.this, MQTTActivity.class));
                    }
                    else {

                    }
                }
            });
        }
        else {

        }
    }
}
