package de.mvhs.android.zeiterfassung;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import de.mvhs.android.zeiterfassung.utils.CsvExporter;


public class RecordListActivity extends ActionBarActivity implements RecordListFragment.SelectionChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_list);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_record_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_export) {
            CsvExporter exporter = new CsvExporter(this);
            exporter.execute();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectionChanged(long id) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment detailFragment = manager.findFragmentById(R.id.DetailContent);

        if (detailFragment instanceof DataChangedListener && detailFragment.isAdded()) {
            // Querformat => Detailanzeige im Fragment
            ((DataChangedListener) detailFragment).onDataChanged(id);
        } else {
            // Hochformat => neu Activity für Bearbeitung
            Intent editIntent = new Intent(this, EditRecordActivity.class);
            editIntent.putExtra(EditRecordFragment.ID_KEY, id);
            editIntent.putExtra(EditRecordFragment.IS_EDITABLE_KEY, true);
            startActivity(editIntent);
        }
    }
}

















