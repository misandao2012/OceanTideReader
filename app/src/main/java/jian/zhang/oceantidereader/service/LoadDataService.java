package jian.zhang.oceantidereader.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import jian.zhang.oceantidereader.constants.Constants;
import jian.zhang.oceantidereader.constants.IntentExtra;
import jian.zhang.oceantidereader.constants.Preference;
import jian.zhang.oceantidereader.domainobjects.Station;
import jian.zhang.oceantidereader.manager.StationManager;
import jian.zhang.oceantidereader.network.WebService;

/**
 * Created by jian on 12/15/2015.
 */
public class LoadDataService extends Service {

    private static final String TAG = "LoadDataService";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new GetStationsTask().execute();
        return START_NOT_STICKY;
    }

    private class GetStationsTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return WebService.getJson(Constants.OCEAN_CANDY_BASE_URL2);
        }

        @Override
        protected void onPostExecute(String jsonData) {
            super.onPostExecute(jsonData);
            new SetUpDatabaseTask().execute(jsonData);
        }
    }

    private class SetUpDatabaseTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... jsonData) {
            try {
                setupStationDatabase(jsonData[0]);
                return true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                setFirstTimeStartFalse();
                // The loading is finish, then send the broadcast to the activity
                sendFinishBroadcast();
            }
            // stop the service after data loaded
            stopSelf();
        }
    }

    private void setFirstTimeStartFalse() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(Preference.PREF_FIRST_TIME_START, false);
        editor.apply();
    }

    private void sendFinishBroadcast() {
        Intent loadIntent = new Intent(IntentExtra.FIRST_TIME_DATA_LOADED);
        LocalBroadcastManager.getInstance(LoadDataService.this).sendBroadcast(loadIntent);
    }

    private void setupStationDatabase(String jsonData)
            throws JSONException {
        // if the task is interrupted in the middle, the first time start is not set false yet,
        // so the database maybe set multiple times, so clean the database first
        StationManager.get(this).clearStations();
        JSONArray jStationArr = new JSONArray(jsonData);
        for (int i = 0; i < jStationArr.length(); i++) {
            JSONObject jStation = jStationArr.getJSONObject(i);
            Station station = new Station();
            station.setName(jStation.getString("name"));
            station.setStateName(jStation.getString("state_name"));
            station.setStationId(jStation.getString("id"));
            station.setFavorite(Constants.FAVORITE_FALSE);
            station.setId(StationManager.get(this).insertStation(station));
        }
    }
}
