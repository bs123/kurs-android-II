package de.mvhs.android.zeiterfassung;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import de.mvhs.android.zeiterfassung.db.ZeitContracts;
import de.mvhs.android.zeiterfassung.db.ZeitContracts.Zeit;

public class AuflistungFragment extends ListFragment implements LoaderCallbacks<Cursor> {

  private final static int    _LIST_LOADER_ID = 1;
  private SimpleCursorAdapter _Adapter        = null;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    return inflater.inflate(R.layout.fragment_list, container, false);
  }

  @Override
  public void onStart() {
    super.onStart();

    // Laden der Daten
    // -- Einen Adapter initialisieren
    _Adapter = new SimpleCursorAdapter(getActivity(), R.layout.row_list, null, new String[] { Zeit.Columns.START, Zeit.Columns.END }, new int[] { R.id.Text1,
        R.id.Text2 }, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

    // Loader starten
    getLoaderManager().initLoader(_LIST_LOADER_ID, null, this);

    // Zuordnung des Adapters zur Liste
    getListView().setAdapter(_Adapter);

    registerForContextMenu(getListView());

    // Registrierung für den Listener
    getListView().setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (getActivity() instanceof IRecordSelected) {
          ((IRecordSelected) getActivity()).onRecordSelected(id);
        }
      }
    });
  }

  @Override
  public void onStop() {
    unregisterForContextMenu(getListView());
    super.onStop();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.menu_export, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.mnu_export:
        // Laden der Daten aus der Datenbank
        Cursor exportData = getActivity().getContentResolver().query(ZeitContracts.Zeit.CONTENT_URI, null, null, null, null);

        // Exporter initialisieren
        CsvAsyncTaskExporter exporter = new CsvAsyncTaskExporter(getActivity());

        // Export starten
        exporter.execute(exportData);
        break;

      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    if (v.getId() == android.R.id.list) {
      getActivity().getMenuInflater().inflate(R.menu.list_edit_menu, menu);
    }

    super.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    // Bestimmen des ausgewählten Eintrages
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

    switch (item.getItemId()) {
      case R.id.mnu_edit:
        // Intent editIntent = new Intent(getActivity(), EditActivity.class);
        // // ID an das Intent mit übergeben
        Intent editIntent = new Intent(getActivity(), EditActivity.class);
        editIntent.putExtra(EditFragment.ID_KEY, info.id);

        startActivity(editIntent);

        return true;

      case R.id.mnu_delete:
        delete(info.id);

        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  private void delete(final long id) {

    // Aufbau eines Dialoges
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Löschen ...") // Titel des Dialoges setzen
            .setMessage("Wollen Sie den Datensatz wirklich löschen?") // Nachricht
            // für
            // den
            // Benutzer
            .setIcon(R.drawable.ic_menu_delete) // Icopn für das Dialog
            .setPositiveButton("Löschen", new OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                Uri deleteUri = ContentUris.withAppendedId(ZeitContracts.Zeit.CONTENT_URI, id);
                getActivity().getContentResolver().delete(deleteUri, null, null);
              }
            }) // Button für die positive
               // Antwort
            .setNegativeButton("Abbrechen", null); // Button zum Abbrechen
    // der Aktion

    // Dialog anzeigen
    builder.create().show();
  }

  @Override
  public Loader<Cursor> onCreateLoader(int loaderId, Bundle extras) {
    CursorLoader loader = null;

    switch (loaderId) {
      case _LIST_LOADER_ID:
        loader = new CursorLoader(getActivity(), ZeitContracts.Zeit.CONTENT_URI, null, null, null, ZeitContracts.Zeit.Columns.START + " DESC");
        break;

      default:
        break;
    }
    return loader;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    switch (loader.getId()) {
      case _LIST_LOADER_ID:
        data.setNotificationUri(getActivity().getContentResolver(), ZeitContracts.Zeit.CONTENT_URI);
        _Adapter.swapCursor(data);
        break;

      default:
        break;
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    switch (loader.getId()) {
      case _LIST_LOADER_ID:
        _Adapter.swapCursor(null);
        break;

      default:
        break;
    }
  }
}
