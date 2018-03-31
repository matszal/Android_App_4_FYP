package com.example.mateusz.homesecurity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttLastWillAndTestament;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.iot.AWSIotClient;
import com.amazonaws.services.iot.model.AttachPrincipalPolicyRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateRequest;
import com.amazonaws.services.iot.model.CreateKeysAndCertificateResult;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.util.UUID;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends AppCompatActivity{
    static final String LOG_TAG = MainActivity.class.getCanonicalName();

    private static String CUSTOMER_SPECIFIC_ENDPOINT;       //cse
    private static String COGNITO_POOL_ID;                  //cp_id
    private static String AWS_IOT_POLICY_NAME;              //policy
    private static Regions MY_REGION = Regions.EU_WEST_1;
    private static String KEYSTORE_NAME;                    //key_name
    private static String KEYSTORE_PASSWORD;                //key_pass;
    private static String CERTIFICATE_ID;                   //cert_id


    String clientId;
    String keystorePath;
    String keystoreName;
    String keystorePassword;

    KeyStore clientKeyStore = null;
    String certificateId;

    private String fileName = "/credentials_app.json";

    //TextView keysWindow;
    Button btnConnect;
    Button btnDisconnect;
    Button btn;

    Button devON;
    Button devOFF;

    TextView tvClientId;
    TextView tvStatus;

    File sdcard = new File("sdcard/AWS_CREDENTIALS" + fileName);

    static AWSIotClient mIotAndroidClient;
    static AWSIotMqttManager mqttManager;
    CognitoCachingCredentialsProvider credentialsProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Allowing Strict mode policy for Nougat support
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Toast.makeText(this, "App started succesfully!",
                Toast.LENGTH_SHORT).show();

        Log.i("info", "Done creating the app");

        //check for storage access permissions
        final MainActivity temp = this;
        MainActivityPermissionsDispatcher.readStorageWithPermissionCheck(temp);
        readStorage();

        //Check if app has an access to internet
        if (!AppStatus.getInstance(this).isOnline()) {

           // Toast.makeText(this,"Please check your internet connection",Toast.LENGTH_LONG).show();
            Log.v("Home", "############################You are not online!!!!");
            //btnConnect.setEnabled(false);
        }

        tvClientId = (TextView) findViewById(R.id.tvClientId);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        btnConnect = (Button)findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(connect);
        btnConnect.setEnabled(false);
        btnDisconnect = (Button)findViewById(R.id.btnDisconnect);
        btnDisconnect.setOnClickListener(disconnect);
        devON = (Button) findViewById(R.id.deviceON);
        devON.setEnabled(false);
        devON.setOnClickListener(publish);
        devOFF = (Button)findViewById(R.id.deviceOFF);
        devOFF.setEnabled(false);
        devOFF.setOnClickListener(publish2);
        btn = (Button) findViewById(R.id.getToken);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String token = FirebaseInstanceId.getInstance().getToken();
                Log.i("info", token);
                Toast.makeText(MainActivity.this, token,Toast.LENGTH_SHORT).show();
            }
        });



        // MQTT client IDs are required to be unique per AWS IoT account.
        // This UUID is "practically unique" but does not _guarantee_
        // uniqueness.
        clientId = UUID.randomUUID().toString();
        tvClientId.setText(clientId);

        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        Region region = Region.getRegion(MY_REGION);

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_ENDPOINT);

        // Set keepalive to 10 seconds.  Will recognize disconnects more quickly but will also send
        // MQTT pings every 10 seconds.
        mqttManager.setKeepAlive(10);

        // Set Last Will and Testament for MQTT.  On an unclean disconnect (loss of connection)
        // AWS IoT will publish this message to alert other clients.
        AWSIotMqttLastWillAndTestament lwt = new AWSIotMqttLastWillAndTestament("my/lwt/topic",
                "Android client lost connection", AWSIotMqttQos.QOS0);
        mqttManager.setMqttLastWillAndTestament(lwt);

        // IoT Client (for creation of certificate if needed)
        mIotAndroidClient = new AWSIotClient(credentialsProvider);
        mIotAndroidClient.setRegion(region);

        keystorePath = getFilesDir().getPath();
        keystoreName = KEYSTORE_NAME;
        keystorePassword = KEYSTORE_PASSWORD;
        certificateId = CERTIFICATE_ID;

        // To load cert/key from keystore on filesystem
        try {
            if (AWSIotKeystoreHelper.isKeystorePresent(keystorePath, keystoreName)) {
                if (AWSIotKeystoreHelper.keystoreContainsAlias(certificateId, keystorePath,
                        keystoreName, keystorePassword)) {
                    Log.i(LOG_TAG, "Certificate " + certificateId
                            + " found in keystore - using for MQTT.");
                    // load keystore from file into memory to pass on connection
                    clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                            keystorePath, keystoreName, keystorePassword);
                    btnConnect.setEnabled(true);
                } else {
                    Log.i(LOG_TAG, "Key/cert " + certificateId + " not found in keystore.");
                }
            } else {
                Log.i(LOG_TAG, "Keystore " + keystorePath + "/" + keystoreName + " not found.");
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "An error occurred retrieving cert/key from keystore.", e);
        }

        if (clientKeyStore == null) {
            Log.i(LOG_TAG, "Cert/key was not found in keystore - creating new key and certificate.");

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create a new private key and certificate. This call
                        // creates both on the server and returns them to the
                        // device.
                        CreateKeysAndCertificateRequest createKeysAndCertificateRequest =
                                new CreateKeysAndCertificateRequest();
                        createKeysAndCertificateRequest.setSetAsActive(true);
                        final CreateKeysAndCertificateResult createKeysAndCertificateResult;
                        createKeysAndCertificateResult =
                                mIotAndroidClient.createKeysAndCertificate(createKeysAndCertificateRequest);
                        Log.i(LOG_TAG,
                                "Cert ID: " +
                                        createKeysAndCertificateResult.getCertificateId() +
                                        " created.");

                        // store in keystore for use in MQTT client
                        // saved as alias "default" so a new certificate isn't
                        // generated each run of this application
                        AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certificateId,
                                createKeysAndCertificateResult.getCertificatePem(),
                                createKeysAndCertificateResult.getKeyPair().getPrivateKey(),
                                keystorePath, keystoreName, keystorePassword);

                        // load keystore from file into memory to pass on
                        // connection
                        clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certificateId,
                                keystorePath, keystoreName, keystorePassword);

                        // Attach a policy to the newly created certificate.
                        // This flow assumes the policy was already created in
                        // AWS IoT and we are now just attaching it to the
                        // certificate.
                        AttachPrincipalPolicyRequest policyAttachRequest =
                                new AttachPrincipalPolicyRequest();
                        policyAttachRequest.setPolicyName(AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnConnect.setEnabled(true);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(LOG_TAG,
                                "Exception occurred when generating new private key and certificate.",
                                e);
                    }
                }
            }).start();
        }
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
               // Toast.makeText(this, "exception after reading json object!", Toast.LENGTH_SHORT).show();
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
    void readStorageNever() {
        Toast.makeText(this, "You have denied permission", Toast.LENGTH_SHORT).show();
    }


    public static class AppStatus {

        private static AppStatus instance = new AppStatus();
        static Context context;
        ConnectivityManager connectivityManager;
        NetworkInfo wifiInfo, mobileInfo;
        boolean connected = false;

        public static AppStatus getInstance(Context ctx) {
            context = ctx.getApplicationContext();
            return instance;
        }

        public boolean isOnline() {
            try {
                connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                connected = networkInfo != null && networkInfo.isAvailable() &&
                        networkInfo.isConnected();
                return connected;


            } catch (Exception e) {
                System.out.println("CheckConnectivity Exception: " + e.getMessage());
                Log.v("connectivity", e.toString());
            }
            return connected;
        }
    }

    View.OnClickListener connect = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {

            Log.d(LOG_TAG, "clientId = " + clientId);

            try {

                mqttManager.connect(clientKeyStore, new AWSIotMqttClientStatusCallback() {
                    @Override
                    public void onStatusChanged(final AWSIotMqttClientStatus status,
                                                final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (status == AWSIotMqttClientStatus.Connecting) {
                                tvStatus.setText("Connecting...");

                            } else if (status == AWSIotMqttClientStatus.Connected) {
                                tvStatus.setText("Connected");
                                btnConnect.setEnabled(false);
                                subscribeLed();
                                devOFF.setEnabled(true);
                                devON.setEnabled(true);
                                //if (!alreadyEcecuted){
                                  //  Intent intent = new Intent(v.getContext(), TabbedActivity.class);
                                    //v.getContext().startActivity(intent);
                                    //alreadyEcecuted = true;
                                //}

                            } else if (status == AWSIotMqttClientStatus.Reconnecting) {
                                if (throwable != null) {
                                    Log.e(LOG_TAG, "Connection error.", throwable);
                                }
                                tvStatus.setText("Reconnecting");
                            } else if (status == AWSIotMqttClientStatus.ConnectionLost) {
                                if (throwable != null) {
                                    Log.e(LOG_TAG, "Connection error.", throwable);
                                }
                                tvStatus.setText("Disconnected");
                                btnConnect.setEnabled(true);
                                devOFF.setEnabled(false);
                                devON.setEnabled(false);
                            } else {
                                tvStatus.setText("Disconnected");
                                btnConnect.setEnabled(true);
                                devOFF.setEnabled(false);
                                devON.setEnabled(false);
                            }
                        }
                    });
                    }
                });
            } catch (final Exception e) {
                Log.e(LOG_TAG, "Connection error.", e);
                tvStatus.setText("Error! " + e.getMessage());
            }

        }

    };



    View.OnClickListener disconnect = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            try {
                mqttManager.disconnect();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Disconnect error.", e);
            }

        }
    };

    public void subscribeLed() {

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

                        //incomingText.append(message+"\n");

                    } catch (UnsupportedEncodingException e) {
                        Log.e(LOG_TAG, "Message encoding error.", e);
                    }
                }
            });



        } catch (Exception e) {
            Log.e(LOG_TAG, "Subscription error.", e);
        }
    }

    View.OnClickListener publish = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String topic = "mytopic/iot/led";
            //final String msg = outgoingText.getText().toString();

            try {
                mqttManager.publishString("on", topic, AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }

        }
    };
    View.OnClickListener publish2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String topic = "mytopic/iot/led";
            //final String msg = outgoingText.getText().toString();

            try {
                mqttManager.publishString("off", topic, AWSIotMqttQos.QOS0);
            } catch (Exception e) {
                Log.e(LOG_TAG, "Publish error.", e);
            }

        }
    };
}

