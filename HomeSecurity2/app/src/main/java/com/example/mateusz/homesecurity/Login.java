package com.example.mateusz.homesecurity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class Login extends BaseActivity {

    private Button logIn;
    private EditText userName;
    private EditText userPassword;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        logIn = (Button) findViewById(R.id.loginButton);
        userName = (EditText) findViewById(R.id.usernameId);
        userPassword = (EditText) findViewById(R.id.passwordId);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = userName.getText().toString();
                String password = userPassword.getText().toString();

                if (!name.equals("") || !password.equals("")){
                    ParseUser.logInInBackground(name, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (e == null) {

                                Toast.makeText(getApplicationContext(), "Login Successfully!"
                                        , Toast.LENGTH_LONG).show();

                                startActivity(new Intent(Login.this, MQTTActivity.class));

                            }else {

                                Toast.makeText(getApplicationContext(), "Not logged in",
                                        Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                }
                else {
                    Toast.makeText(getApplicationContext(), "Please enter username and Password",
                            Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}
