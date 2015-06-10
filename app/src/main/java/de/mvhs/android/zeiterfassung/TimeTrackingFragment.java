package de.mvhs.android.zeiterfassung;


import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import de.mvhs.android.zeiterfassung.db.ZeitContract;
import de.mvhs.android.zeiterfassung.utils.Converter;

/**
 * Created by kurs on 10.06.15.
 */
public class TimeTrackingFragment extends Fragment {
    // Klassenvariablen
    // UI Elemente
    private EditText _startTime = null;
    private EditText _endTime = null;
    private Button _startCommand = null;
    private Button _endCommand = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View rootView = inflater.inflate(R.layout.fragment_time_tracking, container, false);

        // Suchen der UI Elemente aus dem Layout
        _startTime = (EditText) rootView.findViewById(R.id.StartTime);
        _endTime = (EditText) rootView.findViewById(R.id.EndTime);
        _startCommand = (Button) rootView.findViewById(R.id.StartCommand);
        _endCommand = (Button) rootView.findViewById(R.id.EndCommand);

        // Deaktivieren der Bearbeitung in EditText-Feldern
        _startTime.setKeyListener(null);
        _endTime.setKeyListener(null);

        Log.d("Tag", "Meine Meldung");

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Registrieren der Listener (Events)
        _startCommand.setOnClickListener(new OnStartButtonClicked());
        _endCommand.setOnClickListener(new OnEndButtonClicked());

        // Ende Button nach dem Start deaktivieren
        _endCommand.setEnabled(false);

        // Daten aus der Datenbank lesen
        initData();
    }

    private void initData() {
        Cursor data = getActivity().getContentResolver().query(ZeitContract.ZeitDaten.EMPTY_CONTENT_URI, new String[]{ZeitContract.ZeitDaten.Columns.START_TIME}, null, null, null);

        // Offener Datensatz vorhanden
        if (data != null && data.moveToFirst()) {
            String startDateString = data.getString(0); // weil wir nur eine Spalte abfragen
            try {
                Date startDate = ZeitContract.Converters.DB_DATE_TIME_FORMATTER.parse(startDateString);

                _startTime.setText(Converter.toDateTimeString(startDate));
                _startCommand.setEnabled(false);
                _endCommand.setEnabled(true);
            } catch (ParseException e) {
                // Startdatum liegt im falschen Format vor.
                Toast.makeText(getActivity(), R.string.start_date_invalid_format, Toast.LENGTH_LONG).show();
            }
        }

        if (data != null) {
            data.close();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        // Deregistrieren der Listender (Events)
        _startCommand.setOnClickListener(null);
        _endCommand.setOnClickListener(null);
    }

    // Interne Klassen
    private class OnStartButtonClicked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // Deaktivieren des Buttons
            _startCommand.setEnabled(false);

            Calendar jetzt = Calendar.getInstance();

            ContentValues values = new ContentValues();
            values.put(ZeitContract.ZeitDaten.Columns.START_TIME, ZeitContract.Converters.DB_DATE_TIME_FORMATTER.format(jetzt.getTime()));

            getActivity().getContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);

            // Logik nach dem Klicken des Buttons
            _startTime.setText(Converter.toDateTimeString(jetzt.getTime()));

            // Aktivieren des Ende Buttons
            _endCommand.setEnabled(true);
        }
    }

    private class OnEndButtonClicked implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            // Dekativieren des Buttons
            _endCommand.setEnabled(false);

            Calendar jetzt = Calendar.getInstance();

            ContentValues values = new ContentValues();
            values.put(ZeitContract.ZeitDaten.Columns.END_TIME, ZeitContract.Converters.DB_DATE_TIME_FORMATTER.format(jetzt.getTime()));

            getActivity().getContentResolver().update(ZeitContract.ZeitDaten.EMPTY_CONTENT_URI, values, null, null);

            // Logik nach dem Klicken des Buttons
            _endTime.setText(Converter.toDateTimeString(jetzt.getTime()));

            // Aktivieren des Start Buttons
            _startCommand.setEnabled(true);
        }
    }
}
