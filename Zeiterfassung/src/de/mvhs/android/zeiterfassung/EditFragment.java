package de.mvhs.android.zeiterfassung;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import de.mvhs.android.zeiterfassung.db.ZeitContracts;
import de.mvhs.android.zeiterfassung.db.ZeitContracts.Converters;
import de.mvhs.android.zeiterfassung.db.ZeitContracts.Zeit;
import de.mvhs.android.zeiterfassung.db.ZeitContracts.Zeit.Columns;

public class EditFragment extends Fragment implements IRecordSelectedListener {
  public final static String ID_KEY         = "ID";
  private long               _Id            = -1;

  private Calendar           _startDate;
  private Calendar           _endDate;
  private boolean            _readOnly      = false;

  private final DateFormat   _DateFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
  private final DateFormat   _TimeFormatter = DateFormat.getTimeInstance(DateFormat.SHORT);

  private EditText           _StartDateField;
  private EditText           _StartTimeField;
  private EditText           _EndDateFiled;
  private EditText           _EndTimeField;
  private EditText           _PauseField;
  private EditText           _CommentField;

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Auslesen der ID, falls diese übergeben wurde
    if (getActivity().getIntent().getExtras() != null) {
      _Id = getActivity().getIntent().getLongExtra(ID_KEY, -1);
    }

