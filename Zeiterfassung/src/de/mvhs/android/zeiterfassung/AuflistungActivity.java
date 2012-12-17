package de.mvhs.android.zeiterfassung;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import de.mvhs.android.zeiterfassung.db.ZeitContentProvider;
import de.mvhs.android.zeiterfassung.db.ZeitTabelle;

public class AuflistungActivity extends ListActivity implements
		LoaderCallbacks<Cursor> {

	// Loader
	private final static int _DATA_LOADER = 1;
	private final static int _EXPORT_LOADER = 2;
	private final static int _SEND_TO_LOADER = 3;
	private SimpleCursorAdapter _Adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_list);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// getActionBar().setDisplayShowHomeEnabled(true);
		// getActionBar().setDisplayHomeAsUpEnabled(true);

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

	@Override
	protected void onStart() {
		super.onStart();

		// Adapter initialisieren
		_Adapter = new SimpleCursorAdapter(this, // Context
				R.layout.list_row2, // Layout für die Zeile
				null, // Daten
				new String[] { ZeitTabelle.STARTZEIT, ZeitTabelle.ENDZEIT }, // Spalten
				new int[] { R.id.start, R.id.ende }, // IDs der Views, in die
														// die
				// Daten der Spalten
				// platziert werden
				0); // Flag

		setListAdapter(_Adapter);

		// Loader initialisieren
		getLoaderManager().restartLoader(_DATA_LOADER, null, this);

		registerForContextMenu(getListView());
	}

	@Override
	protected void onStop() {
		unregisterForContextMenu(getListView());

		super.onStop();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == android.R.id.list) {
			this.getMenuInflater().inflate(R.menu.list_context, menu);
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// Liste nach dem Eintrag fragen
		final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case R.id.ctx_delete:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			// Dialog mit Daten befüllen (erstellen)
			builder.setTitle(R.string.title_delete)
					.setMessage(R.string.delete_confirmation)
					.setPositiveButton(R.string.cmd_yes,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									getContentResolver()
											.delete(Uri
													.withAppendedPath(
															ZeitContentProvider.CONTENT_URI,
															String.valueOf(info.id)),
													null, null);
									getLoaderManager().restartLoader(
											_DATA_LOADER, null,
											AuflistungActivity.this);
								}
							})
					.setNegativeButton(R.string.cmd_no,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									dialog.cancel();
								}
							});

			// Dialog aufrufen
			builder.create().show();

			break;
		case R.id.ctx_edit:
			Intent editIntent = new Intent(this, EditActivity.class);
			editIntent.putExtra(EditActivity.KEY_ID, info.id);
			startActivity(editIntent);

			break;
		case R.id.ctx_export:
			getLoaderManager().restartLoader(_EXPORT_LOADER, null, this);

			break;
		case R.id.ctx_export_mail:
			getLoaderManager().restartLoader(_SEND_TO_LOADER, null, this);

			break;

		default:
			break;
		}
		return true;
	}

	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		CursorLoader returnValue = null;

		switch (id) {
		case _DATA_LOADER:
		case _EXPORT_LOADER:
		case _SEND_TO_LOADER:
			returnValue = new CursorLoader(this,
					ZeitContentProvider.CONTENT_URI, null, null, null,
					ZeitTabelle.STARTZEIT + " DESC");
			break;

		default:
			break;
		}

		return returnValue;
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		String sendTo = null;

		switch (loader.getId()) {
		case _DATA_LOADER:
			// Übergeben der Daten an den Adapter
			_Adapter.swapCursor(data);

			break;

		case _SEND_TO_LOADER:
			sendTo = EinstellungenActivity.getSendToAddress(this);
		case _EXPORT_LOADER:
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setTitle(R.string.title_export);
			dialog.setMessage(getString(R.string.export_message));
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

			final CSVExporter exporter = new CSVExporter("export", dialog,
					sendTo, this);

			dialog.setCancelable(true);
			dialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
					getString(R.string.cmd_cancel),
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							exporter.cancel(true);
						}
					});
			exporter.execute(new Cursor[] { data });
			break;

		default:
			break;
		}
	}

	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case _DATA_LOADER:
			// Zurücksetzen der Daten im Adapter
			_Adapter.swapCursor(null);
			break;

		default:
			break;
		}
	}
}
