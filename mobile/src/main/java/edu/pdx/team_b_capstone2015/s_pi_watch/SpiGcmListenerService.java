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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
public class SpiGcmListenerService extends GcmListenerService {

    private static final String TAG = "SPIGcmListenerService";

    private static final String ID = "PATIENT_ID";
    private static final String TS = "TS";
    private static final String SIGNAME = "SIGNAME";
    private static final String INTERVAL = "INTERVAL";
    private static final String ALERT_MSG = "ALERT_MSG";
    private static final String ACTION_MSG = "ACTION_MSG";
    private static final String[] keys = {ID, TS, SIGNAME, INTERVAL, ALERT_MSG, ACTION_MSG};
    private static final String TITLE = "TITLE";
//    private static final String NAME = "name";
//    private static final String BED = "bed" ;


    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {

        Log.d(TAG, "Message Received");
        Map<String, String> alert;
        String title = data.getString("title");
        if ((title != null) && title.contentEquals("SPI ALERT!")
                && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_GCMNotifications", false)) {
            alert = parseJSON(data.getString("message"));
            //alert.put(NAME, data.getString(NAME, "Unknown"));
            //String id = alert.get(ID);
            //Uri uri = Uri.parse("wear:/patient"+id);
            //alert.put(BED, data.getString(BED,"N/A"));
            alert.put(TITLE, data.getString(TITLE, "Alert!"));
            Intent send = new Intent(this, SpiMobileIntentService.class).setAction("SEND_NOTIFICATION");
            //put all the alert data into the Intent
            for(String s: alert.keySet()){
                send.putExtra(s,alert.get(s));
            }
            startService(send);
        }
    }


    //parses the json and returns a list of JSON objects each containing an alert;
    private Map<String, String> parseJSON(String result) {
        Map<String, String> dataMap = new HashMap<>();
        JSONObject alert;
        try {
            alert = new JSONObject(result);
            for (String s : keys) {
                //Log.d(TAG, s + " is " + alert.get(s));
                dataMap.put(s, alert.getString(s));
            }

        } catch (JSONException e) {
            Log.d(TAG, "JSON ERROR: " + result);
            e.printStackTrace();
            return null;
        }
        return dataMap;
    }
}

