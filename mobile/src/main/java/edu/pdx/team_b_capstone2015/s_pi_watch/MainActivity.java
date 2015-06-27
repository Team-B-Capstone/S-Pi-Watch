package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;
import android.app.Notification;
import android.view.View;
import android.widget.Button;


public class MainActivity extends ActionBarActivity {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
