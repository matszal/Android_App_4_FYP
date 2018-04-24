package com.example.mateusz.homesecurity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.parse.ParseUser;

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



public class MQTTActivity extends BaseActivity implements View.OnClickListener {

    static final String LOG_TAG = MainActivity.class.getCanonicalName();
    private static Regions MY_REGION = Regions.EU_WEST_1;

    String clientId;
    String keystorePath;
    String keystoreName;
    String keystorePassword;

    KeyStore clientKeyStore = null;
    String certificateId;

    //TextView keysWindow;
    Button btnCon;
    Button btnDisc;
    Button btnSetTemp;

    Button devON;
    Button devOFF;

    TextView tvClientId;
    TextView tvStatus;

    EditText setTempTxt;


    AWSIotClient mIotAndroidClient;
    AWSIotMqttManager mqttManager;
    CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);

        setTempTxt = (EditText) findViewById(R.id.setTempText);
        //setTempTxt.setFilters(new InputFilter[]{new InputFilterMM("17", "27")});
        tvClientId = (TextView) findViewById(R.id.tvClientId);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        btnCon = (Button)findViewById(R.id.btnConnect);
        btnCon.setEnabled(false);
        btnDisc = (Button)findViewById(R.id.btnDisconnect);
        devON = (Button) findViewById(R.id.deviceON);
        devON.setEnabled(false);
        devOFF = (Button)findViewById(R.id.deviceOFF);
        devOFF.setEnabled(false);
        btnSetTemp = (Button)findViewById(R.id.setTemp);
        btnSetTemp.setEnabled(false);

        devON.setOnClickListener(this);
        devOFF.setOnClickListener(this);
        btnCon.setOnClickListener(this);
        btnDisc.setOnClickListener(this);
        btnSetTemp.setOnClickListener(this);


        clientId = UUID.randomUUID().toString();
        tvClientId.setText(clientId);

        //Log.i("¢######################", clientId);
        Log.i("¢######################", Constants.CUSTOMER_SPECIFIC_ENDPOINT.toString());

        // Initialize the AWS Cognito credentials provider
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(), // context
                Constants.COGNITO_POOL_ID, // Identity Pool ID
                MY_REGION // Region
        );

        Region region = Region.getRegion(MY_REGION);

        // MQTT Client
        mqttManager = new AWSIotMqttManager(clientId, Constants.CUSTOMER_SPECIFIC_ENDPOINT);

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
        keystoreName = Constants.KEYSTORE_NAME;
        keystorePassword = Constants.KEYSTORE_PASSWORD;
        certificateId = Constants.CERTIFICATE_ID;


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
                    btnCon.setEnabled(true);
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
                        policyAttachRequest.setPolicyName(Constants.AWS_IOT_POLICY_NAME);
                        policyAttachRequest.setPrincipal(createKeysAndCertificateResult
                                .getCertificateArn());
                        mIotAndroidClient.attachPrincipalPolicy(policyAttachRequest);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnCon.setEnabled(true);
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

    @Override
    public void onClick(View view){

        final String topic = "mytopic/iot/led";

        switch (view.getId()){

            case R.id.deviceON:

                //final String msg = outgoingText.getText().toString();

                try {
                    mqttManager.publishString("on", topic, AWSIotMqttQos.QOS0);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Publish error.", e);
                }
                break;
            case R.id.deviceOFF:

                try {
                    mqttManager.publishString("off", topic, AWSIotMqttQos.QOS0);
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Publish error.", e);
                }
                break;
            case R.id.btnConnect:
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
                                        btnCon.setEnabled(false);
                                        //subscribeLed();
                                        devOFF.setEnabled(true);
                                        devON.setEnabled(true);
                                        btnSetTemp.setEnabled(true);
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
                                        btnCon.setEnabled(true);
                                        devOFF.setEnabled(false);
                                        devON.setEnabled(false);
                                    } else {
                                        tvStatus.setText("Disconnected");
                                        btnCon.setEnabled(true);
                                        devOFF.setEnabled(false);
                                        devON.setEnabled(false);
                                        btnSetTemp.setEnabled(false);
                                    }
                                }
                            });
                        }
                    });
                } catch (final Exception e) {
                    Log.e(LOG_TAG, "Connection error.", e);
                    tvStatus.setText("Error! " + e.getMessage());
                }


                break;
            case R.id.btnDisconnect:
                try {
                    mqttManager.disconnect();
                    ParseUser.logOut();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Disconnect error.", e);
                }
                break;

            case R.id.setTemp:
                String strToNum = setTempTxt.getText().toString();
                 //int temperature = Integer.valueOf(strToNum);

                if (strToNum.matches("")) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(MQTTActivity.this);
                    dialog.setTitle("Empty field");
                    dialog.setMessage("Please set the temperature");
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
                else{
                    Toast.makeText(this, "Temperature set!", Toast.LENGTH_SHORT).show();

                }

                break;
        }

    }

}

