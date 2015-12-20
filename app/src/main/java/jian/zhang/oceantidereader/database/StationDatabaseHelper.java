package jian.zhang.oceantidereader.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import jian.zhang.oceantidereader.constants.Constants;
import jian.zhang.oceantidereader.domainobjects.Station;

public class StationDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "stations.sqlite";
    private static final int VERSION = 1;
    private static final String TABLE_STATION = "station";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_STATE_NAME = "state_name";
    private static final String COLUMN_STATION_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_FAVORITE = "favorite";

    public StationDatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_STATION + " (_id integer primary key autoincrement, "
                + COLUMN_STATE_NAME + " varchar(256), "
                + COLUMN_STATION_ID + " integer, "
                + COLUMN_FAVORITE + " varchar(256), "
                + COLUMN_NAME + " varchar(256))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    //clear the all the data from database
    public void deleteAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_STATION, null, null);
    }

    public long insertStation(Station station) {
        ContentValues contentValue = new ContentValues();
        contentValue.put(COLUMN_STATE_NAME, station.getStateName());
        contentValue.put(COLUMN_STATION_ID, station.getStationId());
        contentValue.put(COLUMN_NAME, station.getName());
        contentValue.put(COLUMN_FAVORITE, station.getFavorite());
        return getWritableDatabase().insert(TABLE_STATION, null, contentValue);
    }

    public List<Station> getStationsFromCursor(StationCursor cursor) {
        List<Station> stations = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            stations.add(cursor.getStation());
            cursor.moveToNext();
        }
        cursor.close();
        return stations;
    }

    // Get the stations grouped by state name
    public StationCursor queryStationsGroupByState() {
        Cursor wrapped = getReadableDatabase().query(TABLE_STATION,
                null, null, null, COLUMN_STATE_NAME, null, null);
        return new StationCursor(wrapped);
    }

    // Get the stations with the specified state name
    public StationCursor queryStationsByState(String stateName) {
        Cursor wrapped = getReadableDatabase().query(TABLE_STATION,
                null, COLUMN_STATE_NAME + " = ?",
                new String[]{stateName}, null, null, null);
        return new StationCursor(wrapped);
    }

    // Get all the favorite stations
    public StationCursor queryStationsByFav() {
        Cursor wrapped = getReadableDatabase().query(TABLE_STATION,
                null, COLUMN_FAVORITE + " = ?",
                new String[]{Constants.FAVORITE_TRUE}, null, null, null);
        return new StationCursor(wrapped);
    }

    public boolean updateCardByStation(Station station) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_FAVORITE, station.getFavorite());
        return getWritableDatabase().update(TABLE_STATION, values, COLUMN_STATION_ID + " =?"
                , new String[]{station.getStationId()}) > 0;
    }

    public static class StationCursor extends CursorWrapper {

        public StationCursor(Cursor c) {
            super(c);
        }

        public Station getStation() {
            if (isBeforeFirst() || isAfterLast())
                return null;
            Station station = new Station();
            station.setId(getLong(getColumnIndex(COLUMN_ID)));
            station.setStateName(getString(getColumnIndex(COLUMN_STATE_NAME)));
            station.setStationId(getString(getColumnIndex(COLUMN_STATION_ID)));
            station.setName(getString(getColumnIndex(COLUMN_NAME)));
            station.setFavorite(getString(getColumnIndex(COLUMN_FAVORITE)));
            return station;
        }
    }
}