    if (getActivity() instanceof IRecordSelected) {
      setHasOptionsMenu(false);
    } else {
      setHasOptionsMenu(true);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_edit, container, false);
  }

  @Override
  public void onStart() {
    super.onStart();

    // Registrierung des Events / Listeners
    _StartDateField = (EditText) getActivity().findViewById(R.id.StartDate);
    _StartTimeField = (EditText) getActivity().findViewById(R.id.StartTime);
    _EndDateFiled = (EditText) getActivity().findViewById(R.id.EndDate);
    _EndTimeField = (EditText) getActivity().findViewById(R.id.EndTime);
    _PauseField = (EditText) getActivity().findViewById(R.id.PauseDuration);
    _CommentField = (EditText) getActivity().findViewById(R.id.CommentText);

    _StartDateField.setOnLongClickListener(new OnStartDateLongClicked());
    _StartDateField.setKeyListener(null);
    _StartTimeField.setOnLongClickListener(new OnStartTimeLongClicked());
    _StartTimeField.setKeyListener(null);
    _EndDateFiled.setOnLongClickListener(new OnEndDateLongClicked());
    _EndDateFiled.setKeyListener(null);
    _EndTimeField.setOnLongClickListener(new OnEndTimeLongClicked());
    _EndTimeField.setKeyListener(null);

    if (_Id > 0) {
      loadData();
    }
  }

  @Override
  public void onStop() {
    _StartDateField.setOnLongClickListener(null);
    _StartTimeField.setOnLongClickListener(null);
    _EndDateFiled.setOnLongClickListener(null);
    _EndTimeField.setOnLongClickListener(null);

    super.onStop();
  }

  private class OnStartDateLongClicked implements OnLongClickListener {

    @Override
    public boolean onLongClick(View v) {
      if (_startDate == null) {
        _startDate = Calendar.getInstance();
      }

      DatePickerDialog dp = new DatePickerDialog(getActivity(), new OnStartDateSelected(), _startDate.get(Calendar.YEAR), _startDate.get(Calendar.MONTH),
              _startDate.get(Calendar.DAY_OF_MONTH));

      dp.show();

      return true;
    }
  }

  private class OnStartDateSelected implements OnDateSetListener {

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
      _startDate.set(year, monthOfYear, dayOfMonth);

      _StartDateField.setText(_DateFormatter.format(_startDate.getTime()));
    }

  }

  private class OnEndDateLongClicked implements OnLongClickListener {

    @Override
    public boolean onLongClick(View v) {
      if (_endDate == null) {
        _endDate = Calendar.getInstance();
      }

      DatePickerDialog dp = new DatePickerDialog(getActivity(), new OnEndDateSelected(), _endDate.get(Calendar.YEAR), _endDate.get(Calendar.MONTH),
              _endDate.get(Calendar.DAY_OF_MONTH));

      dp.show();

      return true;
    }
  }

  private class OnEndDateSelected implements OnDateSetListener {

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
      _endDate.set(year, monthOfYear, dayOfMonth);

      _EndDateFiled.setText(_DateFormatter.format(_endDate.getTime()));
    }

  }

  private class OnStartTimeLongClicked implements OnLongClickListener {

    @Override
    public boolean onLongClick(View v) {
      if (_startDate == null) {
        _startDate = Calendar.getInstance();
      }

      boolean is24 = android.text.format.DateFormat.is24HourFormat(getActivity());

      TimePickerDialog tp = new TimePickerDialog(getActivity(), new OnStatTimeSelected(), _startDate.get(Calendar.HOUR_OF_DAY),
              _startDate.get(Calendar.MINUTE), is24);

      tp.show();

      return true;
    }
  }

  private class OnStatTimeSelected implements OnTimeSetListener {

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
      _startDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
      _startDate.set(Calendar.MINUTE, minute);

      _StartTimeField.setText(_TimeFormatter.format(_startDate.getTime()));
    }
  }

  private class OnEndTimeLongClicked implements OnLongClickListener {

    @Override
    public boolean onLongClick(View v) {
      if (_endDate == null) {
        _endDate = Calendar.getInstance();
      }

      boolean is24 = android.text.format.DateFormat.is24HourFormat(getActivity());

      TimePickerDialog tp = new TimePickerDialog(getActivity(), new OnEndTimeSelected(), _endDate.get(Calendar.HOUR_OF_DAY), _endDate.get(Calendar.MINUTE),
              is24);

      tp.show();

      return true;
    }
  }

  private class OnEndTimeSelected implements OnTimeSetListener {

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
      _endDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
      _endDate.set(Calendar.MINUTE, minute);

      _EndTimeField.setText(_TimeFormatter.format(_endDate.getTime()));
    }
  }

  private void loadData() {
    Uri dataUri = ContentUris.withAppendedId(Zeit.CONTENT_URI, _Id);
    Cursor data = getActivity().getContentResolver().query(dataUri, null, null, null, null);

    if (data != null && data.moveToFirst()) {
      String startValue = data.getString(data.getColumnIndex(Columns.START));

      // Konvertieren der Startzeit
      try {
        _startDate = Calendar.getInstance();
        _startDate.setTime(ZeitContracts.Converters.DB_FORMATTER.parse(startValue));
        _StartDateField.setText(_DateFormatter.format(_startDate.getTime()));
        _StartTimeField.setText(_TimeFormatter.format(_startDate.getTime()));

      } catch (ParseException e) {
        _StartDateField.setText("");
        _StartTimeField.setText("");
      }

      // Prüfen, ob die Endzeit eingertagen ist
      if (!data.isNull(data.getColumnIndex(Columns.END))) {
        String endValue = data.getString(data.getColumnIndex(Columns.END));

        try {
          _endDate = Calendar.getInstance();
          _endDate.setTime(ZeitContracts.Converters.DB_FORMATTER.parse(endValue));
          _EndDateFiled.setText(_DateFormatter.format(_endDate.getTime()));
          _EndTimeField.setText(_TimeFormatter.format(_endDate.getTime()));
        } catch (ParseException e) {
          _EndDateFiled.setText("");
          _EndTimeField.setText("");
        }
      } else {
        _EndDateFiled.setText("");
        _EndTimeField.setText("");
      }

      if (!data.isNull(data.getColumnIndex(Columns.PAUSE))) {
        _PauseField.setText(String.valueOf(data.getInt(data.getColumnIndex(Columns.PAUSE))));
      }

      if (!data.isNull(data.getColumnIndex(Columns.COMMENT))) {
        _CommentField.setText(data.getString(data.getColumnIndex(Columns.COMMENT)));
      }
    }
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.edit_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.mnu_cancel:
        if (_Id > 0) {
          throw new NullPointerException("Hallo ACRA!");
        }
        // getActivity().finish();
        break;

      case R.id.mnu_delete:
        if (_Id > 0) {
          delete();
        }
        break;

      case R.id.mnu_save:
        save();
        break;

      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void delete() {

    // Aufbau eines Dialoges
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Löschen ...") // Titel des Dialoges setzen
            .setMessage("Wollen Sie den Datensatz wirklich löschen?") // Nachricht
            // für
            // den
            // Benutzer
            .setIcon(R.drawable.ic_menu_delete) // Icopn för das Dialog
            .setPositiveButton("Löschen", new OnClickListener() {

              @Override
              public void onClick(DialogInterface dialog, int which) {
                Uri deleteUri = ContentUris.withAppendedId(Zeit.CONTENT_URI, _Id);
                getActivity().getContentResolver().delete(deleteUri, null, null);

                getActivity().finish();
              }
            }) // Button für die positive
               // Antwort
            .setNegativeButton("Abbrechen", null); // Button zum Abbrechen
    // der Aktion

    // Dialog anzeigen
    builder.create().show();
  }

  private void save() {
    int pause = 0;
    pause = Integer.parseInt(_PauseField.getText().toString());
    String comment = _CommentField.getText().toString();

    ContentValues values = new ContentValues();
    values.put(Columns.START, Converters.DB_FORMATTER.format(_startDate.getTime()));
    values.put(Columns.END, Converters.DB_FORMATTER.format(_endDate.getTime()));
    values.put(Columns.PAUSE, pause);
    values.put(Columns.COMMENT, comment);

    if (_Id > 0) {
      // Aktualisierung des Eintrages
      Uri updateUri = ContentUris.withAppendedId(Zeit.CONTENT_URI, _Id);
      getActivity().getContentResolver().update(updateUri, values, null, null);
    } else {
      // Neuer eintrag
      getActivity().getContentResolver().insert(Zeit.CONTENT_URI, values);
    }

    getActivity().finish();
  }

  @Override
  public void onRecordSelectionChanged(long newRecordId) {
    if (_Id != newRecordId) {
      setReadOnlyState(true);

      _Id = newRecordId;
      loadData();
    }
  }

  private void setReadOnlyState(boolean isReadOnly) {
    if (_readOnly != isReadOnly) {
      _readOnly = isReadOnly;

      _StartDateField.setEnabled(!_readOnly);
      _StartTimeField.setEnabled(!_readOnly);
      _EndDateFiled.setEnabled(!_readOnly);
      _EndTimeField.setEnabled(!_readOnly);
      _CommentField.setEnabled(!_readOnly);
      _PauseField.setEnabled(!_readOnly);
    }
  }
}
