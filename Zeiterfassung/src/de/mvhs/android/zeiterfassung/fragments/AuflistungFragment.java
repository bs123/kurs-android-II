package de.mvhs.android.zeiterfassung.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.actionbarsherlock.app.SherlockListFragment;

import de.mvhs.android.zeiterfassung.CSVExporter;
import de.mvhs.android.zeiterfassung.EditActivity;
import de.mvhs.android.zeiterfassung.EinstellungenActivity;
import de.mvhs.android.zeiterfassung.R;
import de.mvhs.android.zeiterfassung.db.ZeitContentProvider;
import de.mvhs.android.zeiterfassung.db.ZeitTabelle;

public class AuflistungFragment extends SherlockListFragment implements
		LoaderCallbacks<Cursor> {

	// Loader
	private final static int _DATA_LOADER = 1;
	private final static int _EXPORT_LOADER = 2;
	private final static int _SEND_TO_LOADER = 3;
	private SimpleCursorAdapter _Adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_list, container);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Adapter initialisieren
		_Adapter = new SimpleCursorAdapter(this.getActivity(), // Context
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
	public void onStop() {
		unregisterForContextMenu(getListView());

		super.onStop();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == android.R.id.list) {
			this.getActivity().getMenuInflater()
					.inflate(R.menu.list_context, menu);
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
			AlertDialog.Builder builder = new AlertDialog.Builder(
					this.getActivity());
			// Dialog mit Daten befüllen (erstellen)
			builder.setTitle(R.string.title_delete)
					.setMessage(R.string.delete_confirmation)
					.setPositiveButton(R.string.cmd_yes,
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {
									AuflistungFragment.this
											.getActivity()
											.getContentResolver()
											.delete(Uri
													.withAppendedPath(
															ZeitContentProvider.CONTENT_URI,
															String.valueOf(info.id)),
													null, null);
									getLoaderManager().restartLoader(
											_DATA_LOADER, null,
											AuflistungFragment.this);
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
			Intent editIntent = new Intent(this.getActivity(),
					EditActivity.class);
			editIntent.putExtra(EditFragment.KEY_ID, info.id);
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
			returnValue = new CursorLoader(this.getActivity(),
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
			sendTo = EinstellungenActivity.getSendToAddress(this.getActivity());
		case _EXPORT_LOADER:
			ProgressDialog dialog = new ProgressDialog(this.getActivity());
			dialog.setTitle(R.string.title_export);
			dialog.setMessage(getString(R.string.export_message));
			dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

			final CSVExporter exporter = new CSVExporter("export", dialog,
					sendTo, this.getActivity());

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
