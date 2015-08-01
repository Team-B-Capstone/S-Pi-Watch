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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.google.android.gms.R.*;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpiGcmListenerService extends GcmListenerService {

    private static final String TAG = "SPIGcmListenerService";
    private static final String TITLE = "PATIENT_ID";
    private static final String ID = "PATIENT_ID";
    private static final String TS = "TS";
    private static final String SIGNAME = "SIGNAME";
    private static final String INTERVAL = "INTERVAL";
    private static final String ALERT_MSG = "ALERT_MSG";
    private static final String ACTION_MSG = "ACTION_MSG";
    private static final String[] keys = {ID, TS, SIGNAME, INTERVAL, ALERT_MSG, ACTION_MSG};
    private static final String GROUP_KEY = "PatientAlert";

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
        //DEBUG
        String message = data.getString("message");
        Log.d(TAG, "From: " + from);
        Log.d(TAG, "Message: " + message);

        Map<String, String> alert;
        if (data.getString("title").contentEquals("SPI ALERT") && GcmPreferences.alertsActive) {

            alert = parseJSON(data.getString("message"));
            // set the Title
            //alert.put(TITLE, "ALERT: ");
            sendNotification(alert);
        }


    }
    // [END receive_message]

//    private void sendNotification(Map<String, String> alert) {
//        Log.d(TAG, "sending alert, patient Id: " + alert.get(ID));
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//        //        PendingIntent.FLAG_ONE_SHOT);
//        //Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.ic_stat_ic_notification)
//                //.setVibrate(new long[]{0, 1000, 1000, 1000})
//                .setContentTitle("ALERT")//alert.get(TITLE))
//                .setContentText("PATIENT ID: " + alert.get(ID)
//                        + "\nTime:" + alert.get(TS));
//                //.setSound(defaultSoundUri)
//                //.setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        notificationManager.notify(Integer.valueOf(alert.get(ID)) /* ID of notification */, notificationBuilder.build());
//    }

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



        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("ALERT:")
                        .setContentText("\nPATIENT ID: " + alert.get(ID) +
                                "\nTS: " + alert.get(TS) +
                                "\nSIGNAME: "+ alert.get(SIGNAME)+
                                "\nINTERVAL: "+alert.get(INTERVAL)+
                                "\nALERT_MSG: "+alert.get(ALERT_MSG)+
                                "\nACTION_MSG: "+alert.get(ACTION_MSG))

                                //The vibration pattern goes {Start delay, Vibration length, Sleep time, Vibration length}
                                //This pattern starts instantly, vibrates for 3 seconds, waits 1 second then vibrates for 3 seconds again.
                        .setVibrate(new long[]{0, 1000, 1000, 1000});


        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());


        // Create a big text style for the second page
//        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
//        secondPageStyle.setBigContentTitle("Patient History")
//                .bigText("\nExtra \nPatient \nHistory \nInformation");
//

        // Create second page notification
//        Notification secondPageNotification =
//                new NotificationCompat.Builder(this)
//                        .setStyle(secondPageStyle)
//                        .build();

        //Create a big Image type for the third page
//        Bitmap ekg = BitmapFactory.decodeResource(getResources(), R.mipmap.ekg_graph);
//        NotificationCompat.BigPictureStyle thirdPageStyle = new NotificationCompat.BigPictureStyle();
//        thirdPageStyle.bigPicture(ekg);

        //Create third page notification
        //The .extend line makes it so that it gets rid of the text box for the ekg graph page
//        Notification thirdPageNotification =
//                new NotificationCompat.Builder(this)
//                        .setStyle(thirdPageStyle)
//                        .extend(new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(true))
//                        .build();

        // Extend the notification builder with the created pages
//        Notification notification = notificationBuilder
//                .extend(new NotificationCompat.WearableExtender()
//                        .addPage(secondPageNotification)
//                        .addPage(thirdPageNotification))
//                .build();

        // Send the notification
        //notificationManager = NotificationManagerCompat.from(this);
        //notificationManager.notify(notificationId, notification);
       //NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

}

