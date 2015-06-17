package de.mvhs.android.zeiterfassung;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import de.mvhs.android.zeiterfassung.db.ZeitContract;
import de.mvhs.android.zeiterfassung.utils.ListViewBinder;

/**
 * Created by eugen on 14.06.15.
 */
public class RecordListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    // Klassenvaribalen
    private ListView _recordList = null;
    private SimpleCursorAdapter _adapter = null;
    private final static String[] _projection = {ZeitContract.ZeitDaten.Columns._ID, ZeitContract.ZeitDaten.Columns.START_TIME, ZeitContract.ZeitDaten.Columns.END_TIME};
    private final static String[] _columns = {ZeitContract.ZeitDaten.Columns.START_TIME, ZeitContract.ZeitDaten.Columns.END_TIME};
    private final static String _sortOrder = ZeitContract.ZeitDaten.Columns.START_TIME + " DESC";

    // Loader ID
    private final static int _LOADER_ID = 100;

    // API Interface
    public interface SelectionChangedListener {
        void onSelectionChanged(long id);
    }

    // Listener Instance
    private SelectionChangedListener _changeListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof SelectionChangedListener) {
            _changeListener = (SelectionChangedListener) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record_list, container, false);

        // Initalisierung der UI Elemente
        _recordList = (ListView) rootView.findViewById(R.id.RecordList);

        // Initialisierung des Adapters
        _adapter = new SimpleCursorAdapter(getActivity(), // Context
                R.layout.row_two_columns, // Layout
                null, // Cursor Daten
                _columns, // Spalten
                new int[]{android.R.id.text1, android.R.id.text2}, // Layout Views
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER // Flags
        );
        _adapter.setViewBinder(new ListViewBinder());
        // Liste mit Adapter verbinden
        _recordList.setAdapter(_adapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        registerForContextMenu(_recordList);
        _recordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (_changeListener != null) {
                    _changeListener.onSelectionChanged(id);
                }
            }
        });

        // Mit loader die Daten im Hintergrund laden
        getActivity().getSupportLoaderManager().restartLoader(_LOADER_ID, null, this);
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterForContextMenu(_recordList);
        _recordList.setOnItemClickListener(null);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.RecordList) {
            getActivity().getMenuInflater().inflate(R.menu.list_context_menu, menu);
        }

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                // Löschen des aktuellen Datensatzes
                final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                final long id = info.id;
                deleteRecord(id);

                return true;
        }

        return super.onContextItemSelected(item);
    }

    private void deleteRecord(final long id) {
        // Abfragedialog erstellen
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.button_delete)).setIcon(R.drawable.ic_menu_delete).setMessage(getString(R.string.delete_dialog_message)).setPositiveButton(R.string.button_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Löschlogik
                Uri uri = ContentUris.withAppendedId(ZeitContract.ZeitDaten.CONTENT_URI, id);
                getActivity().getContentResolver().delete(uri, null, null);
            }
        }).setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Abbrechen
                dialog.dismiss();
            }
        });

        // Dialog anzeigen
        builder.create().show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;

        switch (id) {
            case _LOADER_ID:
                loader = new CursorLoader(getActivity(), // Context
                        ZeitContract.ZeitDaten.CONTENT_URI, // URI für ContentProvider
                        _projection, // Spalten, die geladen werden sollen
                        null, // Filter
                        null, // Filter Parameter
                        _sortOrder);
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final int loaderId = loader.getId();

        switch (loaderId) {
            case _LOADER_ID:
                _adapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        final int loaderId = loader.getId();

        switch (loaderId) {
            case _LOADER_ID:
                _adapter.swapCursor(null);
                break;
        }
    }
}
