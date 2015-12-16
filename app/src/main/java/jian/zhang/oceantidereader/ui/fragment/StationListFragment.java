package jian.zhang.oceantidereader.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import jian.zhang.oceantidereader.R;
import jian.zhang.oceantidereader.manager.StationManager;
import jian.zhang.oceantidereader.constants.Constants;
import jian.zhang.oceantidereader.constants.IntentExtra;
import jian.zhang.oceantidereader.domainobjects.Station;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariables();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.station_list_fragment, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.station_list);
        assert mRecyclerView != null;
        setupRecyclerView(mRecyclerView, mStateName);
        registerFavChangedReceiver();

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onFavoriteChanged);
    }

    /*
    * If the favorite status changed, then refresh the recyclerView
    * */
    private BroadcastReceiver onFavoriteChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setupRecyclerView(mRecyclerView, mStateName);
        }
    };

    private void initVariables() {
        mStateName = getArguments().getString(IntentExtra.STATE_NAME);
        mMultiplePane = getArguments().getBoolean(IntentExtra.MULTIPLE_PANE);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, String stateName) {

        List<Station> stations;
        if (stateName.equals(getString(R.string.favorite_stations))) {
            stations = StationManager.get(getActivity()).getStationsByFav();
            // Did not add any favorites yet
            if (stations.size() == 0) {
                Toast.makeText(getActivity(), getString(R.string.no_favorite_message), Toast.LENGTH_SHORT).show();
            }
        } else {
            stations = StationManager.get(getActivity()).getStationsByState(stateName);
        }
        StationAdapter adapter = new StationAdapter(stations);
        recyclerView.setAdapter(adapter);
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
                mFavoriteLabelTextView.setVisibility(View.VISIBLE);
            } else {
                mFavoriteLabelTextView.setVisibility(View.INVISIBLE);
            }
            mItemView.setTag(station);
        }

        @Override
        public void onClick(View view) {
            Station station = (Station) mItemView.getTag();
            String stationId = station.getStationId();
            String stationName = station.getName();
            if (mMultiplePane) {
                replaceStationDetailFragment(stationId, stationName);
            } else {
                startStationDetailActivity(stationId, stationName);
            }
        }
    }

    private void replaceStationDetailFragment(String stationId, String stationName) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.STATION_ID, stationId);
        bundle.putString(IntentExtra.STATION_NAME, stationName);
        bundle.putBoolean(IntentExtra.SHOW_STATION_SUBTITLE, true);
        StationDetailFragment fragment = new StationDetailFragment();
        fragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.station_detail_container, fragment)
                .commit();
    }

    private void startStationDetailActivity(String stationId, String stationName) {
        Intent intent = new Intent(getActivity(), StationDetailActivity.class);
        intent.putExtra(IntentExtra.STATION_ID, stationId);
        intent.putExtra(IntentExtra.STATION_NAME, stationName);
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
}
