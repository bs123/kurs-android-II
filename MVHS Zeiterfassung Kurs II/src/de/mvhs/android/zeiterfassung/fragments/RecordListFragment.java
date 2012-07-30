package de.mvhs.android.zeiterfassung.fragments;

import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import de.mvhs.android.zeiterfassung.AppPreferenceFragment;
import de.mvhs.android.zeiterfassung.CSVExporter;
import de.mvhs.android.zeiterfassung.OnRecordSelectedListener;
import de.mvhs.android.zeiterfassung.R;
import de.mvhs.android.zeiterfassung.RecordEditActivity;
import de.mvhs.android.zeiterfassung.db.WorkTimeContentProvider;
import de.mvhs.android.zeiterfassung.db.WorktimeCursorAdapter;
import de.mvhs.android.zeiterfassung.db.WorktimeTable;

public class RecordListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
  // Loader ID für die Liste
  private final static int         _LOADER         = 1;
  // Loader ID für den Export
  private final static int         _EXPORT_LOADER  = 2;
  // Loader für Send To Export
  private final static int         _SEND_TO_LOADER = 3;
  // Eigener Cursor
  private WorktimeCursorAdapter    _Adapter        = null;
  // Listener für Änderungen
  private OnRecordSelectedListener _RecordSelected = null;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.record_list_fragment, container);
  }

  @Override
  public void onStart() {
    super.onStart();
    // Registrieren des Context-Menüs für die Liste
    registerForContextMenu(getListView());

    // Laden der Daten
    loadData();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    if (activity instanceof OnRecordSelectedListener) {
      _RecordSelected = (OnRecordSelectedListener) activity;
    } else {
      throw new ClassCastException(activity.toString() + " muss OnRecordSelectedListener implementieren!");
    }
  }

  private void loadData() {
    // Adapter initialisieren
    _Adapter = new WorktimeCursorAdapter(getActivity(), null);

    // Adapter zuweisen
    setListAdapter(_Adapter);

    // Daten mit dem Loader laden
    getLoaderManager().restartLoader(_LOADER, null, this);
  }

  @Override
  public void onStop() {
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
      getActivity().getMenuInflater().inflate(R.menu.edit_delete, menu);
    }

    super.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {

    final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

    switch (item.getItemId()) {
      case R.id.ctx_delete:
        // Abfrage, ob wirklich gelöscht werden soll
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dlg_confirm_title).setMessage(R.string.dlg_confirm_message).setIcon(R.drawable.ic_menu_delete)
            .setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
              }
            }).setPositiveButton(R.string.dlg_delete, new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int which) {
                getActivity().getContentResolver().delete(WorkTimeContentProvider.CONTENT_URI_WORK_TIME, WorktimeTable.COLUMN_ID + "=?",
                    new String[] { String.valueOf(info.id) });
              }
            });

        builder.create().show();

        break;

      case R.id.ctx_edit:
        Intent editIntent = new Intent(getActivity(), RecordEditActivity.class);
        editIntent.putExtra(RecordEditFragment.ID_KEY, info.id);
        editIntent.putExtra(RecordEditFragment.READONLY_KEY, false);
        // Übergeben der ID an die neue Activity
        startActivity(editIntent);
        break;

      case R.id.ctx_export:
        // Exportieren der Daten
        getLoaderManager().restartLoader(_EXPORT_LOADER, null, this);
        break;

      case R.id.ctx_send_to:
        getLoaderManager().restartLoader(_SEND_TO_LOADER, null, this);
        break;

      case R.id.ctx_calendar:
        // Auslesen der Werte für den Kalender
        Cursor data = _Adapter.getCursor();
        if (data.moveToPosition(info.position)) {
          WorktimeTable table = new WorktimeTable(getActivity());
          Date startTime = table.getStartDate(info.id);
          Date endTime = table.getEndDate(info.id);
          // Ausgewählten Eintrag in den Kalender schreiben (API erst ab API14 Verfügbar)
          // Intent intentCalendar = new Intent(Intent.ACTION_INSERT) // Funktioniert nicht auf allen Handys
          Intent intentCalendar = new Intent(Intent.ACTION_EDIT)
          // Bestimmen des Intents
          // .setData(CalendarContract.Events.CONTENT_URI)
              .setType("vnd.android.cursor.item/event")
              // Startzeit
              .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime.getTime())
              // Endzeit
              .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTime())
              // Titel
              .putExtra(CalendarContract.Events.TITLE, "Arbeitszeiterfassung")
              // Beschreibung
              .putExtra(CalendarContract.Events.DESCRIPTION, "Erfasste Zeit")
              // Verfügbarkeit
              .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
          startActivity(intentCalendar);
        }
        break;

      default:
        break;
    }

    return super.onContextItemSelected(item);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    _RecordSelected.onRecordSelected(id);
  }

  public Loader<Cursor> onCreateLoader(int loaderId, Bundle extra) {
    CursorLoader returnValue = null;
    switch (loaderId) {
      case _LOADER:
      case _EXPORT_LOADER:
      case _SEND_TO_LOADER:
        returnValue = new CursorLoader(getActivity(), // Context
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
    String send_to = null;
    switch (loader.getId()) {
      case _LOADER:
        // Zuweisung der Daten an den Adapter
        _Adapter.swapCursor(data);
        break;

      case _SEND_TO_LOADER:
        send_to = AppPreferenceFragment.getPreferences(getActivity()).getString("send_to_email", "");
      case _EXPORT_LOADER:
        // Fortschrittsdialog erzeugen
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setTitle(R.string.dlg_export_title);
        dialog.setMessage(getString(R.string.dlg_export_message));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        final CSVExporter export = new CSVExporter(AppPreferenceFragment.getPreferences(getActivity()).getString("export_path", "export/mvhs")
            + "/export.csv", dialog, send_to, getActivity());

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
