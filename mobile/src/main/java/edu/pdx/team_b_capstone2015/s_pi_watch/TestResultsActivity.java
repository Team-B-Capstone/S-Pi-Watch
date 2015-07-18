package edu.pdx.team_b_capstone2015.s_pi_watch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

//Class to test the REST API
public class TestResultsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        //retrieve the string for constant EXTRA_MESSAGE
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        //Create JSON object
        JSONObject jObject, patient;
        String fname;
        String lname;
        String id;
        String roomNum;
        String output = "";
        try {
            jObject = new JSONObject(message);
            //if there is a id present then we have multiple patients selected.
            if(jObject.has("1")) {
                for (int i = 1; i < 5; i++) {
                    patient = new JSONObject(jObject.getString((new Integer(i)).toString()));
                    fname = patient.getString("firstName");
                    lname = patient.getString("lastName");
                    id = patient.getString("id");
                    roomNum = patient.getString("roomNum");
                    output += "Patient ID: " + id + "\n" + fname + " " + lname + " \nRoom Number " + roomNum + "\n";
                }
            }else{
                fname = jObject.getString("firstName");
                lname = jObject.getString("lastName");
                id = jObject.getString("id");
                roomNum = jObject.getString("roomNum");
                output = "Patient ID: "+id + "\n" + fname + " " + lname + " \nRoom Number " + roomNum +"\n";
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }

        //create an new textView
        TextView textView = new TextView(this);
        textView.setTextSize(24);
        textView.setText(output);
        //set the content view to the textView
        setContentView(textView);
        //setContentView(R.layout.activity_test_results);
    }
}
