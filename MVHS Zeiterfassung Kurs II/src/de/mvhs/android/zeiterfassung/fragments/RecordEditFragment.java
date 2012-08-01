package de.mvhs.android.zeiterfassung.fragments;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import de.mvhs.android.zeiterfassung.OnRecordChangedListener;
import de.mvhs.android.zeiterfassung.R;
import de.mvhs.android.zeiterfassung.db.DBHelper;
import de.mvhs.android.zeiterfassung.db.WorkTimeContentProvider;
import de.mvhs.android.zeiterfassung.db.WorktimeTable;

public class RecordEditFragment extends SherlockFragment implements LocationListener, OnRecordChangedListener {
  // Variablen
  public static final String                       ID_KEY               = "ID";
  public static final String                       READONLY_KEY         = "readonly";
  private long                                     _ID                  = -1;
  private boolean                                  _ReadOnly            = true;
  private String                                   _ContactName         = "";
  private String                                   _GPSPosition         = "";
  private Date                                     _StartTime           = null;
  private Date                                     _TempStartTime;
  private Date                                     _EndTime             = null;
  private LocationManager                          _LocationManager     = null;
  private final static int                         _CAMERA_REQUEST_CODE = 1;
  private final static int                         _GALERY_REQUEST_CODE = 2;
  private Bitmap                                   _Image               = null;
  private Date                                     _TempEndTime;
  private WorktimeTable                            _Table               = new WorktimeTable(getActivity());
  private static final DateFormat                  _TFmedium            = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);

  private final DatePickerDialog.OnDateSetListener _OnStartDateListener = new DatePickerDialog.OnDateSetListener() {

                                                                          public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                                              int dayOfMonth) {
                                                                            onStartDateSet(view, year, monthOfYear, dayOfMonth);
                                                                          }
                                                                        };

  private final TimePickerDialog.OnTimeSetListener _OnStartTimeListener = new TimePickerDialog.OnTimeSetListener() {

                                                                          public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                                            onStartTimeSet(view, hourOfDay, minute);
                                                                          }
                                                                        };

  private final DatePickerDialog.OnDateSetListener _OnEndDateListener   = new DatePickerDialog.OnDateSetListener() {

                                                                          public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                                              int dayOfMonth) {
                                                                            onEndDateSet(view, year, monthOfYear, dayOfMonth);
                                                                          }
                                                                        };

  private final TimePickerDialog.OnTimeSetListener _OnEndTimeListener   = new TimePickerDialog.OnTimeSetListener() {

                                                                          public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                                            onEndTimeSet(view, hourOfDay, minute);
                                                                          }
                                                                        };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    // Auslesen der ID aus der Nachricht, falls vorhanden
    if (getActivity().getIntent().getExtras() != null) {
      Bundle extra = getActivity().getIntent().getExtras();
      if (extra.containsKey(ID_KEY)) {
        this._ID = extra.getLong(ID_KEY);
      }
      if (extra.containsKey(READONLY_KEY)) {
        this._ReadOnly = extra.getBoolean(READONLY_KEY);
      }
    }

    _LocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.record_edit_fragment, container);
  }

  @Override
  public void onStart() {
    super.onStart();
    Criteria criteria = new Criteria();
    String provider = _LocationManager.getBestProvider(criteria, true);
    if (provider != null) {
      _LocationManager.requestLocationUpdates(provider, 1000, 1, this);
    }
    // Laden der Elemente
    EditText startTime = (EditText) getActivity().findViewById(R.id.txt_start_time);
    EditText endTime = (EditText) getActivity().findViewById(R.id.txt_end_time);
    Button searchContact = (Button) getActivity().findViewById(R.id.cmd_select_contact);
    Button searchPosition = (Button) getActivity().findViewById(R.id.cmd_select_position);
    ImageView imageView = (ImageView) getActivity().findViewById(R.id.img_view);

    // Initialisierung der Elemente
    startTime.setKeyListener(null);
    endTime.setKeyListener(null);

    init();

    startTime.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        DatePickerDialog dp = new DatePickerDialog(getActivity(), _OnStartDateListener, _StartTime.getYear() + 1900, _StartTime.getMonth(),
            _StartTime.getDate());
        dp.show();
      }
    });

    endTime.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        DatePickerDialog dp = new DatePickerDialog(getActivity(), _OnEndDateListener, _EndTime.getYear() + 1900, _EndTime.getMonth(), _EndTime
            .getDate());
        dp.show();
      }
    });

    // Kontakt auswählen
    searchContact.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View view) {
        runSearchContact();
      }
    });

    // Position bestimmen
    searchPosition.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View view) {
        runSearchPosition();
      }
    });

    // Kamera über Intent aufrufen
    imageView.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View view) {
        runTakePicture();
      }
    });

    // Bildergalerie über Intent aufrufen
    imageView.setOnLongClickListener(new OnLongClickListener() {

      @Override
      public boolean onLongClick(View view) {
        return runSelectPicture();
      }
    });
  }

  private void init() {
    // Laden der Elemente
    EditText startTime = (EditText) getActivity().findViewById(R.id.txt_start_time);
    EditText endTime = (EditText) getActivity().findViewById(R.id.txt_end_time);
    EditText contact = (EditText) getActivity().findViewById(R.id.txt_contact);
    EditText position = (EditText) getActivity().findViewById(R.id.txt_position);
    ImageView imageView = (ImageView) getActivity().findViewById(R.id.img_view);
    Button searchContact = (Button) getActivity().findViewById(R.id.cmd_select_contact);
    Button searchPosition = (Button) getActivity().findViewById(R.id.cmd_select_position);

    // Editierbarkeit setzen
    startTime.setEnabled(!_ReadOnly);
    endTime.setEnabled(!_ReadOnly);
    contact.setEnabled(!_ReadOnly);
    position.setEnabled(!_ReadOnly);
    imageView.setEnabled(!_ReadOnly);
    searchContact.setEnabled(!_ReadOnly);
    searchPosition.setEnabled(!_ReadOnly);

    // Laden der Daten
    Cursor data = getActivity().getContentResolver().query(WorkTimeContentProvider.CONTENT_URI_WORK_TIME, null, WorktimeTable.COLUMN_ID + "=?",
        new String[] { String.valueOf(_ID) }, null);

    if (data != null && data.moveToFirst()) {
      try {
        _StartTime = DBHelper.DB_DATE_FORMAT.parse(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_START_TIME)));
        _EndTime = DBHelper.DB_DATE_FORMAT.parse(data.getString(data.getColumnIndex(WorktimeTable.COLUMN_END_TIME)));
      } catch (ParseException e) {
        e.printStackTrace();
      }

      if (!data.isNull(data.getColumnIndex(WorktimeTable.COLUMN_CONTACT_NAME))) {
        _ContactName = data.getString(data.getColumnIndex(WorktimeTable.COLUMN_CONTACT_NAME));
      }

      if (!data.isNull(data.getColumnIndex(WorktimeTable.COLUMN_POSITION))) {
        _GPSPosition = data.getString(data.getColumnIndex(WorktimeTable.COLUMN_POSITION));
      }

      if (!data.isNull(data.getColumnIndex(WorktimeTable.COLUMN_PICTURE))) {
        byte[] image = data.getBlob(data.getColumnIndex(WorktimeTable.COLUMN_PICTURE));
        _Image = BitmapFactory.decodeByteArray(image, 0, image.length);
      }
    }

    // Inhalte setzen
    startTime.setText(_StartTime != null ? _TFmedium.format(_StartTime) : "");
    endTime.setText(_EndTime != null ? _TFmedium.format(_EndTime) : "");
    contact.setText(_ContactName);
    position.setText(_GPSPosition);
    imageView.setImageBitmap(_Image);
  }

  @Override
  public void onStop() {
    // Laden der Elemente
    EditText startTime = (EditText) getActivity().findViewById(R.id.txt_start_time);
    startTime.setOnClickListener(null);
    EditText endTime = (EditText) getActivity().findViewById(R.id.txt_end_time);
    endTime.setOnClickListener(null);
    Button searchContact = (Button) getActivity().findViewById(R.id.cmd_select_contact);
    searchContact.setOnClickListener(null);
    _LocationManager.removeUpdates(this);
    super.onStop();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.opt_cancel:
        // Aktuelle Activity beenden
        getActivity().finish();

        break;

      case R.id.opt_delete:
        // Abfrage, ob wirklich gelöscht werden soll
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.dlg_confirm_title).setMessage(R.string.dlg_confirm_message).setIcon(R.drawable.ic_menu_delete)
            .setNegativeButton(R.string.dlg_cancel, new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
              }
            }).setPositiveButton(R.string.dlg_delete, new DialogInterface.OnClickListener() {

              public void onClick(DialogInterface dialog, int which) {
                _Table.deleteWorktime(_ID);
                getActivity().finish();
              }
            });

        builder.create().show();

        break;

      case R.id.opt_save:
        // Speichern des Datensatzen und beenden der Activity
        EditText contact = (EditText) getActivity().findViewById(R.id.txt_contact);
        EditText position = (EditText) getActivity().findViewById(R.id.txt_position);
        ContentValues values = new ContentValues();
        values.put(WorktimeTable.COLUMN_START_TIME, DBHelper.DB_DATE_FORMAT.format(_StartTime));
        values.put(WorktimeTable.COLUMN_END_TIME, DBHelper.DB_DATE_FORMAT.format(_EndTime));
        values.put(WorktimeTable.COLUMN_CONTACT_NAME, contact.getText().toString());
        values.put(WorktimeTable.COLUMN_POSITION, position.getText().toString());
        if (_Image != null) {
          // Konvertierung in ByArray, um in DB speichern zu können
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          _Image.compress(Bitmap.CompressFormat.JPEG, 75, baos);
          values.put(WorktimeTable.COLUMN_PICTURE, baos.toByteArray());
        }
        getActivity().getContentResolver().update(WorkTimeContentProvider.CONTENT_URI_WORK_TIME, values, WorktimeTable.COLUMN_ID + "=?",
            new String[] { String.valueOf(_ID) });
        getActivity().finish();

        break;
      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    ImageView imageView = (ImageView) getActivity().findViewById(R.id.img_view);
    // Bild kommt von der Kamera
    if (requestCode == _CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      if (_Image != null) {
        // Verwerfen des aktuellen Bildes
        _Image.recycle();
      }

      // Bild aus dem Rückgabewert des Intents auslesen
      _Image = (Bitmap) data.getExtras().get("data");
      imageView.setImageBitmap(_Image);
    }
    // Bild kommt aus der Galerie
    else if (requestCode == _GALERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
      Uri selectedImage = data.getData();
      String[] filePathColumn = new String[] { MediaStore.Images.Media.DATA };

      Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
      if (cursor.moveToFirst()) {

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String filePath = cursor.getString(columnIndex);
        cursor.close();

        if (_Image != null) {
          // Verwerfen des aktuellen Bildes
          _Image.recycle();
        }

        _Image = BitmapFactory.decodeFile(filePath);

        imageView.setImageBitmap(_Image);
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  private void onStartDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    // Ausgelesenes Datum setzen
    _TempStartTime = new Date(year - 1900, monthOfYear, dayOfMonth);

    // Zeit Dialog starten
    TimePickerDialog tp = new TimePickerDialog(getActivity(), _OnStartTimeListener, _StartTime.getHours(), _StartTime.getMinutes(), true);
    tp.show();

  }

  private void onStartTimeSet(TimePicker view, int hourOfDay, int minute) {
    // Neues Datum und Uhrzeit zwischenspeichern
    _StartTime = new Date(_TempStartTime.getYear(), _TempStartTime.getMonth(), _TempStartTime.getDate(), hourOfDay, minute);

    // Neues Datum und Uhrzeit ausgeben
    EditText startTime = (EditText) getActivity().findViewById(R.id.txt_start_time);
    startTime.setText(_TFmedium.format(_StartTime));
  }

  private void onEndDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    // Ausgelesenes Datum setzen
    _TempEndTime = new Date(year - 1900, monthOfYear, dayOfMonth);

    // Zeit Dialog starten
    TimePickerDialog tp = new TimePickerDialog(getActivity(), _OnEndTimeListener, _EndTime.getHours(), _EndTime.getMinutes(), true);
    tp.show();

  }

  private void onEndTimeSet(TimePicker view, int hourOfDay, int minute) {
    // Neues Datum und Uhrzeit zwischenspeichern
    _EndTime = new Date(_TempEndTime.getYear(), _TempEndTime.getMonth(), _TempEndTime.getDate(), hourOfDay, minute);

    // Neues Datum und Uhrzeit ausgeben
    EditText endTime = (EditText) getActivity().findViewById(R.id.txt_end_time);
    endTime.setText(_TFmedium.format(_EndTime));
  }

  private void runSearchContact() {
    final EditText contact = (EditText) getActivity().findViewById(R.id.txt_contact);

    String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME };
    String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '" + ("1") + "'";
    String[] selectionArgs = null;
    String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

    final Cursor data = getActivity().getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, selectionArgs,
        sortOrder);

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle(R.string.lbl_contact);
    builder.setSingleChoiceItems(data, -1, ContactsContract.Contacts.DISPLAY_NAME, new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int item) {
        if (data != null && data.moveToPosition(item) && !data.isNull(data.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))) {
          contact.setText(data.getString(data.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
        }

        dialog.dismiss();
      }
    });

    builder.create().show();
  }

  private void runSearchPosition() {
    // Initialisierung der Textbox
    final EditText position = (EditText) getActivity().findViewById(R.id.txt_position);

    // Einstellungen bestimmen
    Criteria criteria = new Criteria();

    // Provider bestimmen
    String provider = _LocationManager.getBestProvider(criteria, true);

    // Prüfen ob Lokalisierung eingeschaltet ist
    if (provider == null) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setTitle(R.string.dlg_position_title).setMessage(R.string.dlg_position_message)
          .setNegativeButton(R.string.dlg_no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          }).setPositiveButton(R.string.dlg_yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
              // Benutzer zur Aktivierng Geolokalisierung auffordern
              Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
              startActivity(settingsIntent);
            }
          }).create().show();
    } else {

      provider = _LocationManager.getBestProvider(criteria, true);

      if (provider != null) {
        Location location = _LocationManager.getLastKnownLocation(provider);

        if (location != null) {
          position.setText("Lat: " + location.getLatitude() + " - Long: " + location.getLongitude());
        }
      }
    }
  }

  private void runTakePicture() {
    Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
    startActivityForResult(intent, _CAMERA_REQUEST_CODE);
  }

  private boolean runSelectPicture() {
    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    intent.setType("image/*");
    startActivityForResult(Intent.createChooser(intent, "Foto auswählen"), _GALERY_REQUEST_CODE);
    return true;
  }

  @Override
  public void onLocationChanged(Location location) {

  }

  @Override
  public void onProviderDisabled(String provider) {

  }

  @Override
  public void onProviderEnabled(String provider) {

  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {

  }

  @Override
  public void onRecordChanged(long id, boolean readOnly) {
    _ID = id;
    _ReadOnly = readOnly;
    init();
  }
}
