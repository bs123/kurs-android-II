package de.mvhs.android.zeiterfassung.db;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;
import de.mvhs.android.zeiterfassung.R;
import de.mvhs.android.zeiterfassung.db.ZeitContracts.Zeit.Columns;

public class ZeitAdapter extends SimpleCursorAdapter {

	private final static int _layout = R.layout.row_list;
	private final static String[] _from = { Columns.START, Columns.END };
	private final static int[] _to = { R.id.Text1, R.id.Text2 };
	private int _startColumnIndex = -1;
	private int _endColumnIndex = -1;

	private final static DateFormat _UI_DATE_FORMATTER = DateFormat
			.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

	public ZeitAdapter(Context context, Cursor c, int flags) {
		super(context, _layout, c, _from, _to, flags);
	}

	@Override
	public Cursor swapCursor(Cursor c) {
		// Index der Spalte nur ein mal bestimmen, nicht bei jeden Zugrif
		if (c != null) {
			_startColumnIndex = c.getColumnIndex(Columns.START);
			_endColumnIndex = c.getColumnIndex(Columns.END);
		}
		return super.swapCursor(c);
	}

	@Override
	public void bindView(View view, Context context, Cursor data) {
		ViewHolder holder;
		if (view.getTag() == null) {
			// Views bestimmen
			TextView startTime = (TextView) view.findViewById(R.id.Text1);
			TextView endTime = (TextView) view.findViewById(R.id.Text2);

			holder = new ViewHolder(startTime, endTime);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

		String startTimeString = data.getString(_startColumnIndex);

		try {
			Date startTimeAsDate = ZeitContracts.Converters.DB_FORMATTER
					.parse(startTimeString);

			holder.StartView
					.setText(_UI_DATE_FORMATTER.format(startTimeAsDate));
		} catch (ParseException e) {
			holder.StartView.setText("--");
		}

		if (!data.isNull(_endColumnIndex)) {
			String endTimeString = data.getString(_endColumnIndex);

			try {
				Date endTimeAsDate = ZeitContracts.Converters.DB_FORMATTER
						.parse(endTimeString);

				holder.EndView
						.setText(_UI_DATE_FORMATTER.format(endTimeAsDate));
			} catch (ParseException e) {
				holder.EndView.setText("--");
			}
		} else {
			holder.EndView.setText("--");
		}
	}

	private class ViewHolder {
		public final TextView StartView;
		public final TextView EndView;

		public ViewHolder(TextView startView, TextView endView) {
			StartView = startView;
			EndView = endView;
		}
	}

}
