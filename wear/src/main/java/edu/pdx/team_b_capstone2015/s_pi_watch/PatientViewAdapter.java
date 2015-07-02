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

/**
 * Created by joel on 6/25/15.
 */
public class PatientViewAdapter extends FragmentGridPagerAdapter {
    //final Context mContext;

    //public PatientViewAdapter(final Context context, FragmentManager fm) {
    //    mContext = context.getApplicationContext();
    //}

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

        //additional columns are added by including additional cardFragments to the row.
        mRows.add(new Row(
                cardFragment("Mr. Patient 1","Male \nAdult \nBed #231 \n"),
                cardFragment("Patient Vitals","Temp: 98 \nHeart Rate: 90 \nBlood Pressure: 160/90 \n" ),
                cardFragment("Alerts", "NONE"),
                new CustomFragment(),
                new CustomFragment2()));
        mRows.add(new Row(
                cardFragment("Mr. Patient 2","Male \nAdult \nBed #232 \n"),
                cardFragment("Patient Vitals","Temp: 98 \nHeart Rate: 90 \nBlood Pressure: 160/90 \n" ),
                cardFragment("Alerts", "NONE"),
                new CustomFragment()));
        mRows.add(new Row(
                cardFragment("Ms. Patient 3","Female \nAdult \nBed #233 \n"),
                cardFragment("Patient Vitals","Temp: 98 \nHeart Rate: 90 \nBlood Pressure: 160/90 \n" ),
                cardFragment("Alerts", "NONE"),
                new CustomFragment()));
        mRows.add(new Row(
                cardFragment("Mr. Patient 4","Male \nAdult \nBed #665 \n"),
                cardFragment("Patient Vitals","Temp: 70 \nHeart Rate: 0 \nBlood Pressure: 0/0 \n" ),
                cardFragment("Alerts", "CODE: BLUE"),
                new CustomFragment()));

        mRows.add(new Row(cardFragment(R.string.dismiss_title, R.string.dismiss_text)));

        mDefaultBg = new ColorDrawable(R.color.dark_grey);
        mClearBg = new ColorDrawable(android.R.color.transparent);
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
        fragment.setCardMarginBottom(
                res.getDimensionPixelSize(R.dimen.card_margin_bottom));
        return fragment;
    }
    //version of CardFragment with dynamic text, and no title
    private Fragment cardFragment(String text) {
        Resources res = mContext.getResources();
        CardFragment fragment =
                CardFragment.create("",text);
        // Add some extra bottom margin to leave room for the page indicator
        fragment.setCardMarginBottom(
                res.getDimensionPixelSize(R.dimen.card_margin_bottom));
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
    // Background for specific row/column
    LruCache<Point, Drawable> mPageBackgrounds = new LruCache<Point, Drawable>(3) {
        @Override
        protected Drawable create(final Point page) {
            // place bugdroid as the background at row 3, column 2
            if (page.y == 3 && page.x == 2) {
                int resid = R.drawable.red;
                new DrawableLoadingTask(mContext) {
                    @Override
                    protected void onPostExecute(Drawable result) {
                        TransitionDrawable background = new TransitionDrawable(new Drawable[] {
                                mClearBg,
                                result
                        });
                        mPageBackgrounds.put(page, background);
                        notifyPageBackgroundChanged(page.y, page.x);
                        background.startTransition(TRANSITION_DURATION_MILLIS);
                    }
                }.execute(resid);
            }
            return GridPagerAdapter.BACKGROUND_NONE;
        }
    };
}
