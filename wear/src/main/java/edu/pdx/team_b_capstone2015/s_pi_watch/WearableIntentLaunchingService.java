/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;

/**
 * Creates a sound on the paired phone to find it.
 */
public class WearableIntentLaunchingService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SPIwearableIntentServ";

    public static final String QUERY_ON = "query_on";
    private static final String PATH_QUERY_STATUS = "/query_status";
    public static final String ACTION_TOGGLE_REQUEST = "action_toggle_request";
    public static final String ACTION_CANCEL_REQUEST = "action_request_off";

    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    private static final long CONNECTION_TIME_OUT_MS = 500;
    private GoogleApiClient mGoogleApiClient;

    public WearableIntentLaunchingService() {
        super(WearableIntentLaunchingService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
            Log.d(TAG, "disconnected due to destroy");
        }
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);

        Log.d(TAG, "start Intent Handler");
        if (mGoogleApiClient.isConnected()) {
            // Set the query off by default.
            boolean query_on = false;
            //if the intent is a toggle request then connect and toggle the value of query_on.
            if (intent.getAction().equals(ACTION_TOGGLE_REQUEST)) {
                // Get current state of the alarm.
                //DataItemBuffer is a structure holding a set af DataItems
                DataItemBuffer result = Wearable.DataApi.getDataItems(mGoogleApiClient).await();

                try {
                    if (result.getStatus().isSuccess()) {
                        if (result.getCount() >= 1) {
                            //
                            for(int i = 0; i < result.getCount(); i++){
                                //check each datapath and get data from only the query path
                                if(result.get(i).getUri().getPath().toString().contentEquals(WearableDataListenerService.PATH_QUERY_STATUS)){
                                    Log.d(TAG, "Getting data value for path "+result.get(i).getUri().getPath().toString());
                                    query_on = DataMap.fromByteArray(result.get(i).getData())
                                            .getBoolean(QUERY_ON, false);
                                }

                            }
                        } else {
                            Log.e(TAG, "Unexpected number of DataItems found.\n"
                                    + "\tExpected: 1\n"
                                    + "\tActual: " + result.getCount()+ "\n"
                            + "\tCurrent: " + DataMap.fromByteArray(result.get(0).getData())
                                    .getBoolean(QUERY_ON));
                        }
                    } else if (Log.isLoggable(TAG, Log.DEBUG)) {
                        Log.d(TAG, "onHandleIntent: failed to get current alarm state");
                    }
                } finally {
                    result.release();
                }
                // Toggle the query value
                query_on = !query_on;
                // Change notification text based on new value.
                String notificationText = query_on ? getString(R.string.turn_query_off)
                        : getString(R.string.turn_query_on);
                //update the main activity with the new data.
                WearableMainActivity.updateNotification(this, notificationText);
            }
            if(intent.getAction().equals(ACTION_CANCEL_REQUEST)) {
                Log.d(TAG, "Stop query request");
                query_on = false;
            }

            //create the putDataMapRequest with path
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_QUERY_STATUS);
            //set the boolean field
            putDataMapRequest.getDataMap().putBoolean(QUERY_ON, query_on);
            //call the DataApi to update the shared values
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapRequest.asPutDataRequest())
                    .await();
        } else {
            Log.e(TAG, "Failed to toggle alarm on phone - Client disconnected from Google Play "
                    + "Services");
        }
        mGoogleApiClient.disconnect();
        Log.d(TAG, "disconnected");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "connected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "connection suspendeded");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "connection failed");
    }


}
