package de.mvhs.android.zeiterfassung;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import de.mvhs.android.zeiterfassung.Intefaces.OnRecordChangedListener;
import de.mvhs.android.zeiterfassung.Intefaces.OnRecordSelectedListener;

public class AuflistungActivity extends SherlockFragmentActivity implements
		OnRecordSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent homeIntent = new Intent(this, MainActivity.class);
			startActivity(homeIntent);

			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void onRecordSelected(long id) {
		Fragment details = getSupportFragmentManager().findFragmentById(
				R.id.frag_detail);

		if (details != null && details.isAdded()
				&& details instanceof OnRecordChangedListener) {
			((OnRecordChangedListener) details).onRecordChanged(id, true);
		}
	}
}
