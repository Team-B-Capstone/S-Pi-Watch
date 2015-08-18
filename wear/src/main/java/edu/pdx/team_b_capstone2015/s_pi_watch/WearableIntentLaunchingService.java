
package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.util.Set;

public class WearableIntentLaunchingService extends IntentService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "SPI-IntentService";
    public static final String ACTION_START_REQUEST = "action_start_request";
    private static final String CAPABILITY_NAME = "Query_S-PI";

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
        CapabilityApi.GetCapabilityResult result = Wearable.CapabilityApi.getCapability(
                mGoogleApiClient, CAPABILITY_NAME,
                CapabilityApi.FILTER_REACHABLE).await();

        String cap = result.getCapability().getName();
        if(cap.contentEquals("Query_S-PI")){
            Log.d(TAG, "capability found: " + cap );
            Log.d(TAG, "start Intent Handler " + intent.getAction());
            if (mGoogleApiClient.isConnected()) {


                // Set the query off by default.
                //boolean query_on = false;
                //if the intent is a toggle request then connect and toggle the value of query_on.
                if (intent.getAction().equals(ACTION_START_REQUEST)) {
                    Log.d(TAG, "start query request");
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, pickBestNodeId(result.getCapability().getNodes()), MainActivity.PATH_QUERY_STATUS, "Get_Patients".getBytes());
                    //query_on = true;
                }

            } else {
                Log.e(TAG, "Failed to toggle alarm on phone - Client disconnected from Google Play "
                        + "Services");
            }
        }else{
            Log.d(TAG, "No capable app found on mobile");
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

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

}
