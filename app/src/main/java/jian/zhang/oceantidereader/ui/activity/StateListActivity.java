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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import jian.zhang.oceantidereader.R;
import jian.zhang.oceantidereader.constants.Constants;
import jian.zhang.oceantidereader.constants.IntentExtra;
import jian.zhang.oceantidereader.constants.Preference;
import jian.zhang.oceantidereader.domainobjects.Station;
import jian.zhang.oceantidereader.manager.StationManager;
import jian.zhang.oceantidereader.network.WebService;
import jian.zhang.oceantidereader.service.LoadDataService;
import jian.zhang.oceantidereader.ui.fragment.StationListFragment;

public class StateListActivity extends AppCompatActivity {

    private RecyclerView mStateRecyclerView;
    private ProgressBar mProgressBar;
    private boolean mMultiplePane;
    private ActionBar mActionBar;
    private String mCurrentStateName;
    private ProgressDialog mProgressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.state_list_activity);

        initVariables(savedInstanceState);
        initViews();
        registerDataLoadedReceiver();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //check if it is the first time install the App
        if (sharedPrefs.getBoolean(Preference.PREF_FIRST_TIME_START, true)) {
            setupProgressDialog();
            startLoadingDataService();
        } else {
            updateUI();
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onDataLoaded);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        // keep the current state name after rotating
        bundle.putString(Constants.CURRENT_STATE_NAME, mCurrentStateName);
    }

    private void updateUI() {
        List<Station> stations = StationManager.get(this).getStationsGroupByState();
        StationAdapter adapter = new StationAdapter(stations);
        mStateRecyclerView.setAdapter(adapter);
    }

    private void initViews() {
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        setupToolbar();
        setupFloatingButton();
        initRecyclerView();
    }

    private void initVariables(Bundle savedInstanceState) {
        // if it is the tablet with landscape orientation, it will have three panes
        if (findViewById(R.id.station_list_container) != null) {
            mMultiplePane = true;
        }
        if (savedInstanceState != null) {
            mCurrentStateName = savedInstanceState.getString(Constants.CURRENT_STATE_NAME);
        }
    }

    private void setupProgressDialog(){
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressDialog = ProgressDialog.show(this, "", getString(R.string.loading_message), true, true);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    private BroadcastReceiver onDataLoaded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // when the data loading is finished, it will receive this broadcast,
            // then dismiss the progress views and update the UI
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            mProgressBar.setVisibility(View.GONE);
            updateUI();
        }
    };

    private void startLoadingDataService() {
        if (WebService.networkConnected(this)) {
            // Put the loading task in a service because it need a long time,
            // don't want to be interrupted by the UI
            Intent intent = new Intent(StateListActivity.this, LoadDataService.class);
            startService(intent);
        } else {
            WebService.showNetworkDialog(this);
        }
    }

    private void initRecyclerView() {
        mStateRecyclerView = (RecyclerView) findViewById(R.id.state_list);
        mStateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupFloatingButton() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Goes to the station list view but only show all the favorite stations
                mCurrentStateName = getString(R.string.favorite_stations);
                onButtonClicked(mCurrentStateName);
                Toast.makeText(StateListActivity.this, getString(R.string.got_favorite_stations), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            if (mCurrentStateName != null && mMultiplePane) {
                // If on a tablet device with landscape orientation, the toolbar will show the current state name
                mActionBar.setTitle(mCurrentStateName);
            } else {
                mActionBar.setTitle(getString(R.string.state_list));
            }
        }
    }

    private void registerDataLoadedReceiver() {
        IntentFilter filter = new IntentFilter(IntentExtra.FIRST_TIME_DATA_LOADED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onDataLoaded, filter);
    }

    private class StationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mStateNameTextView;
        private View mItemView;

        public StationHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mStateNameTextView = (TextView) itemView;
            mItemView = itemView;
        }

        public void bindStation(Station station) {
            mStateNameTextView.setText(station.getStateName());
            mItemView.setTag(station.getStateName());
        }

        @Override
        public void onClick(View v) {
            mCurrentStateName = (String) mItemView.getTag();
            onButtonClicked(mCurrentStateName);
        }
    }

    private void onButtonClicked(String name) {
        if (mMultiplePane) {
            if (mActionBar != null) {
                mActionBar.setTitle(name);
            }
            // It is multiple panes, so replace the fragment, otherwise will start a new activity
            replaceStationListFragment(name);
        } else {
            startStationListActivity(name);
        }
    }

    private void replaceStationListFragment(String name) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.STATE_NAME, name);
        bundle.putBoolean(IntentExtra.MULTIPLE_PANE, mMultiplePane);
        StationListFragment fragment = new StationListFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.station_list_container, fragment)
                .commit();
    }

    private void startStationListActivity(String name) {
        Intent intent = new Intent(StateListActivity.this, StationListActivity.class);
        intent.putExtra(IntentExtra.STATE_NAME, name);
        startActivity(intent);
    }

    private class StationAdapter extends RecyclerView.Adapter<StationHolder> {
        private List<Station> mStations;

        public StationAdapter(List<Station> stations) {
            mStations = stations;
        }

        @Override
        public StationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(StateListActivity.this);
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new StationHolder(view);
        }

        @Override
        public void onBindViewHolder(StationHolder holder, int position) {
            Station station = mStations.get(position);
            holder.bindStation(station);
        }

        @Override
        public int getItemCount() {
            return mStations.size();
        }
    }
}
