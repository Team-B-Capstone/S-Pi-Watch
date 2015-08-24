package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import static edu.pdx.team_b_capstone2015.s_pi_watch.MainActivity.PATH_NOTIFICATION;


//listens for notification messages
public class NotificationListenerService extends WearableListenerService
        implements GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = "SPI-NotificationListSrv";
    private static final String ID = "PATIENT_ID";
    //private static final String TS = "TS";
    public static final String SIGNAME = "SIGNAME";
    public static final String INTERVAL = "INTERVAL";
    public static final String ALERT_MSG = "ALERT_MSG";
    public static final String ACTION_MSG = "ACTION_MSG";
    private static final String TITLE = "TITLE";
    private static final String NAME = "NAME";
    private static final String BED = "BED" ;
    public NotificationListenerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();



    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if(messageEvent.getPath().contentEquals(PATH_NOTIFICATION)){
            DataMap data = DataMap.fromByteArray(messageEvent.getData());
            sendNotification(data);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    private void sendNotification(DataMap alert) {

        // Create a pending intent that starts this wearable app
        Intent startIntent = new Intent(this, MainActivity.class).setAction(Intent.ACTION_MAIN);
        // Add extra data for app startup
        startIntent.putExtra("id",alert.getString(ID));
        PendingIntent startPendingIntent =
                PendingIntent.getActivity(this, 0, startIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        Log.d(TAG, "Sending notificaton for ID: " + alert.getString(ID));
        Integer notificationId = Integer.valueOf(alert.getString(ID));



        Notification secondPageNotification =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Alert Message")
                        .setContentText(alert.getString(ALERT_MSG))

                        .build();

        Notification thirdPageNotification =
                new NotificationCompat.Builder(this)
                        .setContentTitle("Action Message")
                        .setContentText(alert.getString(ACTION_MSG))
                        .setColor(Color.RED)
                        .build();

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setLocalOnly(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(alert.getString(TITLE))
                        .setContentText(alert.getString(NAME) +
                                "\nPatient ID: " + alert.getString(ID) +
                                "\nBed: " + alert.getString(BED) +
                                "\nAlert Type: " + alert.getString(SIGNAME) +
                                "\nLength: " + alert.getString(INTERVAL))
                        .extend(new NotificationCompat.WearableExtender()
                                        .addPage(secondPageNotification)
                                        .addPage(thirdPageNotification)
                                        .setBackground(BitmapFactory.decodeResource(getResources(), R.drawable.red))
                                        .setHintScreenTimeout(NotificationCompat.WearableExtender.SCREEN_TIMEOUT_LONG)

                        )
                        .setVibrate(new long[]{0, 1000, 1000, 1000})
                        .setPriority(Notification.PRIORITY_MAX)
                        .setVisibility(Notification.VISIBILITY_PUBLIC)
                        .setContentIntent(startPendingIntent);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());

    }
}
