package de.mvhs.android.zeiterfassung;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;

public class AuflistungActivity extends ActionBarActivity implements
		IRecordSelected {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();

			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRecordSelected(long selectedRecordId) {
		FragmentManager manager = getSupportFragmentManager();
		Fragment editFragment = manager
				.findFragmentById(R.id.RecordEditFragment);

		if (editFragment instanceof IRecordSelectedListener) {
			// Querformat => Daten an Fragment weiter reichen
			((IRecordSelectedListener) editFragment)
					.onRecordSelectionChanged(selectedRecordId);
		} else {
			// Hochformat => Intent absenden
			Intent editIntent = new Intent(this, EditActivity.class);
			editIntent.putExtra(EditFragment.ID_KEY, selectedRecordId);

			startActivity(editIntent);
		}
	}

}
