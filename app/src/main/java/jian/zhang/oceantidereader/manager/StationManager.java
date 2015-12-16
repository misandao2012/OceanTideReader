package jian.zhang.oceantidereader.manager;

import android.content.Context;

import java.util.List;

import jian.zhang.oceantidereader.database.StationDatabaseHelper;
import jian.zhang.oceantidereader.domainobjects.Station;

/**
 * Created by jian on 12/14/2015.
 */

/* Singleton Station Manager to wrap up database functions
* */
public class StationManager {
    private static StationManager sStationManager;

    private StationDatabaseHelper mDatabaseHelper;

    public static StationManager get(Context context) {
        if (sStationManager == null) {
            sStationManager = new StationManager(context);
        }
        return sStationManager;
    }

    private StationManager(Context context) {
        mDatabaseHelper = new StationDatabaseHelper(context);
    }

    public List<Station> getStationsGroupByState() {
        return mDatabaseHelper.getStationsFromCursor(mDatabaseHelper.queryStationsGroupByState());
    }

    public List<Station> getStationsByState(String stateName) {
        return mDatabaseHelper.getStationsFromCursor(mDatabaseHelper.queryStationsByState(stateName));
    }

    public List<Station> getStationsByFav() {
        return mDatabaseHelper.getStationsFromCursor(mDatabaseHelper.queryStationsByFav());
    }

    public void clearStations(){
        mDatabaseHelper.deleteAllData();
    }

    public long insertStation(Station station){
        return mDatabaseHelper.insertStation(station);
    }

    public Station getStationById(String id){
        return mDatabaseHelper.getStationById(id);
    }

    public boolean updateCardByStation(Station station){
        return mDatabaseHelper.updateCardByStation(station);
    }
}
