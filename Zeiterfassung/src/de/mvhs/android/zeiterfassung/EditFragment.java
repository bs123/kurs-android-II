package de.mvhs.android.zeiterfassung;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import de.mvhs.android.zeiterfassung.db.ZeitContracts;
import de.mvhs.android.zeiterfassung.db.ZeitContracts.Converters;
import de.mvhs.android.zeiterfassung.db.ZeitContracts.Zeit;
import de.mvhs.android.zeiterfassung.db.ZeitContracts.Zeit.Columns;

public class EditFragment extends Fragment implements IRecordSelectedListener,
		LocationListener {
	public final static String ID_KEY = "ID";
	private long _Id = -1;

	private Calendar _startDate;
	private Calendar _endDate;
	private boolean _readOnly = false;
	private boolean _returnWithResult = false;

	private final DateFormat _DateFormatter = DateFormat
			.getDateInstance(DateFormat.SHORT);
	private final DateFormat _TimeFormatter = DateFormat
			.getTimeInstance(DateFormat.SHORT);

	private final static double _NOT_SET = 1000d;
	private double _Long = _NOT_SET;
	private double _Lat = _NOT_SET;

	private EditText _StartDateField;
	private EditText _StartTimeField;
	private EditText _EndDateFiled;
	private EditText _EndTimeField;
	private EditText _PauseField;
	private EditText _CommentField;
	private EditText _Position;
	private Button _RefreshGps;
	private Button _TakePicture;
	private Button _SelectPicture;
	private ImageView _ImageView;
	private Bitmap _Image = null;

	// Positionierung
	private Location _lastLocation;
	private LocationManager _locationManager;
	private String _provider;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit, container, false);
	}

	@Override
	public void onStart() {
		super.onStart();

		// Positionierung aktivieren, falls von Benutzer gewünscht
		SharedPreferences shared = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		boolean gpsIsActive = shared.getBoolean("log_gps", false);

		if (gpsIsActive && (getActivity() instanceof IRecordSelected) == false) {
			_locationManager = (LocationManager) getActivity()
					.getSystemService(Context.LOCATION_SERVICE);

			_provider = LocationManager.GPS_PROVIDER;

			_lastLocation = _locationManager.getLastKnownLocation(_provider);
		}

		if (!_returnWithResult) {
			// Registrierung des Events / Listeners
			_StartDateField = (EditText) getActivity().findViewById(
					R.id.StartDate);
			_StartTimeField = (EditText) getActivity().findViewById(
					R.id.StartTime);
			_EndDateFiled = (EditText) getActivity().findViewById(R.id.EndDate);
			_EndTimeField = (EditText) getActivity().findViewById(R.id.EndTime);
			_PauseField = (EditText) getActivity().findViewById(
					R.id.PauseDuration);
			_CommentField = (EditText) getActivity().findViewById(
					R.id.CommentText);
			_Position = (EditText) getActivity().findViewById(R.id.Position);
			_RefreshGps = (Button) getActivity().findViewById(
					R.id.RefreshPosition);
			_ImageView = (ImageView) getActivity().findViewById(R.id.Image);

			// Bilder
			_TakePicture = (Button) getActivity()
					.findViewById(R.id.TakePicture);
			_SelectPicture = (Button) getActivity().findViewById(
					R.id.SelectPicture);

			// Camera-App aufrufen
			_TakePicture.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent takePictureIntent = new Intent(
							android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(takePictureIntent, 100);
				}
			});
			// Gallery-App aufrufen
			_SelectPicture.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent selectPictureIntent = new Intent(
							Intent.ACTION_PICK,
							android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
					selectPictureIntent.setType("image/*");

					startActivityForResult(selectPictureIntent, 200);
				}
			});

			_StartDateField
					.setOnLongClickListener(new OnStartDateLongClicked());
			_StartDateField.setKeyListener(null);
			_StartTimeField
					.setOnLongClickListener(new OnStartTimeLongClicked());
			_StartTimeField.setKeyListener(null);
			_EndDateFiled.setOnLongClickListener(new OnEndDateLongClicked());
			_EndDateFiled.setKeyListener(null);
			_EndTimeField.setOnLongClickListener(new OnEndTimeLongClicked());
			_EndTimeField.setKeyListener(null);
			_RefreshGps.setOnClickListener(new OnRefreshGpsClicked());

			if (_Id > 0) {
				loadData();
			}
			_returnWithResult = false;
		}

		// Listener für Positionierung einschalten
		if (_locationManager != null) {
			_locationManager.requestLocationUpdates(_provider, 1000, 100, this);
		} else {
			_RefreshGps.setEnabled(false);
		}
	}

	@Override
	public void onStop() {
		if (_locationManager != null) {
			_locationManager.removeUpdates(this);
			_locationManager = null;
			_lastLocation = null;
		}

		if (!_returnWithResult) {
			_RefreshGps.setOnClickListener(null);

			_StartDateField.setOnLongClickListener(null);
			_StartTimeField.setOnLongClickListener(null);
			_EndDateFiled.setOnLongClickListener(null);
			_EndTimeField.setOnLongClickListener(null);
		}

		super.onStop();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		_returnWithResult = true;

		// Prüfen auf den Aufrufer
		if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
			// Camera hat ein Foto zurückgegeben

			// Prüfen, ob ein Bild bereits geladen ist
			if (_Image != null) {
				_Image.recycle();
				_Image = null;
			}

			// Bild aus der Rückgabe der Camera lesen
			_Image = (Bitmap) data.getExtras().get("data");
			_ImageView.setImageBitmap(null);
			_ImageView.setImageBitmap(_Image);
		} else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
			// Gallery
			// URI des Bildes in der Datenbank
			Uri selectedImage = data.getData();

			// Spalte mit dem Original-Pfad
			String[] pathColumn = { MediaStore.Images.Media.DATA };

			Cursor imageData = getActivity().getContentResolver().query(
					selectedImage, pathColumn, null, null, null);

			if (imageData.moveToFirst()) {
				String filePath = imageData.getString(0);

				if (_Image != null) {
					_Image.recycle();
					_Image = null;
				}

				_Image = BitmapFactory.decodeFile(filePath);
				_ImageView.setImageBitmap(_Image);
			}

			imageData.close();
		}
	}

	private class OnStartDateLongClicked implements OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			if (_startDate == null) {
				_startDate = Calendar.getInstance();
			}

			DatePickerDialog dp = new DatePickerDialog(getActivity(),
					new OnStartDateSelected(), _startDate.get(Calendar.YEAR),
					_startDate.get(Calendar.MONTH),
					_startDate.get(Calendar.DAY_OF_MONTH));

			dp.show();

			return true;
		}
	}

	private class OnStartDateSelected implements OnDateSetListener {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			_startDate.set(year, monthOfYear, dayOfMonth);

			_StartDateField
					.setText(_DateFormatter.format(_startDate.getTime()));
		}

	}

	private class OnEndDateLongClicked implements OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			if (_endDate == null) {
				_endDate = Calendar.getInstance();
			}

			DatePickerDialog dp = new DatePickerDialog(getActivity(),
					new OnEndDateSelected(), _endDate.get(Calendar.YEAR),
					_endDate.get(Calendar.MONTH),
					_endDate.get(Calendar.DAY_OF_MONTH));

			dp.show();

			return true;
		}
	}

	private class OnEndDateSelected implements OnDateSetListener {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
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

			boolean is24 = android.text.format.DateFormat
					.is24HourFormat(getActivity());

			TimePickerDialog tp = new TimePickerDialog(getActivity(),
					new OnStatTimeSelected(),
					_startDate.get(Calendar.HOUR_OF_DAY),
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

			_StartTimeField
					.setText(_TimeFormatter.format(_startDate.getTime()));
		}
	}

	private class OnEndTimeLongClicked implements OnLongClickListener {

		@Override
		public boolean onLongClick(View v) {
			if (_endDate == null) {
				_endDate = Calendar.getInstance();
			}

			boolean is24 = android.text.format.DateFormat
					.is24HourFormat(getActivity());

			TimePickerDialog tp = new TimePickerDialog(getActivity(),
					new OnEndTimeSelected(),
					_endDate.get(Calendar.HOUR_OF_DAY),
					_endDate.get(Calendar.MINUTE), is24);

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
		Cursor data = getActivity().getContentResolver().query(dataUri, null,
				null, null, null);

		if (data != null && data.moveToFirst()) {
			String startValue = data.getString(data
					.getColumnIndex(Columns.START));

			// Konvertieren der Startzeit
			try {
				_startDate = Calendar.getInstance();
				_startDate.setTime(ZeitContracts.Converters.DB_FORMATTER
						.parse(startValue));
				_StartDateField.setText(_DateFormatter.format(_startDate
						.getTime()));
				_StartTimeField.setText(_TimeFormatter.format(_startDate
						.getTime()));

			} catch (ParseException e) {
				_StartDateField.setText("");
				_StartTimeField.setText("");
			}

			// Prüfen, ob die Endzeit eingertagen ist
			if (!data.isNull(data.getColumnIndex(Columns.END))) {
				String endValue = data.getString(data
						.getColumnIndex(Columns.END));

				try {
					_endDate = Calendar.getInstance();
					_endDate.setTime(ZeitContracts.Converters.DB_FORMATTER
							.parse(endValue));
					_EndDateFiled.setText(_DateFormatter.format(_endDate
							.getTime()));
					_EndTimeField.setText(_TimeFormatter.format(_endDate
							.getTime()));
				} catch (ParseException e) {
					_EndDateFiled.setText("");
					_EndTimeField.setText("");
				}
			} else {
				_EndDateFiled.setText("");
				_EndTimeField.setText("");
			}

			if (!data.isNull(data.getColumnIndex(Columns.PAUSE))) {
				_PauseField.setText(String.valueOf(data.getInt(data
						.getColumnIndex(Columns.PAUSE))));
			}

			if (!data.isNull(data.getColumnIndex(Columns.COMMENT))) {
				_CommentField.setText(data.getString(data
						.getColumnIndex(Columns.COMMENT)));
			}

			if (!data.isNull(data.getColumnIndex(Columns.LONGTITUDE))
					&& !data.isNull(data.getColumnIndex(Columns.LATITUDE))) {
				_Position
						.setText(formatCoordinate(data.getDouble(data
								.getColumnIndex(Columns.LATITUDE)), data
								.getDouble(data
										.getColumnIndex(Columns.LONGTITUDE))));
			}

			if (!data.isNull(data.getColumnIndex(Columns.IMAGE))) {
				byte[] image = data.getBlob(data.getColumnIndex(Columns.IMAGE));
				_Image = BitmapFactory.decodeByteArray(image, 0, image.length);

				_ImageView.setImageBitmap(_Image);
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
			// if (_Id > 0) {
			// throw new NullPointerException("Hallo ACRA!");
			// }
			getActivity().finish();
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
						Uri deleteUri = ContentUris.withAppendedId(
								Zeit.CONTENT_URI, _Id);
						getActivity().getContentResolver().delete(deleteUri,
								null, null);

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
		values.put(Columns.START,
				Converters.DB_FORMATTER.format(_startDate.getTime()));
		values.put(Columns.END,
				Converters.DB_FORMATTER.format(_endDate.getTime()));
		values.put(Columns.PAUSE, pause);
		values.put(Columns.COMMENT, comment);

		// Save new position, if definied
		if (_Lat != _NOT_SET && _Long != _NOT_SET) {
			values.put(Columns.LONGTITUDE, _Long);
			values.put(Columns.LATITUDE, _Lat);
		}

		// Speichern des Bildes
		if (_Image != null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			_Image.compress(Bitmap.CompressFormat.JPEG, 75, baos);
			values.put(Columns.IMAGE, baos.toByteArray());
		}

		if (_Id > 0) {
			// Aktualisierung des Eintrages
			Uri updateUri = ContentUris.withAppendedId(Zeit.CONTENT_URI, _Id);
			getActivity().getContentResolver().update(updateUri, values, null,
					null);
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
			_Position.setEnabled(!_readOnly);
			_RefreshGps.setEnabled(!_readOnly && _locationManager != null);
		}
	}

	private class OnRefreshGpsClicked implements
			android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (_locationManager != null && _lastLocation != null) {
				_Long = _lastLocation.getLongitude();
				_Lat = _lastLocation.getLatitude();
				_Position.setText(formatCoordinate(_Long, _Lat));
			}
		}
	}

	private String formatCoordinate(double longitude, double latitude) {
		String formated = "";

		if (longitude != _NOT_SET && latitude != _NOT_SET) {
			// Decimal Degrees = Degrees + minutes/60 + seconds/3600
			int longGrad = (int) longitude;
			int longMin = (int) ((longitude - longGrad) * 60);
			int longSec = (int) ((longitude - longGrad - (longMin / 60d)) * 3600);

			int latGrad = (int) latitude;
			int latMin = (int) ((latitude - latGrad) * 60);
			int latSec = (int) ((latitude - latGrad - (latMin / 60d)) * 3600);

			formated = getString(R.string.CoordinateFormatString, longGrad,
					longMin, longSec, latGrad, latMin, latSec);
		}

		return formated;
	}

	@Override
	public void onLocationChanged(Location location) {
		_lastLocation = location;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// Nichts tun
	}

	@Override
	public void onProviderEnabled(String provider) {
		// Nichts tun
	}

	@Override
	public void onProviderDisabled(String provider) {
		// Nichts tun
	}
}
