package jian.zhang.oceantidereader.ui.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import jian.zhang.oceantidereader.R;
import jian.zhang.oceantidereader.constants.IntentExtra;
import jian.zhang.oceantidereader.domainobjects.Station;
import jian.zhang.oceantidereader.ui.fragment.StationDetailFragment;

/**
 * An activity representing a single Item detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link StationListActivity}.
 */
public class StationDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_detail_activity);
        Station station = getIntent().getParcelableExtra(IntentExtra.STATION_PARCELABLE);
        setupToolbar(station);

        if (savedInstanceState == null) {
            addStationDetailFragment(station);
        }
    }

    private void setupToolbar(Station station){
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(station.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void addStationDetailFragment(Station station){
        Bundle bundle = new Bundle();
        bundle.putParcelable(IntentExtra.STATION_PARCELABLE, station);
        bundle.putBoolean(IntentExtra.SHOW_STATION_SUBTITLE, getIntent().getBooleanExtra(IntentExtra.SHOW_STATION_SUBTITLE, false));
        StationDetailFragment fragment = new StationDetailFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.station_detail_container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
