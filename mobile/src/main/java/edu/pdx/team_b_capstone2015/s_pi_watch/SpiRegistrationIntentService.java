/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class SpiRegistrationIntentService extends IntentService  {
    private static String registrationToken;
    private String host;// = "api.s-pi-demo.com";//"10.0.0.7";
    int PORT = 9996;
    private static final String TAG = "SPI-RegIntentService";

    public SpiRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "GCM: Preparing to handle intent");
        //SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(intent.getAction().contentEquals("REGISTER")){
            try {
                // In the (unlikely) event that multiple refresh operations occur simultaneously,
                // ensure that they are processed sequentially.
                synchronized (TAG) {
                    InstanceID instanceID = InstanceID.getInstance(this);
                    // project id is: "50371043678"
                    registrationToken = instanceID.getToken("50371043678",
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    // [END get_token]
                    Log.i(TAG, "GCM Registration Token: " + registrationToken);

                    // send registration token to server
                    sendRegistrationToServer();

                }
            } catch (Exception e) {
                Log.d(TAG, "Failed to complete token refresh", e);
                // If an exception happens while fetching the new token or updating our registration data
                // on a third-party server, this ensures that we'll attempt the update at a later time.
                //sharedPreferences.edit().putBoolean(GcmPreferences.SENT_TOKEN_TO_SERVER, false).apply();
            }
        }
        if(intent.getAction().contentEquals("UNREGISTER")) {
            if(registrationToken == null){
                InstanceID instanceID = InstanceID.getInstance(this);
                // project id is: "50371043678"
                try {
                    registrationToken = instanceID.getToken("50371043678",GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to complete token refresh", e);
                    return;
                }
            }
            unregisterFromServer();
        }
    }
    /**
     * Send registration to our servers.
     *
     */
    private void sendRegistrationToServer() {
        //get the ip for the vertx server from the preferences.
        host = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_ipPref","api.s-pi-demo.com");
        new Thread(new Runnable() {
            public void run() {
                try {

                    Log.i(TAG, "GCM: Sending token to server at host: " + host + " and port: " + PORT + ": " + registrationToken);
                    Socket clientSocket = new Socket(host, PORT);
//                    Socket clientSocket = new Socket("131.252.208.103", PORT);
                    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    //outToServer.writeBytes("STRT"+registrationToken);
                    outToServer.writeBytes(registrationToken);
                    outToServer.flush();
                    //outToServer.close();

                    //reciept from server is not working yet....
                    BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String resp = inFromServer.readLine();
                    Log.i(TAG, "GCM: FROM SERVER: " + resp);
                    clientSocket.close();
                } catch(Exception e) {
                    Log.i(TAG, "sendRegistrationToServer ERROR:" + e);
                }

            }
        }).start();
    }
    //this is not currently being used.
    private void unregisterFromServer() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    Log.i(TAG, "GCM: stopping gcm server at host: " + host + " and port: " + PORT + ": " + registrationToken);
                    Socket clientSocket = new Socket(host, PORT);
                    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    outToServer.writeBytes("STOP" + registrationToken);
                    outToServer.flush();
                    clientSocket.close();


                } catch(Exception e) {
                    Log.i(TAG, "ERROR:" + e);
                }

            }
        }).start();
    }

}
