package jian.zhang.oceantidereader.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import jian.zhang.oceantidereader.R;
import jian.zhang.oceantidereader.constants.Constants;
import jian.zhang.oceantidereader.constants.IntentExtra;
import jian.zhang.oceantidereader.domainobjects.Station;
import jian.zhang.oceantidereader.loader.StationsByStateLoader;
import jian.zhang.oceantidereader.ui.activity.StationDetailActivity;

/**
 * Created by jian on 12/14/2015.
 */
public class StationListFragment extends Fragment {
    /**
     * Whether or not the activity is in three-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mMultiplePane;
    private String mStateName;
    private RecyclerView mRecyclerView;
    private StationListLoaderCallbacks mStationListLoaderCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariables();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.station_list_fragment, container, false);
        setupStateNameTextView(rootView);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.station_list);
        assert mRecyclerView != null;
        initLoader();
        registerFavChangedReceiver();
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onFavoriteChanged);
    }

    private void initLoader() {
        mStationListLoaderCallbacks = new StationListLoaderCallbacks();
        getLoaderManager().initLoader(1, null, mStationListLoaderCallbacks);
    }

    /*
    * If the favorite status changed, then restart the loader and refresh the recyclerView
    * */
    private BroadcastReceiver onFavoriteChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // reload the data when the favorite status changed
            getLoaderManager().restartLoader(1, null, mStationListLoaderCallbacks);
        }
    };

    private void initVariables() {
        mStateName = getArguments().getString(IntentExtra.STATE_NAME);
        mMultiplePane = getArguments().getBoolean(IntentExtra.MULTIPLE_PANE);
    }

    private class StationListLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<Station>> {
        @Override
        public Loader<List<Station>> onCreateLoader(int id, Bundle bundle) {
            return new StationsByStateLoader(getActivity(), mStateName);
        }

        @Override
        public void onLoadFinished(Loader<List<Station>> loader, List<Station> stationList) {
            setupRecyclerViewAdapter(stationList);
        }

        @Override
        public void onLoaderReset(Loader<List<Station>> loader) {
            mRecyclerView.setAdapter(null);
        }
    }

    private void setupRecyclerViewAdapter(List<Station> stationList){
        StationAdapter adapter = new StationAdapter(stationList);
        // if there is no favorite stations yet
        if (stationList.size() == 0 && mStateName.equals(getString(R.string.favorite_stations))) {
            Toast.makeText(getActivity(), getActivity().getString(R.string.no_favorite_message), Toast.LENGTH_SHORT).show();
        }
        mRecyclerView.setAdapter(adapter);
    }

    private void registerFavChangedReceiver() {
        IntentFilter filter = new IntentFilter(IntentExtra.FAVORITE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onFavoriteChanged, filter);
    }

    private class StationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mStationNameTextView;
        private TextView mFavoriteLabelTextView;
        private View mItemView;

        public StationHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mStationNameTextView = (TextView) itemView.findViewById(R.id.tv_station_name);
            mFavoriteLabelTextView = (TextView) itemView.findViewById(R.id.tv_favorite_label);
            mItemView = itemView;
        }

        public void bindStation(Station station) {
            mStationNameTextView.setText(station.getName());
            if (station.getFavorite().equals(Constants.FAVORITE_TRUE)) {
                // If the station is favorite, then mark it as RED "FAV"
                mFavoriteLabelTextView.setVisibility(View.VISIBLE);
            } else {
                mFavoriteLabelTextView.setVisibility(View.INVISIBLE);
            }
            mItemView.setTag(station);
        }

        @Override
        public void onClick(View view) {
            Station station = (Station) mItemView.getTag();
            if (mMultiplePane) {
                // If multiple panes, then replace the fragment
                replaceStationDetailFragment(station);
            } else {
                // If single pane, then start a new activity
                startStationDetailActivity(station);
            }
        }
    }

    private void replaceStationDetailFragment(Station station) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(IntentExtra.STATION_PARCELABLE, station);
        bundle.putBoolean(IntentExtra.SHOW_STATION_SUBTITLE, true);
        StationDetailFragment fragment = new StationDetailFragment();
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.station_detail_container, fragment)
                .commit();
    }

    private void startStationDetailActivity(Station station) {
        Intent intent = new Intent(getActivity(), StationDetailActivity.class);
        intent.putExtra(IntentExtra.STATION_PARCELABLE, station);
        intent.putExtra(IntentExtra.SHOW_STATION_SUBTITLE, false);
        getActivity().startActivity(intent);
    }

    private class StationAdapter extends RecyclerView.Adapter<StationHolder> {
        private List<Station> mStations;

        public StationAdapter(List<Station> stations) {
            mStations = stations;
        }

        @Override
        public StationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.station_list_item, parent, false);
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

    private void setupStateNameTextView(View rootView) {
        TextView stateNameTextView = (TextView) rootView.findViewById(R.id.tv_state_name);
        // If it is multiple panes, do not show the station subtitle
        if (mMultiplePane) {
            stateNameTextView.setVisibility(View.VISIBLE);
            stateNameTextView.setText(mStateName);
        } else {
            stateNameTextView.setVisibility(View.INVISIBLE);
        }
    }
}
