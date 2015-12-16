package jian.zhang.oceantidereader.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import jian.zhang.oceantidereader.R;
import jian.zhang.oceantidereader.constants.IntentExtra;
import jian.zhang.oceantidereader.ui.fragment.StationListFragment;

public class StationListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_list_activity);

        String stateName = getIntent().getStringExtra(IntentExtra.STATE_NAME);
        setupToolbar(stateName);

        if (savedInstanceState == null) {
            startStationListFragment(stateName);
        }
    }

    private void setupToolbar(String stateName) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(stateName);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void startStationListFragment(String stateName) {
        Bundle bundle = new Bundle();
        bundle.putString(IntentExtra.STATE_NAME, stateName);
        bundle.putBoolean(IntentExtra.MULTIPLE_PANE, getIntent().getBooleanExtra(IntentExtra.MULTIPLE_PANE, false));
        StationListFragment fragment = new StationListFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.station_list_container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, StateListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
