
package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

public class WearableIntentLaunchingService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SPI-IntentService";

    public static final String QUERY_ON = "query_on";
    private static final String PATH_QUERY_STATUS = "/query_status";
    public static final String ACTION_START_REQUEST = "action_start_request";
    public static final String ACTION_CANCEL_REQUEST = "action_cancel_request";

    // Timeout for making a connection to GoogleApiClient (in milliseconds).
    //private static final long CONNECTION_TIME_OUT_MS = 500;
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
        }

        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mGoogleApiClient.blockingConnect();

        Log.d(TAG, "start Intent Handler " + intent.getAction());
        if (mGoogleApiClient.isConnected()) {
            // Set the query off by default.
            boolean query_on = false;
            //if the intent is a toggle request then connect and toggle the value of query_on.
            if (intent.getAction().equals(ACTION_START_REQUEST)) {
                Log.d(TAG, "start query request");
                query_on = true;
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
            Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapRequest.asPutDataRequest()).await();

        } else {
            Log.e(TAG, "Failed to toggle alarm on phone - Client disconnected from Google Play "
                    + "Services");
        }
        mGoogleApiClient.disconnect();
        Log.d(TAG, "disconnected");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        //Log.d(TAG, "connected");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        //Log.d(TAG, "connection suspendeded");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        //Log.d(TAG, "connection failed");
    }


}
