package edu.pdx.team_b_capstone2015.s_pi_watch;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi.MessageListener;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity implements DataApi.DataListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private Context context;
    private GridViewPager pager;
    private TextView mIntroText;
    Intent startQueryOperation;
    private static final int REQUEST_RESOLVE_ERROR = 1000;
    public static final String PATH_QUERY_STATUS = "/query_status";
    public static final String PATH_PATIENT = "/patient";
    private static final String PATH_NOTIFICATION = "/Notification";
    public static final int MAX_PATIENTS = 4 ;
    private static final String TAG = "SPI-MainActivity";
    private GoogleApiClient mGoogleApiClient;
    private boolean mResolvingError = false;
    public static List<Map<String,String>> patientData;
    public static final String NAME = "name";
    public static final String BED = "bed" ;
    public static final String ID = "id";
    public static final String AGE = "age";
    public static final String TEMP = "temperature";
    public static final String HEIGHT = "height";
    public static final String BP = "blood_pressure" ;
    public static final String STATUS = "status";
    public static final String CASE_ID = "case_id" ;
    public static final String H_ID = "hospital_admission_id" ;
    public static final String CARDIAC = "cardiac";
    public static final String ALLERGIES = "allergies";
    public static final String WEIGHT = "weight";
    public static final String HEART_RATE = "heart-rate";
    public static final String P_ID= "patient_id";
    public static final String SIGNAME = "SIGNAME";
    public static final String INTERVAL = "INTERVAL";
    public static final String ALERT_MSG = "ALERT_MSG";
    public static final String ACTION_MSG = "ACTION_MSG";
    public static final String[] keys = {NAME,BED,ID,AGE,TEMP,HEIGHT,BP,STATUS,CASE_ID,H_ID,CARDIAC,ALLERGIES,WEIGHT,HEART_RATE,P_ID};
    public static final String[] notificationKeys = {SIGNAME,INTERVAL,ALERT_MSG,ACTION_MSG};
    private static final String DATA_AVAILABLE = "Data_available";
    private boolean running;

    //called when the activity is first created. We do the initial set up of the activity here. This would include creating views and binding data to lists.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        //create a list of maps containing the patient data.
        patientData = new ArrayList<>();
        for(int i = 0; i<MAX_PATIENTS;i++){
            patientData.add(i, new HashMap<String, String>());
        }
        //google api client for message passing to mobile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        //listen for changes in data while app is open
        //listens for a responce from the mobile that the patient data is available
        Wearable.MessageApi.addListener(mGoogleApiClient, new MessageListener() {
            @Override
            public void onMessageReceived(MessageEvent messageEvent){
                //Log.d(TAG, "message recieved: path = " + messageEvent.getPath() + "Message = " + new String(messageEvent.getData()));
                if(messageEvent.getPath().contentEquals(PATH_QUERY_STATUS)){
                    if(new String(messageEvent.getData()).contentEquals(DATA_AVAILABLE)){
                        Log.i(TAG, "Patient data downloaded");

                        //update the patient info in the main activity
                        DataItemBuffer databuffer = Wearable.DataApi.getDataItems(mGoogleApiClient).await();
                        int cnt = databuffer.getCount();
                        for(int i = 0; i < cnt; i++){
                            String path = databuffer.get(i).getUri().getPath().toString();
                            //check path against each patient path
                            if(path.contentEquals(PATH_QUERY_STATUS)) continue;
                            for (int j = 0; j < MAX_PATIENTS; j++) {
                                String pth = PATH_PATIENT + Integer.toString(j);
                                if (path.contentEquals(pth)) {
                                    //put the dataItem in the array location corresponding to the path num.
                                    //copy each key into the map
                                    for(String k : keys){
                                        patientData.get(j).put(k, DataMap.fromByteArray(databuffer.get(i).getData()).getString(k));
                                    }
                                    continue;
                                }
                            }
                        }
                        databuffer.close();
                        createPagerAdapter();
                    }
                }
                //update the notifications if they change while the app is open.
//                if(messageEvent.getPath().contentEquals(PATH_NOTIFICATION)){
//                    DataMap data = DataMap.fromByteArray(messageEvent.getData());
//                    Map<String,String> patient = patientData.get(Integer.parseInt(data.getString(ID)));
//                    for(String s: notificationKeys){
//                        patient.put(s, data.getString(s));
//                    }
//                    Log.d(TAG,"updating pager");
//                    pager.getAdapter().notifyDataSetChanged();
//                }

            }
        });





        // Create a notification with an action to toggle if the phone is queryng the Rest API.
        startQueryOperation = new Intent(this, WearableIntentLaunchingService.class);
        //set the action for the intent
        startQueryOperation.setAction(WearableIntentLaunchingService.ACTION_START_REQUEST);
