package jian.zhang.oceantidereader.ui.activity;

/**
 * Created by jian on 12/14/2015.
 */

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import jian.zhang.oceantidereader.R;
import jian.zhang.oceantidereader.constants.IntentExtra;
import jian.zhang.oceantidereader.constants.Preference;
import jian.zhang.oceantidereader.ui.fragment.StateListFragment;

public class StateListActivity extends AppCompatActivity{

    private ProgressBar mProgressBar;
    private boolean mMultiplePane;
    private ProgressDialog mProgressDialog;
    private Callback mCallback;

    public ProgressBar getProgressBar(){
        return mProgressBar;
    }

    public boolean getMultiplePane(){
        return mMultiplePane;
    }

    public interface Callback {
        void onFavButtonClicked();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.state_list_activity);
        initVariables();
        registerDataLoadedReceiver();
        initViews();
        initActions();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onDataLoaded);
    }

    private void initActions(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPrefs.getBoolean(Preference.PREF_FIRST_TIME_START, true)) {
            setupProgressDialogs();
        } else {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void registerDataLoadedReceiver() {
        IntentFilter filter = new IntentFilter(IntentExtra.FIRST_TIME_DATA_LOADED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onDataLoaded, filter);
    }

    private BroadcastReceiver onDataLoaded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // when the data loading is finished, it will receive this broadcast,
            // then dismiss the progress views
            dismissProgressDialogs();
        }
    };

    private void onButtonClicked() {
        if (mCallback != null) {
            mCallback.onFavButtonClicked();
        }
    }

    private void startStateListFragment() {
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.state_list_container);
        if (fragment == null) {
            fragment = new StateListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.state_list_container, fragment)
                    .commit();
        }
    }

    private void initViews() {
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        setupToolbar();
        setupFloatingButton();
        startStateListFragment();
    }

    private void initVariables() {
        // If it is the tablet with landscape orientation, it will have three panes
        if (findViewById(R.id.station_list_container) != null) {
            mMultiplePane = true;
        }
    }

    private void setupProgressDialogs() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressDialog = ProgressDialog.show(this, "", getString(R.string.loading_message), true, true);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    private void dismissProgressDialogs(){
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private void setupFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Go to the station list view but only show all the favorite stations
                onButtonClicked();
                Toast.makeText(StateListActivity.this, getString(R.string.got_favorite_stations), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.app_title));
        }
    }
}
