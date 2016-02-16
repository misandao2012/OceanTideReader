package jian.zhang.oceanwithlibrary.loader;

import android.content.Context;

import java.util.List;

import jian.zhang.oceanwithlibrary.R;
import jian.zhang.oceanwithlibrary.domainobjects.Station;
import jian.zhang.oceanwithlibrary.manager.StationManager;

/**
 * Created by jian on 12/19/2015.
 */
public class StationsByStateLoader extends DataLoader<List<Station>> {

    private String mStateName;

    public StationsByStateLoader(Context context, String stateName) {
        super(context);
        mStateName = stateName;
    }

    @Override
    public List<Station> loadInBackground() {
        if (mStateName.equals(getContext().getString(R.string.favorite_stations))) {
            return StationManager.get(getContext()).getStationsByFav();
        }
        return StationManager.get(getContext()).getStationsByState(mStateName);
    }
}
