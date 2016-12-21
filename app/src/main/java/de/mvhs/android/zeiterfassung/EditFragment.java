package de.mvhs.android.zeiterfassung;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class EditFragment extends Fragment implements IChangeContent {
  public final static String ID_KEY = "EditItemId";
  public final static String READONLY_KEY = "IsReadonly";

  private final static String[] _COLUMNS = {
      TimeDataContract.TimeData.Columns.START, // Index 0
      TimeDataContract.TimeData.Columns.END, // Index 1
      TimeDataContract.TimeData.Columns.PAUSE, // Index 2
      TimeDataContract.TimeData.Columns.COMMENT // Index 3
  };
  private final static int _START_INDEX = 0;
  private final static int _END_INDEX = 1;
  private final static int _PAUSE_INDEX = 2;
  private final static int _COMMENT_INDEX = 3;

  private long _id = -1;
  private boolean _isReadonly = true;
  private Calendar _startDateTimeValue;
  private Calendar _endDatTimeValue;
  private EditText _startDate;
  private DateFormat _dateFormatter;
  private DateFormat _timeFormatter;
  private EditText _startTime;
  private EditText _endDate;
  private EditText _endTime;
  private EditText _pause;
  private EditText _comment;

  Dialog _dialog = null;

  private boolean _cancelled = false;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.fragment_edit_grid, container, false);

    // Init android formatter
    _dateFormatter = android.text.format.DateFormat.getDateFormat(getActivity());
    _timeFormatter = android.text.format.DateFormat.getTimeFormat(getActivity());

    // Auslesen der ID aus Metainformationen
    _id = getActivity().getIntent().getLongExtra(ID_KEY, -1);
    _isReadonly = getActivity().getIntent().getBooleanExtra(READONLY_KEY, true);

    // Initialisieren der UI Elemente
    _startDate = (EditText)rootView.findViewById(R.id.StartDateValue);
    _startDate.setKeyListener(null);
    _startDate.setEnabled(!_isReadonly);
    _startTime = (EditText)rootView.findViewById(R.id.StartTimeValue);
    _startTime.setKeyListener(null);
    _startTime.setEnabled(!_isReadonly);
    _endDate = (EditText)rootView.findViewById(R.id.EndDateValue);
    _endDate.setKeyListener(null);
    _endDate.setEnabled(!_isReadonly);
    _endTime = (EditText)rootView.findViewById(R.id.EndTimeValue);
    _endTime.setKeyListener(null);
    _endTime.setEnabled(!_isReadonly);
    _pause = (EditText)rootView.findViewById(R.id.PauseValue);
    _pause.setEnabled(!_isReadonly);
    _comment = (EditText)rootView.findViewById(R.id.CommentValue);
    _comment.setEnabled(!_isReadonly);

    return rootView;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if(!_isReadonly){
      setHasOptionsMenu(true);
    }
  }

  @Override
  public void onStart() {
    super.onStart();

    if (_isReadonly){
      return;
    }

    // Unterscheidung, neuer oder vorhandener Datensatz
    if (_id == -1) {
      initNewEntry();
    } else {
      loadData();
    }

    // Initialisierung für die Dialoge
    _startDate.setOnLongClickListener(new OnDateDialogShow(getActivity(), _startDateTimeValue, _startDate));
    _startTime.setOnLongClickListener(new OnTimeDialogShow(getActivity(), _startDateTimeValue, _startTime));
    _endDate.setOnLongClickListener(new OnDateDialogShow(getActivity(), _endDatTimeValue, _endDate));
    _endTime.setOnLongClickListener(new OnTimeDialogShow(getActivity(), _endDatTimeValue, _endTime));
  }
  @Override
  public void onStop() {
    // Speichern, nur wenn nicht abgebrochen
    if (!_cancelled) {
      saveData();
    }

    // Verhindet den Fehler beim Drehen des Bildschirms
    if(_dialog != null && _dialog.isShowing()){
      _dialog.dismiss();
    }

    // Deregister dialogs
    _startDate.setOnLongClickListener(null);
    _startTime.setOnLongClickListener(null);
    _endDate.setOnLongClickListener(null);
    _endTime.setOnLongClickListener(null);

    super.onStop();
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.mneu_edit, menu);
    // Löschen nur bei vorhandenen Einträgen erlauben
    if (_id == -1) {
      MenuItem deleteButton = menu.findItem(R.id.MenuItemDelete);
      deleteButton.setVisible(false);
    }

    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.MenItemCancel:
        _cancelled = true;
        getActivity().finish();
        return true;

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
                Uri deleteUri =
                    ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, _id);

                getActivity().getContentResolver().delete(deleteUri, null, null);
                _cancelled = true;
                getActivity().finish();
              }
            });

        builder.create().show();
        return true;

      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void loadData() {
    // Vorhandener Datensatz
    Uri dataUri =
        ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, _id);

    Cursor data = getActivity().getContentResolver().query(dataUri, _COLUMNS, null, null, null);

    // Daten lesen
    if (data.moveToFirst()) {
      try {
        // Startzeit auslesen
        String startValueFromDb = data.getString(_START_INDEX);
        // Startzeit umwandeln
        _startDateTimeValue = TimeDataContract.Converter.parseFromDb(startValueFromDb);

        // Ausgabe an der Oberfläche
        _startDate.setText(_dateFormatter.format(_startDateTimeValue.getTime()));
        _startTime.setText(_timeFormatter.format(_startDateTimeValue.getTime()));

        if (data.isNull(_END_INDEX)) {
          _endDate.setText("");
          _endTime.setText("");
        } else {
          // Endzeit auslesen
          String endValueFromDb = data.getString(_END_INDEX);
          // Endzeit umwandeln
          _endDatTimeValue = TimeDataContract.Converter.parseFromDb(endValueFromDb);

          // Ausgabe an der Oberfläche
          _endDate.setText(_dateFormatter.format(_endDatTimeValue.getTime()));
          _endTime.setText(_timeFormatter.format(_endDatTimeValue.getTime()));
        }

        // Pause auslesen
        _pause.setText(String.valueOf(data.getInt(_PAUSE_INDEX)));

        // Kommentar auslesen
        if (data.isNull(_COMMENT_INDEX)) {
          _comment.setText("");
        } else {
          _comment.setText(data.getString(_COMMENT_INDEX));
        }
      } catch (ParseException e) {
        e.printStackTrace();
      }
    }

    data.close();
  }

  private void initNewEntry() {
    // Titel setzen
    getActivity().setTitle(R.string.NewDataActivityTitle);

    // Initialisierng für auf die aktuelle Zeit
    _startDateTimeValue = Calendar.getInstance();
    _endDatTimeValue = Calendar.getInstance();

    // Ausgabe an der Oberfläche
    _startDate.setText(_dateFormatter.format(_startDateTimeValue.getTime()));
    _startTime.setText(_timeFormatter.format(_startDateTimeValue.getTime()));

    _endDate.setText(_dateFormatter.format(_endDatTimeValue.getTime()));
    _endTime.setText(_timeFormatter.format(_endDatTimeValue.getTime()));

    _pause.setText("0");

    _comment.setText("");
  }

  private void saveData() {
    // Daten sammeln
    ContentValues values = new ContentValues();
    // Startzeit
    values.put(TimeDataContract.TimeData.Columns.START,
        TimeDataContract.Converter.formatForDb(_startDateTimeValue));
    // Endzeit
    values.put(TimeDataContract.TimeData.Columns.END,
        TimeDataContract.Converter.formatForDb(_endDatTimeValue));
    // Pause
    String pauseStringValue = _pause.getText().toString();
    if (pauseStringValue != null && !pauseStringValue.isEmpty()) {
      // Pause gesetzt
      values.put(TimeDataContract.TimeData.Columns.PAUSE, Integer.parseInt(pauseStringValue));
    } else {
      values.put(TimeDataContract.TimeData.Columns.PAUSE, 0);
    }
    // Kommentar
    String commentValue = _comment.getText().toString();
    if (commentValue != null && !commentValue.isEmpty()) {
      values.put(TimeDataContract.TimeData.Columns.COMMENT, commentValue);
    } else {
      values.put(TimeDataContract.TimeData.Columns.COMMENT, "");
    }

    // Neuen Eintrag hinzugügen, oder vorhandenes aktualisieren
    if (_id == -1) {
      // Neues hinzufügen
      getActivity().getContentResolver().insert(TimeDataContract.TimeData.CONTENT_URI, values);
    } else {
      // Vorhandenes aktualisieren
      Uri updateUri = ContentUris.withAppendedId(TimeDataContract.TimeData.CONTENT_URI, _id);
      getActivity().getContentResolver().update(updateUri, values, null, null);
    }
  }

  @Override
  public void onChangeContent(long id) {
    _id = id;

    loadData();
  }

  class OnDateDialogShow implements View.OnLongClickListener{
    private final Context _context;
    private final Calendar _dateToChenge;
    private final EditText _textToChange;

    OnDateDialogShow(Context context, Calendar dateToChenge, EditText textToChange){

      _context = context;
      _dateToChenge = dateToChenge;
      _textToChange = textToChange;
    }


    @Override
    public boolean onLongClick(View v) {
      _dialog = new DatePickerDialog(
          _context,
          new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
              _dateToChenge.set(year, month, dayOfMonth);

              _textToChange.setText(_dateFormatter.format(_dateToChenge.getTime()));
              _dialog = null;
            }
          },
          _dateToChenge.get(Calendar.YEAR),
          _dateToChenge.get(Calendar.MONTH),
          _dateToChenge.get(Calendar.DAY_OF_MONTH)
      );
      _dialog.show();
      return true;
    }
  }

  class OnTimeDialogShow implements View.OnLongClickListener{
    private final Context _context;
    private final Calendar _timeToChange;
    private final EditText _textToChange;

    OnTimeDialogShow(Context context, Calendar timeToChange, EditText textToChange){

      _context = context;
      _timeToChange = timeToChange;
      _textToChange = textToChange;
    }


    @Override
    public boolean onLongClick(View v) {
      boolean is24 = android.text.format.DateFormat.is24HourFormat(_context);

      _dialog = new TimePickerDialog(
          _context,
          new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
              _timeToChange.set(Calendar.HOUR_OF_DAY, hourOfDay);
              _timeToChange.set(Calendar.MINUTE, minute);

              _textToChange.setText(_timeFormatter.format(_timeToChange.getTime()));
              _dialog = null;
            }
          },
          _timeToChange.get(Calendar.HOUR_OF_DAY),
          _timeToChange.get(Calendar.MINUTE),
          is24
      );
      _dialog.show();
      return true;
    }
  }
}
