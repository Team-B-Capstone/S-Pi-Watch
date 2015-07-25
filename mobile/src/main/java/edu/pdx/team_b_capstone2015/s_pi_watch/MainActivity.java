package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.app.Notification;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.Wearable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;



public class MainActivity extends Activity{
    private static final String TAG = "SPIMAIN";

    // Google Cloud Messaging helper objects:
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    protected void addNotification() {
        int notificationId = 001;

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("ALERT:")
                        .setContentText("\nPATIENT ID: \nROOM #:   \n[Extra Patient Information]")

                        //The vibration pattern goes {Start delay, Vibration length, Sleep time, Vibration length}
                        //This pattern starts instantly, vibrates for 3 seconds, waits 1 second then vibrates for 3 seconds again.
                        .setVibrate(new long[]{0, 3000, 1000, 3000});


        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());


        // Create a big text style for the second page
        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
        secondPageStyle.setBigContentTitle("Patient History")
                .bigText("\nExtra \nPatient \nHistory \nInformation");


        // Create second page notification
        Notification secondPageNotification =
                new NotificationCompat.Builder(this)
                        .setStyle(secondPageStyle)
                        .build();

        //Create a big Image type for the third page
        Bitmap ekg = BitmapFactory.decodeResource(getResources(), R.mipmap.ekg_graph);
        NotificationCompat.BigPictureStyle thirdPageStyle = new NotificationCompat.BigPictureStyle();
        thirdPageStyle.bigPicture(ekg);

        //Create third page notification
        //The .extend line makes it so that it gets rid of the text box for the ekg graph page
        Notification thirdPageNotification =
                new NotificationCompat.Builder(this)
                        .setStyle(thirdPageStyle)
                        .extend(new WearableExtender().setHintShowBackgroundOnly(true))
                        .build();

        // Extend the notification builder with the created pages
        Notification notification = notificationBuilder
                .extend(new NotificationCompat.WearableExtender()
                        .addPage(secondPageNotification)
                        .addPage(thirdPageNotification))
                .build();

        // Send the notification
        notificationManager =
                NotificationManagerCompat.from(this);
        notificationManager.notify(notificationId, notification);


    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Call the function that creates and sends the notifications
                addNotification();
            }
        });

        // register local device with google cloud server
        registerWithGC();
    }

    // registers the local device with google cloud server
    // allows backend to send messages (alerts!)
    protected void registerWithGC() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // TODO: I used to have an info display that would update when the GCM message was
                // sent, but it's not necessary at the moment
                // however, this abstract class has to be implemented, so I thought I'd keep the
                // skeleton code around in case was want it later (it is kind of nice!)
//                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//                boolean sentToken = sharedPreferences
//                        .getBoolean(GcmPreferences.SENT_TOKEN_TO_SERVER, false);
//                if (sentToken) {
//                    mInformationTextView.setText(getString(R.string.gcm_send_message));
//                } else {
//                    mInformationTextView.setText(getString(R.string.token_error_message));
//                }
            }
        };

        if (checkPlayServices()) {
            Log.i(TAG, "GCM: Initiating Intent: Registering with GCM");
            // Start IntentService to register this application with GCM.
            Intent GCMRegistration = new Intent(MainActivity.this, SpiRegistrationIntentService.class);
            startService(GCMRegistration);
        }
    }

    /**
     * Check the device to make sure this device has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
