package de.mvhs.android.zeiterfassung;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import de.mvhs.android.zeiterfassung.db.TimeDataContract;

/**
 * Created by eugen on 06.11.16.
 */

public class EditActivity extends AppCompatActivity {
    public final static String ID_KEY = "EditItemId";
    private long _id = -1;

    // UI Felder
    private EditText _startDate;
    private EditText _startTime;
    private EditText _endDate;
    private EditText _endTime;
    private EditText _pause;
    private EditText _comment;

    // Speicher
    private Calendar _startDateTimeValue;
    private Calendar _endDateTimeValue;

    // Formatter
    private DateFormat _dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
    private DateFormat _timeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_grid);

        // Auslesen der ID aus Metainformationen
        _id = getIntent().getLongExtra(ID_KEY, -1);

        // Initialisieren der UI Elemente
        _startDate = (EditText) findViewById(R.id.StartDateValue);
        _startDate.setKeyListener(null);
        _startTime = (EditText) findViewById(R.id.StartTimeValue);
        _startTime.setKeyListener(null);
        _endDate = (EditText) findViewById(R.id.EndDateValue);
        _endDate.setKeyListener(null);
        _endTime = (EditText) findViewById(R.id.EndTimeValue);
        _endTime.setKeyListener(null);
        _pause = (EditText) findViewById(R.id.PauseValue);
        _comment = (EditText) findViewById(R.id.CommentValue);
    }

    @Override
    protected void onStart() {
        super.onStart();

        LoadData();

        // Bearbeiten mit Dialogen
        _startDate.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowStartDateDialog();
                return true;
            }
        });

        _startTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ShowStartTimeDialog();
                return true;
            }
        });
    }

    private void ShowStartTimeDialog() {
        TimePickerDialog dialog = new TimePickerDialog(
                this, // Context
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        _startDateTimeValue.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        _startDateTimeValue.set(Calendar.MINUTE, minute);

                        _startTime.setText(_timeFormatter.format(_startDateTimeValue.getTime()));
                    }
                }, // Callback
                _startDateTimeValue.get(Calendar.HOUR_OF_DAY), // Stinden in 24h
                _startDateTimeValue.get(Calendar.MINUTE), // Minuten
                true // 24h Anzeige?
        );
        dialog.show();
    }

    private void ShowStartDateDialog() {
        DatePickerDialog dialog = new DatePickerDialog(
                this, // Context
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        _startDateTimeValue.set(year, month, dayOfMonth);

                        _startDate.setText(_dateFormatter.format(_startDateTimeValue.getTime()));
                    }
                }, // Callback
                _startDateTimeValue.get(Calendar.YEAR), // Jahr
                _startDateTimeValue.get(Calendar.MONTH), // Monat
                _startDateTimeValue.get(Calendar.DAY_OF_MONTH) // Tag des Monats
        );
        dialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SaveData();
    }

    private void SaveData() {
        ContentValues values = new ContentValues();
        // Start
        values.put(TimeDataContract.TimeData.Columns.START,
                TimeDataContract.Converter.formatForDb(_startDateTimeValue));
        // Ende
        values.put(TimeDataContract.TimeData.Columns.END,
                TimeDataContract.Converter.formatForDb(_endDateTimeValue));
        // Pause
        int pauseValue = 0;
        if (_pause.getText().toString().isEmpty() == false) {
            pauseValue = Integer.parseInt(_pause.getText().toString());
        }
        values.put(TimeDataContract.TimeData.Columns.PAUSE, pauseValue);

        // Kommentar
        values.put(TimeDataContract.TimeData.Columns.COMMENT,
                _comment.getText().toString());

        // Datenbank aktualisieren
        if (_id == -1) {
            getContentResolver().insert(TimeDataContract.TimeData.CONTENT_URI, values);
        } else {
            Uri updateUri = ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, _id);
            getContentResolver().update(updateUri, values, null, null);
        }
    }

    private void LoadData() {
        // Neuer Datensatz
        if (_id == -1) {
            _startDateTimeValue = Calendar.getInstance();
            _endDateTimeValue = Calendar.getInstance();
            return;
        }

        // Vorhandener Datensatz
        Uri dataUri =
                ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, _id);

        Cursor data = getContentResolver().query(dataUri, null, null, null, null);

        // Daten lesen
        if (data.moveToFirst()) {
            // Startzeit auslesen
            String startValueFromDb =
                    data.getString(data.getColumnIndex(TimeDataContract.TimeData.Columns.START));

            try {
                _startDateTimeValue = TimeDataContract.Converter.parseFromDb(startValueFromDb);

                // Ausgabe an der Oberfläche
                _startDate.setText(_dateFormatter.format(_startDateTimeValue.getTime()));
                _startTime.setText(_timeFormatter.format(_startDateTimeValue.getTime()));

                // Endzeit
                int endIndex = data.getColumnIndex(TimeDataContract.TimeData.Columns.END);
                if (data.isNull(endIndex)) {
                    _endDate.setText("");
                    _endTime.setText("");
                } else {
                    String endValueFromDb = data.getString(endIndex);
                    _endDateTimeValue = TimeDataContract.Converter.parseFromDb(endValueFromDb);

                    _endDate.setText(_dateFormatter.format(_endDateTimeValue.getTime()));
                    _endTime.setText(_timeFormatter.format(_endDateTimeValue.getTime()));
                }

                // Pause
                int pauseValueFromDb = data.getInt(data.getColumnIndex(TimeDataContract.TimeData.Columns.PAUSE));
                _pause.setText(String.valueOf(pauseValueFromDb));

                // Kommentar
                int commentIndex = data.getColumnIndex(TimeDataContract.TimeData.Columns.COMMENT);
                if (data.isNull(commentIndex)) {
                    _comment.setText("");
                } else {
                    String commentFromDb = data.getString(commentIndex);
                    _comment.setText(commentFromDb);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        data.close();
    }
}
