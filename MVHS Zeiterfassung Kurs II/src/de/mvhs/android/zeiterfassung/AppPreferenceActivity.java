package de.mvhs.android.zeiterfassung;

import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class AppPreferenceActivity extends SherlockPreferenceActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.pref_fragment);
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
}
