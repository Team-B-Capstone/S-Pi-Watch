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

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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
    public static final String PATH_PATIENT = "/patient";
    private static final String TITLE = "title";
    private static final String NAME = "name";
    private static final String BED = "bed" ;

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

        Log.d(TAG, "Message: " + data.getString("message"));
        Map<String, String> alert;
        if (data.getString("title").contentEquals("SPI ALERT!")
                && PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_GCMNotifications", false)) {
            alert = parseJSON(data.getString("message"));
            alert.put(NAME,data.getString(NAME,"John Doe"));
            alert.put(BED,data.getString(BED,"N/A"));
            alert.put(TITLE,data.getString(TITLE,"Alert: "));
            sendNotification(alert);
        }
    }


    //parses the json and returns a list of JSON objects each containing an alert;
    private Map<String, String> parseJSON(String result) {
        Map<String, String> dataMap = new HashMap<>();
        JSONObject alert;
        try {
            alert = new JSONObject(result);
            dataMap.put(TITLE,"ALERT:");
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

    //create and send the notification
    private void sendNotification(Map<String, String> alert) {
        Log.d(TAG, "Sending notificaton for ID: " + alert.get(ID));
        Integer notificationId = Integer.valueOf(alert.get(ID));



         Notification secondPageNotification =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Alert Message")
                        .setContentText(alert.get(ALERT_MSG))

                        .build();

         Notification thirdPageNotification =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Action Message")
                        .setContentText(alert.get(ACTION_MSG))
                        .setColor(Color.RED)
                        .build();

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(alert.get(TITLE))
                        .setContentText(alert.get(NAME) +
                                "\nPatient ID: " + alert.get(ID) +
                                "\nBed: " + alert.get(BED) +
                                "\nAlert Type: " + alert.get(SIGNAME) +
                                "\nLength: " + alert.get(INTERVAL))
                        .extend(new NotificationCompat.WearableExtender()
                                        .addPage(secondPageNotification)
                                        .addPage(thirdPageNotification)
                                        .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.red))
                                        .setHintScreenTimeout(NotificationCompat.WearableExtender.SCREEN_TIMEOUT_LONG)
                        )
                        .setVibrate(new long[]{0, 1000, 1000, 1000})
                        .setPriority(Notification.PRIORITY_MAX)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                ;

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());

    }


}

