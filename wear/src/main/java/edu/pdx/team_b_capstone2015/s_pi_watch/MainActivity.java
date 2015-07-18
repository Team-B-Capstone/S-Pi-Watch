package edu.pdx.team_b_capstone2015.s_pi_watch;
import android.content.res.Resources;
import android.support.wearable.view.DotsPageIndicator;
import android.support.wearable.view.GridViewPager;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.view.WindowInsets;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView mTextView;

    //called when the activity is first created. We do the initial set up of the activity here. This would include creating views and binding data to lists.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final Resources res = getResources();
        final GridViewPager pager = (GridViewPager) findViewById(R.id.pager);

        //sets the adapter for the pager
        pager.setAdapter(new PatientViewAdapter(this, getFragmentManager()));
        DotsPageIndicator dotsPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
        dotsPageIndicator.setPager(pager);
    }
    //called when the activity is becoming visible to the user. onStart() is followed by onResume()
    @Override
    protected void onStart() {
        super.onStart();
    }

    //method is always called when the activity is being placed in the background or is about to be destroyed. This is where we can save our persistent data
    @Override
    protected void onPause() {
        super.onPause();
    }

    //method is called when the activity starts interacting with the user, always followed by onResume
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
        super.onDestroy();
    }
}
