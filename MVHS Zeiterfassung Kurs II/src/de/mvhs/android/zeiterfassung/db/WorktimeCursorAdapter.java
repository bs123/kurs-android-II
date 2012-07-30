package de.mvhs.android.zeiterfassung.db;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.mvhs.android.zeiterfassung.R;

public class WorktimeCursorAdapter extends SimpleCursorAdapter {
  // Layout ID der Zeile
  private final static int      _LAYOUT_ID          = R.layout.worktime_row;
  // Hilfsconstanten für Zeitausgabe
  private final static int      _SEC_MUNUTE_DEVIDER = 60;
  private final static int      _SEC_HOUR_DEVIDER   = 60 * _SEC_MUNUTE_DEVIDER;
  private final static int      _SEC_DAY_DEVIDER    = 24 * _SEC_HOUR_DEVIDER;
  // Darzustellende Spalten
  private final static String[] _FROM               = new String[] { WorktimeTable.COLUMN_SELECT_START_DATE, WorktimeTable.COLUMN_SELECT_START_TIME,
      WorktimeTable.COLUMN_SELECT_END_DATE, WorktimeTable.COLUMN_SELECT_END_TIME, WorktimeTable.COLUMN_SELECT_WORK_TIME };
  // Zuordnung der Spalten zu den Views in der Zeile
  private final static int[]    _To                 = new int[] { R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5 };

  public WorktimeCursorAdapter(Context context, Cursor data) {
    super(context, _LAYOUT_ID, data, _FROM, _To, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
  }

  // Neue Zeile erzeugen
  @Override
  public View newView(Context context, Cursor data, ViewGroup parent) {

    final LayoutInflater inflater = LayoutInflater.from(context);
    View v = inflater.inflate(_LAYOUT_ID, parent, false);

    TextView start_date = (TextView) v.findViewById(R.id.text1);
    TextView start_time = (TextView) v.findViewById(R.id.text2);
    TextView end_date = (TextView) v.findViewById(R.id.text3);
    TextView end_time = (TextView) v.findViewById(R.id.text4);
    TextView work_time = (TextView) v.findViewById(R.id.text5);
    if (start_date != null) {
      start_date.setText(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_START_DATE)));
    }
    if (start_time != null) {
      start_time.setText(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_START_TIME)));
    }
    if (end_date != null) {
      end_date.setText(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_END_DATE)));
    }
    if (end_time != null) {
      end_time.setText(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_END_TIME)));
    }
    if (work_time != null) {
      work_time.setText(getWorkTimeString(data.getLong(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_WORK_TIME))));
    }

    return v;
  }

  // Binden der Daten an eine bereits vorhandene Zeile
  @Override
  public void bindView(View v, Context context, Cursor data) {
    TextView start_date = (TextView) v.findViewById(R.id.text1);
    TextView start_time = (TextView) v.findViewById(R.id.text2);
    TextView end_date = (TextView) v.findViewById(R.id.text3);
    TextView end_time = (TextView) v.findViewById(R.id.text4);
    TextView work_time = (TextView) v.findViewById(R.id.text5);
    if (start_date != null) {
      start_date.setText(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_START_DATE)));
    }
    if (start_time != null) {
      start_time.setText(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_START_TIME)));
    }
    if (end_date != null) {
      end_date.setText(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_END_DATE)));
    }
    if (end_time != null) {
      end_time.setText(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_END_TIME)));
    }
    if (work_time != null) {
      work_time.setText(getWorkTimeString(data.getLong(data.getColumnIndex(WorktimeTable.COLUMN_SELECT_WORK_TIME))));
    }
  }

  // Formatierung der Zeit
  private String getWorkTimeString(long seconds) {
    StringBuilder returnValue = new StringBuilder();
    long totalDays = seconds / _SEC_DAY_DEVIDER;

    if (totalDays > 0) {
      returnValue.append(String.valueOf(totalDays)).append(" ");
    }
    int hours = (int) (seconds - (totalDays * _SEC_DAY_DEVIDER)) / _SEC_HOUR_DEVIDER;
    if (hours < 10) {
      returnValue.append("0").append(String.valueOf(hours)).append(":");
    } else {
      returnValue.append(String.valueOf(hours)).append(":");
    }
    int minutes = (int) (seconds - ((totalDays * _SEC_DAY_DEVIDER) + (hours * _SEC_HOUR_DEVIDER))) / _SEC_MUNUTE_DEVIDER;
    if (minutes < 10) {
      returnValue.append("0").append(String.valueOf(minutes));
    } else {
      returnValue.append(String.valueOf(minutes));
    }

    return returnValue.toString();
  }
}
