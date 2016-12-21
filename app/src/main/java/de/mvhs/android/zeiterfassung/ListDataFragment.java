package de.mvhs.android.zeiterfassung;

import android.Manifest;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.DateFormat;
import java.util.zip.Inflater;

import de.mvhs.android.zeiterfassung.db.TimeDataContract;

/**
 * Created by Kurs on 26.10.2016.
 */

public class ListDataFragment extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {
  public static final int _EXPORT_REQUEST_CODE = 200;
  private ListView _dataList;
  private SimpleCursorAdapter _adapter;

  private final static int _LOADER = 100;
  private final static String[] _VIEW_COLUMNS = new String[]{
      TimeDataContract.TimeData.Columns.START,
      TimeDataContract.TimeData.Columns.END
  };
  private final static String[] _QUERY_COLUMNS = new String[]{
      BaseColumns._ID,
      TimeDataContract.TimeData.Columns.START,
      TimeDataContract.TimeData.Columns.END
  };
  private final static int[] _LAYOUT_VIEW_IDS = new int[]{
      R.id.StartTimeValue,
      R.id.EndTimeValue
  };
  private final static String _FILTER =
      "IFNULL(" + TimeDataContract.TimeData.Columns.END + ",'')<>''";
  private final static String _ORDER =
      TimeDataContract.TimeData.Columns.START + " DESC";

  private DateFormat _UI_DATE_TIME_FORMATTER = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_list_data, container, false);

    _dataList = (ListView) rootView.findViewById(R.id.DataList);
    return super.onCreateView(inflater, container, savedInstanceState);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
   // setContentView(R.layout.fragment_list_data);
      setHasOptionsMenu(true);

 //   _dataList = (ListView) getActivity().findViewById(R.id.DataList);

    // Adapter initialisieren
    /*_adapter = new SimpleCursorAdapter(
        this, // Context
        R.layout.row_data_item, // Layout für die Zeile
        null, // Daten
        _VIEW_COLUMNS, // Spalten aus den Daten
        _LAYOUT_VIEW_IDS, // IDs der Views im Layout
        SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER // Benachrichtigung
    );

    // Verarbeitung der Datum und Uhrzeit Werte
    _adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
      @Override
      public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if ((view instanceof TextView) == false) {
          return false;
        }

        TextView textView = (TextView) view;

        // NULL Prüfung aus der Datenbank
        if (cursor.isNull(columnIndex)) {
          textView.setText("---");
        } else {
          try {
            // Konvertierung aus der Datenbank
            String dateString = cursor.getString(columnIndex);
            Calendar date = TimeDataContract.Converter.parseFromDb(dateString);

            textView.setText(_UI_DATE_TIME_FORMATTER.format(date.getTime()));
          } catch (ParseException e) {
            textView.setText("PARSE ERROR");
          }
        }

        return true;
      }
    });

    // Adpter zuordnen
    _dataList.setAdapter(_adapter);*/
  }

  @Override
  public void onStart() {
    super.onStart();
    getActivity().getSupportLoaderManager()
        .restartLoader(
            _LOADER, // ID des Loaders
            null, // Zusatzdaten, falls notwendig
            this); // Context

    _dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent editIntent = new Intent(getActivity(), EditActivity.class);
        editIntent.putExtra(EditFragment.ID_KEY, _adapter.getItemId(position));
        startActivity(editIntent);
      }
    });

    registerForContextMenu(_dataList);
  }

  @Override
  public void onStop() {
    super.onStop();
    getActivity().getSupportLoaderManager()
        .destroyLoader(_LOADER);

    _dataList.setOnItemClickListener(null);

    unregisterForContextMenu(_dataList);
  }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_list, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.MenuExport:
        // Abfragen, ob Berechtigung vorhanden ist
        if (ContextCompat.checkSelfPermission(
            getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
          // Berechtigung erfragen
          ActivityCompat.requestPermissions(
              getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              _EXPORT_REQUEST_CODE);
        } else {
          // Exportieren erlaubt
          CsvExporter exporter = new CsvExporter(getActivity());
          exporter.execute();
        }
        return true;

      case R.id.MenuAddNew:
        Intent addIntent = new Intent(getActivity(), EditActivity.class);
        startActivity(addIntent);
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    if (v.getId() == R.id.DataList) {
      getActivity().getMenuInflater().inflate(R.menu.menu_context_list, menu);
    }
    super.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    final AdapterView.AdapterContextMenuInfo info =
        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

    switch (item.getItemId()) {
      case R.id.MenuItemDelete:
        // Löschen-Logik
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
            .setTitle(R.string.DeleteDialogTitle)
            .setMessage(R.string.DeleteDialogMessage)
            .setNegativeButton(R.string.DialogCancelButton, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
              }
            })
            .setPositiveButton(R.string.DialogDeleteButton, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                // Jetzt in der Datenbank löschen
                long id = info.id;
                Uri deleteUri =
                    ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, id);

                getActivity().getContentResolver().delete(deleteUri, null, null);
              }
            });

        builder.create().show();

        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    switch (requestCode) {
      case _EXPORT_REQUEST_CODE:
        // Antwort des Benutzers verarbeiten
        if (grantResults.length == 1
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          CsvExporter exporter = new CsvExporter(getActivity());
          exporter.execute();
        }
        break;
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    CursorLoader loader = null;

    switch (id) {
      case _LOADER:
        loader = new CursorLoader(
            getActivity(), // Context
            TimeDataContract.TimeData.CONTENT_URI, // URI zu Daten
            _QUERY_COLUMNS, // Spalten
            _FILTER, // Filer (WHERE)
            null, // Filter Parameter
            _ORDER // Sortierung (ORDER BY)
        );
        break;
    }

    return loader;
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    switch (loader.getId()) {
      case _LOADER:
        _adapter.swapCursor(data);
        break;
    }
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    switch (loader.getId()) {
      case _LOADER:
        _adapter.swapCursor(null);
        break;
    }
  }
}
