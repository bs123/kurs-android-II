package de.mvhs.android.zeiterfassung;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import de.mvhs.android.zeiterfassung.db.ZeitContract;
import de.mvhs.android.zeiterfassung.utils.Converter;

/**
 * Created by eugen on 14.06.15.
 */
public class EditRecordFragment extends Fragment implements DataChangedListener {
    public final static String ID_KEY = "DataID";
    public final static String IS_EDITABLE_KEY = "RecordIsEditable";
    private TextView _startDate;
    private TextView _startTime;
    private TextView _endDate;
    private TextView _endTime;
    private TextView _pause;
    private TextView _comment;

    // Rohdaten
    private Calendar _start = Calendar.getInstance();
    private Calendar _end = Calendar.getInstance();
    private long _id = -1;
    private boolean _isReadOnly = true;

    @Override
    public void onDataChanged(long id) {
        _id = id;
        initFromDatabase(id);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity().getIntent() != null) {
            _id = getActivity().getIntent().getLongExtra(ID_KEY, -1);
            _isReadOnly = !getActivity().getIntent().getBooleanExtra(IS_EDITABLE_KEY, false);
        }

        setHasOptionsMenu(!_isReadOnly);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView;
        if(_isReadOnly){
            rootView = inflater.inflate(R.layout.fragment_edit_record_readonly, container, false);
        } else {
            rootView = inflater.inflate(R.layout.fragment_edit_record, container, false);
        }

        // Init UI elements
        _startDate = (TextView) rootView.findViewById(R.id.StartDate);
        _startDate.setKeyListener(null);
        _startTime = (TextView) rootView.findViewById(R.id.StartTime);
        _startTime.setKeyListener(null);
        _endDate = (TextView) rootView.findViewById(R.id.EndDate);
        _endDate.setKeyListener(null);
        _endTime = (TextView) rootView.findViewById(R.id.EndTime);
        _endTime.setKeyListener(null);
        _pause = (TextView) rootView.findViewById(R.id.Pause);
        _comment = (TextView) rootView.findViewById(R.id.Comment);
        if(_isReadOnly){
            _pause.setKeyListener(null);
            _comment.setKeyListener(null);
        }


        if (_id > 0) {
            // Laden eines vorhandenen Datensatzes
            initFromDatabase(_id);
        }

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Registrierung der Listener
        if (!_isReadOnly) {
            _startDate.setOnLongClickListener(new EditDateHelper(getActivity(), _start, _startDate));
            _startTime.setOnLongClickListener(new EditTimeHelper(getActivity(), _start, _startTime));
            _endDate.setOnLongClickListener(new EditDateHelper(getActivity(), _end, _endDate));
            _endTime.setOnLongClickListener(new EditTimeHelper(getActivity(), _end, _endTime));
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Deregistrieren der Listener
        _startDate.setOnLongClickListener(null);
        _startTime.setOnLongClickListener(null);
        _endDate.setOnLongClickListener(null);
        _endTime.setOnLongClickListener(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_edit_record, menu);

        // Entfernen von Delete, wenn ein neuer Datensatz
        if (_id <= 0) {
            menu.removeItem(R.id.action_delete);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                saveRecord(_id);
                break;

            case R.id.action_cancel:
                getActivity().onBackPressed();
                break;

            case R.id.action_delete:
                deleteRecord(_id);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFromDatabase(long id) {
        if (!_isReadOnly) {
            getActivity().setTitle(getString(R.string.activity_title_edit));
        }

        Uri uri = ContentUris.withAppendedId(ZeitContract.ZeitDaten.CONTENT_URI, id);
        Cursor data = getActivity().getContentResolver().query(uri, null, null, null, null);

        if (data == null) {
            return;
        }

        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Startzeit
        int columnIndex = data.getColumnIndex(ZeitContract.ZeitDaten.Columns.START_TIME);
        String startValue = data.getString(columnIndex);
        try {
            Date startDate = ZeitContract.Converters.DB_DATE_TIME_FORMATTER.parse(startValue);

            _start.setTimeInMillis(startDate.getTime());

            _startDate.setText(Converter.toDateString(startDate));
            _startTime.setText(Converter.toTimeString(startDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Endzeit
        columnIndex = data.getColumnIndex(ZeitContract.ZeitDaten.Columns.END_TIME);
        String endValue = data.isNull(columnIndex) ? null : data.getString(columnIndex);
        if (endValue != null) {
            try {
                Date endDate = ZeitContract.Converters.DB_DATE_TIME_FORMATTER.parse(endValue);

                _end.setTimeInMillis(endDate.getTime());

                _endDate.setText(Converter.toDateString(endDate));
                _endTime.setText(Converter.toTimeString(endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            _endDate.setText("");
            _endTime.setText("");
        }

        // Pause
        columnIndex = data.getColumnIndex(ZeitContract.ZeitDaten.Columns.PAUSE);
        int pause = data.getInt(columnIndex);
        _pause.setText(String.valueOf(pause));

        // Kommentar
        columnIndex = data.getColumnIndex(ZeitContract.ZeitDaten.Columns.COMMENT);
        String comment = data.isNull(columnIndex) ? null : data.getString(columnIndex);
        _comment.setText(comment == null ? "" : comment);

        // Cursor schließen
        data.close();
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
                getActivity().onBackPressed();
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

    private void saveRecord(final long id) {
        // Prüfung, ob Werte vorhanden sind
        if (!hasValues()) {
            getActivity().onBackPressed();
            return;
        }

        // Pause auslesen
        String pauseString = _pause.getText().toString();
        int pauseValue = 0;
        if (isEmptyOrNull(pauseString)) {
            pauseValue = 0;
        } else {
            try {
                pauseValue = Integer.parseInt(pauseString);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                pauseValue = 0;
            }
        }

        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.START_TIME, ZeitContract.Converters.DB_DATE_TIME_FORMATTER.format(_start.getTime()));
        values.put(ZeitContract.ZeitDaten.Columns.END_TIME, ZeitContract.Converters.DB_DATE_TIME_FORMATTER.format(_end.getTime()));
        values.put(ZeitContract.ZeitDaten.Columns.PAUSE, pauseValue);
        values.put(ZeitContract.ZeitDaten.Columns.COMMENT, _comment.getText().toString());


        // Update, Aktualisierung des Eintrags
        if (_id > 0) {
            Uri updateUri = ContentUris.withAppendedId(ZeitContract.ZeitDaten.CONTENT_URI, id);
            getActivity().getContentResolver().update(updateUri, values, null, null);

        }
        // Insert, neuer Eintrag
        else {
            getActivity().getContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);
        }

        getActivity().onBackPressed();
    }

    private boolean hasValues() {
        if (isEmptyOrNull(_startDate.getText().toString())) {
            return false;
        }

        if (isEmptyOrNull(_startTime.getText().toString())) {
            return false;
        }

        if (isEmptyOrNull(_endDate.getText().toString())) {
            return false;
        }

        return !isEmptyOrNull(_endTime.getText().toString());

    }

    private boolean isEmptyOrNull(String value) {
        if (value == null) {
            return true;
        }

        if ("".equals(value)) {
            return true;
        }

        if (" ".equals(value)) {
            return true;
        }

        return false;
    }
}
