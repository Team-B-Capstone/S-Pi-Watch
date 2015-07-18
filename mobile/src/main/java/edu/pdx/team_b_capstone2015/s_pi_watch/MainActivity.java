package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
import android.widget.EditText;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;



public class MainActivity extends Activity {
    public final static String apiURL = "http://s-pi-demo.com/api/patients/";
    public final static String EXTRA_MESSAGE = "edu.pdx.team_b_capstone2015.s_pi_watch.MESSAGE";

    public void getPatients(View view) {
        //assign text field to var
        EditText editText = (EditText) findViewById(R.id.patient_id);
        //get a string from that text field
        String patient = editText.getText().toString();
        String urlString = apiURL + patient;
        new CallAPI().execute(urlString);

    }

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

    //parses the REST API response
    private class patientResults {
        public String statusNbr;
        public String hygieneResult;
    }

    //separate thread to call REST API
    private class CallAPI extends AsyncTask<String, String, String> {

        //Executes in a separate thread
        @Override
        protected String doInBackground(String... params) {
            String urlString=params[0]; // URL to call
            String resultToDisplay;
            InputStream in;
            // HTTP Get
            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                in = urlConnection.getInputStream();
                //in = new BufferedInputStream(urlConnection.getInputStream());

            } catch (Exception e ) {
                System.out.println(e.getMessage());
                return e.getMessage();
            }
            //parse the results here......
            resultToDisplay = convertStreamToString(in);

            return resultToDisplay.toString();
        }
        String convertStreamToString(java.io.InputStream is) {
            java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
        //executes in context of UI thread, executes after the API call has completed.
        protected void onPostExecute(String result) {
            //Create a intent that opens the results activity
            Intent intent = new Intent(getApplicationContext(), TestResultsActivity.class);
            //add the string to the intent.
            intent.putExtra(EXTRA_MESSAGE, result);
            //start the Activity
            startActivity(intent);
        }

    } // end CallAPI

//    //Function to parse an XML file (Don't need this
//    private patientResults parseXML( XmlPullParser parser ) throws XmlPullParserException, IOException {
//
//        int eventType = parser.getEventType();
//        patientResults result = new patientResults();
//
//        while( eventType!= XmlPullParser.END_DOCUMENT) {
//            String name = null;
//
//            switch(eventType)
//            {
//                case XmlPullParser.START_TAG:
//                    name = parser.getName();
//
//                    if( name.equals("Error")) {
//                        System.out.println("Web API Error!");
//                    }
//                    else if ( name.equals("StatusNbr")) {
//                        result.statusNbr = parser.nextText();
//                    }
//                    else if (name.equals("HygieneResult")) {
//                        result.hygieneResult = parser.nextText();
//                    }
//
//                    break;
//
//                case XmlPullParser.END_TAG:
//                    break;
//            } // end switch
//
//            eventType = parser.next();
//        } // end while
//
//        return result;
//    }
}
