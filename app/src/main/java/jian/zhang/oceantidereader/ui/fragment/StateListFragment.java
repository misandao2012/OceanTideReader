package jian.zhang.oceantidereader.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import jian.zhang.oceantidereader.R;
import jian.zhang.oceantidereader.constants.IntentExtra;
import jian.zhang.oceantidereader.constants.Preference;
import jian.zhang.oceantidereader.domainobjects.Station;
import jian.zhang.oceantidereader.manager.StationManager;
import jian.zhang.oceantidereader.network.WebService;
import jian.zhang.oceantidereader.service.LoadDataService;
import jian.zhang.oceantidereader.ui.activity.StateListActivity;
import jian.zhang.oceantidereader.ui.activity.StationListActivity;
import jian.zhang.oceantidereader.utils.Utils;

/**
 * Created by jian on 12/16/2015.
 */
public class StateListFragment extends Fragment implements StateListActivity.Callback {

    private RecyclerView mStateRecyclerView;
    private boolean mMultiplePane;
    private Context mContext;

    // implement callback function to open the favorite stations view
    @Override
    public void onFavButtonClicked() {
        onButtonClicked(getString(R.string.favorite_stations));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        setupCallback();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        recycleCallback();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.state_list_fragment, container, false);
        initRecyclerView(rootView);
        registerDataLoadedReceiver();
        initActions();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onDataLoaded);
    }

    private void initActions(){
        //check if it is the first time install the App
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (sharedPrefs.getBoolean(Preference.PREF_FIRST_TIME_START, true)) {
            Utils.lockOrientationPortrait(getActivity());
            startLoadingDataService();
        } else {
            loadData();
        }
    }

    private void setupCallback(){
        if (mContext instanceof StateListActivity) {
            ((StateListActivity) mContext).setCallback(this);
        }
    }

    private void recycleCallback(){
        if (mContext != null && mContext instanceof StateListActivity) {
            ((StateListActivity) mContext).setCallback(null);
        }
    }

    private void updateUI(List<Station> stations) {
        StationAdapter adapter = new StationAdapter(stations);
        mStateRecyclerView.setAdapter(adapter);
        Utils.unlockOrientation(getActivity());
    }

    private BroadcastReceiver onDataLoaded = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // when the data loading is finished, it will receive this broadcast,
            // then dismiss the progress views and update the UI
            loadData();
        }
    };

    private void loadData() {
        new LoadDatabaseTask().execute();
    }

    // Load the data use a task
    private class LoadDatabaseTask extends AsyncTask<Void, Void, List<Station>> {

        @Override
        protected void onPreExecute() {
            if (mContext instanceof StateListActivity) {
                ((StateListActivity) mContext).getProgressBar().setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<Station> doInBackground(Void... params) {
            return StationManager.get(getActivity()).getStationsGroupByState();
        }

        @Override
        protected void onPostExecute(List<Station> stations) {
            super.onPostExecute(stations);
            if (mContext instanceof StateListActivity) {
                ((StateListActivity) mContext).getProgressBar().setVisibility(View.GONE);
            }
            updateUI(stations);
        }
    }

    private void startLoadingDataService() {
        if (WebService.networkConnected(getActivity())) {
            // Put the loading task in a service because it need a long time,
            // don't want to be interrupted by the UI
            Intent intent = new Intent(getActivity(), LoadDataService.class);
            getActivity().startService(intent);
        } else {
            WebService.showNetworkDialog(getActivity());
        }
    }

    private void initRecyclerView(View rootView) {
        mStateRecyclerView = (RecyclerView) rootView.findViewById(R.id.state_list);
        mStateRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void registerDataLoadedReceiver() {
        IntentFilter filter = new IntentFilter(IntentExtra.FIRST_TIME_DATA_LOADED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onDataLoaded, filter);
    }

    private void onButtonClicked(String name) {
        if (mContext!=null && mContext instanceof StateListActivity) {
            mMultiplePane = ((StateListActivity) mContext).getMultiplePane();
        }
        if (mMultiplePane) {
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
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.station_list_container, fragment)
                .commit();
    }

    private void startStationListActivity(String name) {
        Intent intent = new Intent(getActivity(), StationListActivity.class);
        intent.putExtra(IntentExtra.STATE_NAME, name);
        startActivity(intent);
    }

    private class StateHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mStateNameTextView;
        private View mItemView;

        public StateHolder(View itemView) {
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
            String stateName = (String) mItemView.getTag();
            onButtonClicked(stateName);
        }
    }

    private class StationAdapter extends RecyclerView.Adapter<StateHolder> {
        private List<Station> mStations;

        public StationAdapter(List<Station> stations) {
            mStations = stations;
        }

        @Override
        public StateHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new StateHolder(view);
        }

        @Override
        public void onBindViewHolder(StateHolder holder, int position) {
            Station station = mStations.get(position);
            holder.bindStation(station);
        }

        @Override
        public int getItemCount() {
            return mStations.size();
        }
    }
}
