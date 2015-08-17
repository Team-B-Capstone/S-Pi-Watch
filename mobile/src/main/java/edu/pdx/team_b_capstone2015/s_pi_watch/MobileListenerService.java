package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;

import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MobileListenerService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks {

    //public final static String apiURL = "http://api.s-pi-demo.com/patients";
    public static final String FIELD_QUERY_ON = "query_on";
    public static final String PATH_PATIENTS = "/patients";
    public static final String PATH_PATIENT = "/patient";
    public static final String PATH_QUERY_STATUS = "/query_status";

    //REST API keys
    private static final String NAME = "name";
    private static final String BED = "bed" ;
    private static final String ID = "id";
    private static final String AGE = "age";
    private static final String TEMP = "temperature";
    private static final String HEIGHT = "height";
    private static final String BP = "blood_pressure" ;
    private static final String STATUS = "status";
    private static final String CASE_ID = "case_id" ;
    private static final String H_ID = "hospital_admission_id" ;
    private static final String CARDIAC = "cardiac";
    private static final String ALLERGIES = "allergies";
    private static final String WEIGHT = "weight";
    private static final String HEART_RATE = "heart-rate";
    private static final String P_ID= "patient_id";
    private static final String[] keys = {NAME,BED,ID,AGE,TEMP,HEIGHT,BP,STATUS,CASE_ID,H_ID,CARDIAC,ALLERGIES,WEIGHT,HEART_RATE,P_ID};
    private static final byte[] DATA_AVAILABLE = "Data_available".getBytes() ;
    private static final String GET_PATIENTS = "Get_Patients";
    private GoogleApiClient mGoogleApiClient;
    private static final String TAG = "SPI-MobileService";

    @Override
    public void onCreate() {
        super.onCreate();

        //Log.d(TAG, "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy");
        if (mGoogleApiClient.isConnected() || mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d(TAG, "message recieved: path = " + messageEvent.getPath() + " Message = " + new String(messageEvent.getData()));
        if(messageEvent.getPath().contentEquals(PATH_QUERY_STATUS)){
            if(new String(messageEvent.getData()).contentEquals(GET_PATIENTS)){
                String json = getPatients();
                if(json != null){
                    mGoogleApiClient = new GoogleApiClient.Builder(this)
                            .addApi(Wearable.API)
                            .addConnectionCallbacks(this)
                            .build();
                    mGoogleApiClient.blockingConnect();
                    updateData(json);
                    //send a response so the wearable knows the data is available
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, messageEvent.getSourceNodeId(), PATH_QUERY_STATUS, DATA_AVAILABLE);
                    mGoogleApiClient.disconnect();
                }
            }
        }

    }

    //queries the http REST api for patient info
    //retuns a json string with all patient info
    private String getPatients() {

        String server = "http://"
                + PreferenceManager.getDefaultSharedPreferences(this).getString("pref_ipPref","api.s-pi-demo.com");
        String path = "/patients";
        String result;
        InputStream in;
        // HTTP Get
        try {
            Log.d(TAG, "getting patient names from "+ server + path);
            URL url = new URL(server+path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            in = urlConnection.getInputStream();

        } catch (Exception ex ) {
            try{
                URL url = new URL(server+":8080"+path);
                Log.d(TAG, "getting patient names from "+ url.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = urlConnection.getInputStream();

            }catch(Exception e){
                Log.e(TAG, "Error during HTTP connection");
                return null;
            }

        }

        result = convertStreamToString(in);
        //Log.d(TAG, "server returned from patients call " + result);
        return result;
    }
    //converts a stream to a string
    String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    //updates the shared dataitems
    //takes a json string
    private void updateData(String result){


        PutDataMapRequest put = PutDataMapRequest.create(PATH_PATIENTS);

        DataApi.DataItemResult status =
                Wearable.DataApi.putDataItem(mGoogleApiClient, put.asPutDataRequest()).await();
        Log.d(TAG, "Put results: " + status.getStatus().toString());
        //send each patient

        for(PutDataMapRequest p : parseJS(result)){
            Log.d(TAG, "Puting " + p.getUri().getPath());
            Wearable.DataApi.putDataItem(mGoogleApiClient, p.asPutDataRequest()).await();
        }



    }

    @Override
    public void onConnected(Bundle bundle) {
        //Log.d(TAG, "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Log.d(TAG, "onConnectionSuspended");
    }

    //parses the json and returns a list of PutDataMaps for each patient with the path set to patient#
    private List<PutDataMapRequest> parseJS(String result) {
        ArrayList<PutDataMapRequest> putDataMapRequest = new ArrayList<>();
        DataMap data;
        //Parse json
        //Create JSON object
        JSONObject allPatients, patient;
        try {
            allPatients = new JSONObject(result);
            for (int i = 0; i < 4; i++){
                patient = new JSONObject(allPatients.getString( Integer.toString(i+1) )); //patient ids are 1-4 in json
                PutDataMapRequest put = PutDataMapRequest.create(PATH_PATIENT + Integer.toString(i));//patient paths are /patient0 - /patient3
                for(String k : keys){
                    if(k.contains(TEMP)){
                        put.getDataMap().putString(k, Integer.toString(Double.valueOf(patient.getString(k)).intValue())); //temp needs to be a int
                    }else {
                        put.getDataMap().putString(k, patient.getString(k));
                    }
                }
                putDataMapRequest.add(put);
            }

        } catch (JSONException e) {
            Log.d(TAG, "JSON ERROR");
            e.printStackTrace();
            return null;
        }
        return putDataMapRequest;
    }

}
