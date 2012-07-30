package de.mvhs.android.zeiterfassung;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.mvhs.android.zeiterfassung.fragments.RecordEditFragment;

public class RecordListActivity extends SherlockFragmentActivity implements OnRecordSelectedListener {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.record_list);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Nutzen des App-Icons als Home-Button
    getSupportActionBar().setHomeButtonEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    // Zurückkehren zum Startbildschirm
      case android.R.id.home:
        Intent homeIntent = new Intent(this, MainActivity.class);
        this.startActivity(homeIntent);
        break;

      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRecordSelected(long id) {
    Fragment details = getSupportFragmentManager().findFragmentById(R.id.frag_edit);
    // Querformat und Details sind sichtbar
    if (details != null && details.isAdded() && details instanceof OnRecordChangedListener) {
      ((OnRecordChangedListener) details).onRecordChanged(id, true);
    }
    // Hochformat, ohne Detailansicht
    else {
      Intent editIntent = new Intent(this, RecordEditActivity.class);
      editIntent.putExtra(RecordEditFragment.ID_KEY, id);
      editIntent.putExtra(RecordEditFragment.READONLY_KEY, false);
      // Übergeben der ID an die neue Activity
      startActivity(editIntent);
    }
  }
}