/*        //Create a pendingIntent for intent with request code 0 don't generate more then one at a time.
        PendingIntent toggleQueryIntent = PendingIntent.getService(this, 0, startQueryOperation,
                PendingIntent.FLAG_CANCEL_CURRENT);

        // This intent cancels the queries, used when the app is closed
        cancelQueryOperation = new Intent(this, WearableIntentLaunchingService.class);
        cancelQueryOperation.setAction(WearableIntentLaunchingService.ACTION_CANCEL_REQUEST);
        PendingIntent cancelQueryIntent = PendingIntent.getService(this, 0, cancelQueryOperation,
                PendingIntent.FLAG_CANCEL_CURRENT);*/


        setContentView(R.layout.activity_main);
        mIntroText = (TextView) findViewById(R.id.intro);

        pager = (GridViewPager) findViewById(R.id.pager);
        //sets the adapter for the pager
        //pager.setAdapter(new PatientViewAdapter(this, getFragmentManager()));
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);
        mGoogleApiClient.connect();
    }

    //saved the app state when rotating, prevents multiple registrations.
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("running", running);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null){
            running = savedInstanceState.getBoolean("running");
        }
    }

    //called when the activity is becoming visible to the user. onStart() is followed by onResume()
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    //method is always called when the activity is being placed in the background or is about to be destroyed. This is where we can save our persistent data
    @Override
    protected void onPause() {
        super.onPause();
        //startService(cancelQueryOperation);
        mGoogleApiClient.disconnect();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
    }

    //method is called when the activity starts interacting with the user.
    @Override
    protected void onResume() {
        super.onResume();

    }

    //Method is called called after the activity has stopped, just before it’s to be started again. onRestart() is always followed by onStart()
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    //Method is called when the activity is destroyed.
    @Override
    protected void onDestroy() {
        //stop the mobile from quering the REST API
        //startService(cancelQueryOperation);
        super.onDestroy();

    }


    //ConnectionCallbacks
    @Override
    public void onConnected(Bundle connectionHint) {
        mResolvingError = false;
        Log.d(TAG, "onConnected");
        if(!running) {
            startService(startQueryOperation);
        }
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    //ConnectionCallbacks
    @Override
    public void onConnectionSuspended(int cause) {
            //maybe set wallpaper to indicate data is not live.
       }

    //OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            Log.e(TAG, "Connection to Google API client has failed");
            mResolvingError = false;

        }
    }


    //creates the pager with the data.
    public void createPagerAdapter(){
        runOnUiThread(new Runnable(){
            @Override
            public void run() {

                //pager.getAdapter().notifyDataSetChanged();
                //check to see if the activity was opened by a notification and set to correct row if  it was
                Intent intent = getIntent();
                final String patientId = intent.getStringExtra(ID);
                if(patientId != null){
                    //add a listener so that as soon as the layout is created we can change the row.
                    pager.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right,
                                                   int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            //remove this listener as it should only be used once
                            pager.removeOnLayoutChangeListener(this);
                            pager.setCurrentItem(Integer.parseInt(patientId), 0, false);
                            pager.getAdapter().notifyDataSetChanged();

                        }
                    });
                }
                pager.setAdapter(new PatientViewAdapter(context, getFragmentManager()));
                pager.setVisibility(View.VISIBLE);
                mIntroText.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onDataChanged(DataEventBuffer events) {
        for (DataEvent event : events) {
            Log.d(TAG, "URI path: " + event.getDataItem().getUri().getPath().toString());
            if (event.getType() == DataEvent.TYPE_DELETED) {
                Log.i(TAG, event + " deleted");
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {//Indicates that the enclosing DataEvent was triggered by a data item being added or changed.
                //Log.i(TAG, event + " data Changed");
                //get a data item from the data map
                //queryServer is set to true when the wearable app is active
                if (event.getDataItem().getUri().getPath().toString().contentEquals(PATH_QUERY_STATUS)) {
                    //ignore this type of event and exit.
                    continue;
                }

                for (int i = 0; i < MAX_PATIENTS; i++) {
                    String path = event.getDataItem().getUri().getPath().toString();
                    //check each patient for updates, allows a individual patient to be updated as the data changes.
                    if (path.contentEquals(PATH_PATIENT + Integer.toString(i))) {
                        Log.i(TAG, "Patient" + i + " data changed");
                        for(String k : keys){
                            patientData.get(i).put(k, DataMap.fromByteArray(event.getDataItem().getData()).getString(k));
                        }
                        pager.getAdapter().notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
