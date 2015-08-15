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

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.LruCache;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static edu.pdx.team_b_capstone2015.s_pi_watch.MainActivity.*;

public class PatientViewAdapter extends FragmentGridPagerAdapter {

    private static final int TRANSITION_DURATION_MILLIS = 100;

    private final Context mContext;
    private List<Row> mRows;
    private ColorDrawable mDefaultBg;

    private ColorDrawable mClearBg;

    public PatientViewAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        mContext = ctx;

        //list of rows for this adapter
        mRows = new ArrayList<PatientViewAdapter.Row>();
        for(int i = 0; i < MAX_PATIENTS ; i++){
            if(patientData.get(i)!= null){
                Map<String,String> p = patientData.get(i);

                mRows.add(new Row(
                        cardFragment(p.get(NAME),"Patient ID: "+p.get(P_ID)
                                        +"\nBed: "+p.get(BED)
                                        +"\nStatus: "+p.get(STATUS)),
                        cardFragment("Patient Vitals", "Heart Rate: "+p.get(HEART_RATE)
                                        +"\nBlood Pressure: "+p.get(BP)
                                        +"\nTemp: "+p.get(TEMP)),
                        //cardFragment("Alerts", "TO BE IMPLEMENTED LATER"),//need integration with alert notifications
                        cardFragment("Clinical Data", "Height: "+p.get(HEIGHT)
                                        +"\nWeight: "+p.get(WEIGHT)
                                        +"\nAllergies: "+p.get(ALLERGIES))
                        //add additional fragments to extend
                        //,new CustomFragment()
                        //,new CustomFragment2()
                        ));
            }
        }

        mRows.add(new Row(cardFragment(R.string.dismiss_title, R.string.dismiss_text)));
        mDefaultBg = new ColorDrawable(mContext.getResources().getColor(R.color.dark_grey));
        mClearBg = new ColorDrawable(mContext.getResources().getColor(android.R.color.transparent));
    }


    /** A convenient container for a row of fragments. */
    private class Row {
        final List<Fragment> columns = new ArrayList<Fragment>();

        public Row(Fragment... fragments) {
            for (Fragment f : fragments) {
                add(f);
            }
        }

        public void add(Fragment f) {
            columns.add(f);
        }

        Fragment getColumn(int i) {
            return columns.get(i);
        }

        public int getColumnCount() {
            return columns.size();
        }
    }

    //gets the correct fragment from a row and col
    public Fragment getFragment(int row, int col) {
        Row adapterRow = mRows.get(row);
        return adapterRow.getColumn(col);
    }

    @Override
    public Drawable getBackgroundForRow(final int row) {
        return mRowBackgrounds.get(row);
    }

    @Override
    public Drawable getBackgroundForPage(final int row, final int column) {
        //Special backgrounds for specific pages is currently not implemented
        return mPageBackgrounds.get(new Point(column, row));

    }

    @Override
    public int getRowCount() {
        return mRows.size();
    }

    @Override
    public int getColumnCount(int rowNum) {
        return mRows.get(rowNum).getColumnCount();
    }


    //array of background image ids.
    static final int[] BG_IMAGES = new int[] {
            R.drawable.background_row1,
            R.drawable.background_row2,
            R.drawable.background_row3,
            R.drawable.background_row4
    };
    //version of cardFragment with static text
    private Fragment cardFragment(int titleRes, int textRes) {
        Resources res = mContext.getResources();
        CardFragment fragment =
                CardFragment.create(res.getText(titleRes), res.getText(textRes));
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(
                res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }
    //version of CardFragment with dynamic text
    private Fragment cardFragment(String title, String text) {
        Resources res = mContext.getResources();
        CardFragment fragment =
                CardFragment.create(title, text);
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }
    //version of CardFragment with dynamic text, and no title
    private Fragment cardFragment(String text) {
        Resources res = mContext.getResources();
        CardFragment fragment =
                CardFragment.create("",text);
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }
    //
    private Fragment fragment(String text) {
        Resources res = mContext.getResources();
        Fragment fragment = new CustomFragment();
        Bundle args = fragment.getArguments();
        //set args here.
        fragment.setArguments(args);
        return fragment;
    }
    // Class for loading Backgrounds
    class DrawableLoadingTask extends AsyncTask<Integer, Void, Drawable> {
        private static final String TAG = "Loader";
        private Context context;

        DrawableLoadingTask(Context context) {
            this.context = context;
        }

        @Override
        protected Drawable doInBackground(Integer... params) {
            Log.d(TAG, "Loading asset 0x" + Integer.toHexString(params[0]));
            return context.getResources().getDrawable(params[0]);
        }
    }
    //Default background for each row.
    LruCache<Integer, Drawable> mRowBackgrounds = new LruCache<Integer, Drawable>(3) {
        @Override
        protected Drawable create(final Integer row) {
            int resid = BG_IMAGES[row % BG_IMAGES.length];
            new DrawableLoadingTask(mContext) {
                @Override
                protected void onPostExecute(Drawable result) {
                    TransitionDrawable background = new TransitionDrawable(new Drawable[] {
                            mDefaultBg,
                            result
                    });
                    mRowBackgrounds.put(row, background);
                    notifyRowBackgroundChanged(row);
                    background.startTransition(TRANSITION_DURATION_MILLIS);
                }
            }.execute(resid);
            return mDefaultBg;
        }
    };
    // Code below allows for a different Background for specific row/column
    LruCache<Point, Drawable> mPageBackgrounds = new LruCache<Point, Drawable>(3) {
        @Override
        protected Drawable create(final Point page) {
            // place a red background at row 3, column 2
//            if (page.y == 3 && page.x == 2) {
//                int resid = R.drawable.red;
//                new DrawableLoadingTask(mContext) {
//                    @Override
//                    protected void onPostExecute(Drawable result) {
//                        TransitionDrawable background = new TransitionDrawable(new Drawable[] {
//                                mClearBg,
//                                result
//                        });
//                        mPageBackgrounds.put(page, background);
//                        notifyPageBackgroundChanged(page.y, page.x);
//                        background.startTransition(TRANSITION_DURATION_MILLIS);
//                    }
//                }.execute(resid);
//            }
            return GridPagerAdapter.BACKGROUND_NONE;
        }
    };
}
