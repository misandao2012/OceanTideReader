package jian.zhang.oceantidereader.ui.fragment;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jian.zhang.oceantidereader.R;
import jian.zhang.oceantidereader.constants.Constants;
import jian.zhang.oceantidereader.constants.IntentExtra;
import jian.zhang.oceantidereader.domainobjects.Station;
import jian.zhang.oceantidereader.domainobjects.Tide;
import jian.zhang.oceantidereader.manager.StationManager;
import jian.zhang.oceantidereader.network.WebService;
import jian.zhang.oceantidereader.utils.Utils;

public class StationDetailFragment extends Fragment {

    private static final String TAG = "OceanTide";

    private ProgressBar mProgressBar;
    private RecyclerView mTideRecyclerView;
    private FloatingActionButton mFab;
    private Station mStation;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public StationDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVariables();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.station_detail_fragment, container, false);

        initViews(rootView);
        setupStationNameTextView(rootView);
        setupFavCheckBoxFeature(rootView);
        startStationDetailTask();
        return rootView;
    }

    private void initVariables(){
        mStation = getArguments().getParcelable(IntentExtra.STATION_PARCELABLE);
    }

    private void initViews(View rootView){
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        mTideRecyclerView = (RecyclerView) rootView.findViewById(R.id.list_tide);
        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
    }

    private void startStationDetailTask(){
        if (WebService.networkConnected(getActivity())) {
            new GetStationDetailTask().execute();
        } else {
            WebService.showNetworkDialog(getActivity());
        }
    }

    private void setupStationNameTextView(View rootView) {
        TextView stationNameTextView = (TextView) rootView.findViewById(R.id.tv_station_name);
        // If it is multiple panes, do not show the station subtitle
        if (getArguments().getBoolean(IntentExtra.SHOW_STATION_SUBTITLE)) {
            stationNameTextView.setVisibility(View.VISIBLE);
            stationNameTextView.setText(mStation.getName());
        } else {
            stationNameTextView.setVisibility(View.INVISIBLE);
        }
    }

    // get the tide info list for the low or high
    private List<Tide> getTideList(String jsonData, String lowOrHigh) throws JSONException {
        List<Tide> tideList = new ArrayList<>();
        JSONObject jTideObj = new JSONObject(jsonData);
        JSONArray jTideArr = jTideObj.getJSONArray(lowOrHigh);

        for (int i = 0; i < jTideArr.length(); i++) {
            JSONObject jTide = jTideArr.getJSONObject(i);
            Tide tide = new Tide();
            tide.setTime(jTide.getString("time"));
            tide.setFeet(jTide.getString("feet"));
            tide.setLowOrHigh(lowOrHigh);
            tideList.add(tide);
        }
        return tideList;
    }


    // Call the api with station Id to get the station detail information
    private class GetStationDetailTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... params) {
            return WebService.getJson(Constants.OCEAN_CANDY_BASE_URL + "/" + mStation.getStationId());
        }

        @Override
        protected void onPostExecute(final String jsonData) {
            super.onPostExecute(jsonData);
            mProgressBar.setVisibility(View.GONE);
            List<Tide> tideList = setupTideList(jsonData);
            setupTideList(tideList);
            setupShareFeature(tideList);
        }
    }

    private List<Tide> setupTideList(String jsonData){
        List<Tide> tideList = new ArrayList<>();
        try {
            tideList.addAll(getTideList(jsonData, "Low"));
            tideList.addAll(getTideList(jsonData, "High"));
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return tideList;
    }

    // If the favorite check box is checked, the favorite feature will be updated to the database
    private void setupFavCheckBoxFeature(View rootView) {
        CheckBox favoriteCheck = (CheckBox) rootView.findViewById(R.id.favorite_check);
        initFavCheckBox(favoriteCheck);
        favoriteCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFavCheckClicked(view);
            }
        });
    }

    private void initFavCheckBox(CheckBox favoriteCheck) {
        String ifFavorite = mStation.getFavorite();
        if (ifFavorite != null && ifFavorite.equals(Constants.FAVORITE_TRUE)) {
            favoriteCheck.setChecked(true);
        } else {
            favoriteCheck.setChecked(false);
        }
    }

    private void onFavCheckClicked(View view) {
        if (((CheckBox) view).isChecked()) {
            mStation.setFavorite(Constants.FAVORITE_TRUE);
        } else {
            mStation.setFavorite(Constants.FAVORITE_FALSE);
        }
        // The favorite status changed, then send the broadcast
        sendFavChangedBroadcast();
    }

    private void sendFavChangedBroadcast() {
        Intent intent = new Intent(IntentExtra.FAVORITE_CHANGED);
        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
        StationManager.get(getActivity()).updateCardByStation(mStation);
    }

    private void setupShareFeature(final List<Tide> tides) {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareStationTideInformation(tides, mStation);
            }
        });
    }

    /*
    * Share the tide information to other apps
    * */
    private void shareStationTideInformation(List<Tide> tides, Station station) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "I got " + tides.size() + " Tide Information from " + station.getName());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_tide_infomation)));
    }

    private void setupTideList(List<Tide> tideList) {
        mTideRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        TideAdapter adapter = new TideAdapter(tideList);
        mTideRecyclerView.setAdapter(adapter);
    }

    private class TideHolder extends RecyclerView.ViewHolder {

        private TextView mTimeTextView;
        private TextView mFeetTextView;
        private TextView mLowOrHighTextView;

        public TideHolder(View itemView) {
            super(itemView);
            mTimeTextView = (TextView) itemView.findViewById(R.id.tv_time);
            mFeetTextView = (TextView) itemView.findViewById(R.id.tv_feet);
            mLowOrHighTextView = (TextView) itemView.findViewById(R.id.tv_lowOrHigh);
        }

        public void bindTide(Tide tide) {
            mTimeTextView.setText(Utils.parseTideTime(tide.getTime()));
            mFeetTextView.setText(tide.getFeet());
            // Indicate the tide is low or high
            mLowOrHighTextView.setText(getString(R.string.low_or_high, tide.getLowOrHigh()));
        }
    }

    private class TideAdapter extends RecyclerView.Adapter<TideHolder> {
        private List<Tide> mTides;

        public TideAdapter(List<Tide> tides) {
            mTides = tides;
        }

        @Override
        public TideHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.station_detail_item, parent, false);
            return new TideHolder(view);
        }

        @Override
        public void onBindViewHolder(TideHolder holder, int position) {
            Tide tide = mTides.get(position);
            holder.bindTide(tide);
        }

        @Override
        public int getItemCount() {
            return mTides.size();
        }
    }
}
