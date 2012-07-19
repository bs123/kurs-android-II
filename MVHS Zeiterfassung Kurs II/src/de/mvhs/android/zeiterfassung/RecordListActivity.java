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
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import de.mvhs.android.zeiterfassung.db.WorkTimeContentProvider;
import de.mvhs.android.zeiterfassung.db.WorktimeCursorAdapter;
import de.mvhs.android.zeiterfassung.db.WorktimeTable;

public class RecordListActivity extends ListActivity implements LoaderCallbacks<Cursor> {
  // Loader ID für die Liste
  private final static int      _LOADER        = 1;
  // Loader ID für den Export
  private final static int      _EXPORT_LOADER = 2;
  // Eigener Cursor
  private WorktimeCursorAdapter _Adapter       = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.record_list);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Nutzen des App-Icons als Home-Button
    getActionBar().setHomeButtonEnabled(true);
    getActionBar().setDisplayHomeAsUpEnabled(true);
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
  protected void onStart() {
    super.onStart();
    // Registrieren des Context-Menüs für die Liste
    registerForContextMenu(getListView());

    // Laden der Daten
    loadData();
  }

  private void loadData() {
    // Adapter initialisieren
    _Adapter = new WorktimeCursorAdapter(this, null);

    // Adapter zuweisen
    setListAdapter(_Adapter);

    // Daten mit dem Loader laden
    getLoaderManager().restartLoader(_LOADER, null, this);
  }

  @Override
  protected void onStop() {
    // Datenbank sauber schließen
    setListAdapter(null);
    // Deregistrieren des Context-Menüs für die Liste
    unregisterForContextMenu(getListView());

    super.onStop();
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

    // Bindung des Context-Menüs an die Liste
    if (v.getId() == android.R.id.list) {
      this.getMenuInflater().inflate(R.menu.edit_delete, menu);
    }

    super.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {

    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

    switch (item.getItemId()) {
      case R.id.ctx_delete:
        // Abfrage, ob wirklich gelöscht werden soll
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.dlg_confirm_title).setMessage(R.string.dlg_confirm_message).setIcon(R.drawable.ic_menu_delete)
            .setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
              }
            }).setPositiveButton(R.string.dlg_delete, new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int which) {
                getContentResolver().delete(WorkTimeContentProvider.CONTENT_URI_WORK_TIME, WorktimeTable.COLUMN_ID + "=?",
                    new String[] { String.valueOf(info.id) });
              }
            });

        builder.create().show();

        break;
      case R.id.ctx_edit:
        Intent editIntent = new Intent(this, RecordEditActivity.class);
        editIntent.putExtra(RecordEditActivity.ID_KEY, info.id);
        // Übergeben der ID an die neue Activity
        startActivity(editIntent);

        break;

      case R.id.ctx_export:
        // Exportieren der Daten
        getLoaderManager().restartLoader(_EXPORT_LOADER, null, this);

        break;
      default:
        break;
    }

    return super.onContextItemSelected(item);
  }

  public Loader<Cursor> onCreateLoader(int loaderId, Bundle extra) {
    CursorLoader returnValue = null;
    switch (loaderId) {
      case _LOADER:
      case _EXPORT_LOADER:
        returnValue = new CursorLoader(this, // Context
            WorkTimeContentProvider.CONTENT_URI_WORK_TIME_SELECT, // ContentProvider
                                                                  // URI
            null, // Alle Spalten
            WorktimeTable.COLUMN_SELECT_START_DATE + " IS NOT NULL AND " + WorktimeTable.COLUMN_SELECT_END_DATE + "<> ''", // Bedingung,
                                                                                                                           // nur
                                                                                                                           // abgeschlossene
                                                                                                                           // Einträge
            null, // Parameter
            WorktimeTable.COLUMN_SELECT_START_DATE + " DESC," + WorktimeTable.COLUMN_SELECT_START_TIME + " DESC"); // Sortierung
        break;

      default:
        break;
    }

    return returnValue;
  }

  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    switch (loader.getId()) {
      case _LOADER:
        // Zuweisung der Daten an den Adapter
        _Adapter.swapCursor(data);
        break;

      case _EXPORT_LOADER:
        // Fortschrittsdialog erzeugen
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(R.string.dlg_export_title);
        dialog.setMessage(getString(R.string.dlg_export_message));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        final CSVExporter export = new CSVExporter(
            AppPreferenceFragment.getPreferences(this).getString("export_path", "export/mvhs") + "/export.csv", dialog);

        dialog.setCancelable(true);
        dialog.setButton(ProgressDialog.BUTTON_NEGATIVE, getString(R.string.dlg_cancel), new DialogInterface.OnClickListener() {

          public void onClick(DialogInterface dialog, int which) {
            export.cancel(true);
          }
        });

        // Export der Daten
        export.execute(new Cursor[] { data });
        break;

      default:
        break;
    }
  }

  public void onLoaderReset(Loader<Cursor> loader) {
    switch (loader.getId()) {
      case _LOADER:
        // zurücksetzen der Daten in Adapter
        _Adapter.swapCursor(null);
        break;

      default:
        break;
    }
  }
}
