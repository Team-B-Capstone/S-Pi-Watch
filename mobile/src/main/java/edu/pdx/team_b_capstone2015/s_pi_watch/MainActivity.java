package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;



public class MainActivity extends Activity {
    private static final String TAG = "SPI-MAIN";
    private static boolean registered = false;
    // Google Cloud Messaging helper objects:
    //private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    //private WebSocketClient mWebSocketClient;
    //private static final String WEBSOCKET_HOST = "ws://10.0.07:8080";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //detects changes in the preferences to automatically register with a different vertx server.
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(
                new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
                Log.i(TAG, key+" preference changed");
                if(key.contentEquals("pref_ipPref")) {
                    registerWithGC();
                }
            }
        });

        if(!registered){
            registerWithGC();
            registered = true;
        }

        // Display the fragment as the main content.

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
/*  OLD TEST CODE
        setContentView(R.layout.activity_main);
        //connectWebSocket();
        //sets the preferences on the first startup only.
        PreferenceManager.setDefaultValues(this,R.xml.preferences,false);
        alertsActive = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("@string/GCM_notifications",true);
        final Button regButton = (Button) findViewById(R.id.button);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerWithGC();
            }
        });
        final Button unregButton = (Button) findViewById(R.id.unregbutton);

        if(alertsActive){
            unregButton.setText(getString(R.string.enable_alerts));
        }else{
            unregButton.setText(getString(R.string.disable_alerts));
        }
        unregButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //toggle the alerts active flag
                if(alertsActive){
                    alertsActive = false;
                }else{
                    alertsActive = true;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //reset the text on the button.
                        if (GcmPreferences.alertsActive) {
                            unregButton.setText(getString(R.string.enable_alerts));
                        } else {
                            unregButton.setText(getString(R.string.disable_alerts));
                        }
                    }
                });
            }
        });

    }*/


    //saved the app state when rotating, prevents multiple registrations.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("registered",registered);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null){
            registered = savedInstanceState.getBoolean("registered");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    // registers the local device with google cloud server
    // allows backend to send messages (alerts!)
    protected void registerWithGC() {
        if (checkPlayServices()) {
            Log.i(TAG, "GCM: Initiating Intent: Registering with GCM");
            // Start IntentService to register this application with GCM.
            Intent GCMRegistration = new Intent(MainActivity.this, SpiRegistrationIntentService.class);
            GCMRegistration.setAction("REGISTER");
            startService(GCMRegistration);
        }
    }

    protected void unregisterWithGC() {

        if (checkPlayServices()) {
            Log.i(TAG, "GCM: Initiating Intent: unregistering with GCM");
            // Start IntentService to register this application with GCM.
            Intent GCMRegistration = new Intent(MainActivity.this, SpiRegistrationIntentService.class);
            GCMRegistration.setAction("UNREGISTER");
            startService(GCMRegistration);
        }
    }


    /**
     * Check the device to make sure this device has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
/*  Not yet implemented
    //connect to a websocket
    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI(WEBSOCKET_HOST);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {

            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i(TAG, "WebSocket Opened");
                mWebSocketClient.send("Hello from " + Build.MANUFACTURER + " " + Build.MODEL);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button button2 = (Button)findViewById(R.id.button2);
                        EditText editText = (EditText)findViewById(R.id.outMessage);
                        TextView textView = (TextView)findViewById(R.id.messages);
                        button2.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        editText.setVisibility(View.VISIBLE);
                    }
                });
            }

            @Override
            public void onMessage(String s) {

                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView)findViewById(R.id.messages);
                        textView.setText(textView.getText() + "\n" + message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                //if closed by other end
                Log.i(TAG,"Websocket Closed " + s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Button button2 = (Button) findViewById(R.id.button2);
                        EditText editText = (EditText) findViewById(R.id.outMessage);
                        TextView textView = (TextView) findViewById(R.id.messages);
                        button2.setVisibility(View.INVISIBLE);
                        textView.setVisibility(View.INVISIBLE);
                        editText.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.i(TAG,"Websocket Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }
    public void sendMessage(View view) {
        EditText editText = (EditText)findViewById(R.id.outMessage);
        if(mWebSocketClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)){
            mWebSocketClient.send(editText.getText().toString());
        }
        editText.setText("");
    }*/


}
