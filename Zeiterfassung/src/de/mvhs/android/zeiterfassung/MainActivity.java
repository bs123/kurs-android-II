package de.mvhs.android.zeiterfassung;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import de.mvhs.android.zeiterfassung.db.ZeitContentProvider;
import de.mvhs.android.zeiterfassung.db.ZeitTabelle;

public class MainActivity extends SherlockActivity implements LocationListener {
	private final DateFormat _DTF = SimpleDateFormat.getDateTimeInstance(
			SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);
	private EditText _Startzeit;
	private EditText _Endzeit;
	private Button _Start;
	private Button _Ende;

	private LocationManager _LocManager = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Referenz auf die Buttons im Layout suchen
		_Start = (Button) findViewById(R.id.starten);
		_Ende = (Button) findViewById(R.id.beenden);
		_Startzeit = (EditText) findViewById(R.id.startzeit);
		_Endzeit = (EditText) findViewById(R.id.endzeit);

		// Fehler
		_LocManager.getBestProvider(new Criteria(), true);

		_LocManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Listener für "Starten"-Button definieren
		_Start.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				onStarten();
			}
		});

		// Listener für "Beenden"-Button definieren
		_Ende.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				onBeenden();
			}
		});

		// Editierbarkeit der Felder deaktivieren
		_Startzeit.setKeyListener(null);
		_Endzeit.setKeyListener(null);

		pruefeZustand();
	}

	@Override
	protected void onStart() {
		super.onStart();

		Criteria criteria = new Criteria();
		String provider = _LocManager.getBestProvider(criteria, true);
		if (provider != null) {
			_LocManager.requestLocationUpdates(provider, 1000, 1, this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		_LocManager.removeUpdates(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 100) {
			// Position bestimmen
			Criteria creteria = new Criteria();
			String provider = _LocManager.getBestProvider(creteria, true);

			if (provider != null) {
				Location location = _LocManager.getLastKnownLocation(provider);

				if (location != null) {
					TextView locationView = (TextView) findViewById(R.id.txt_gps);

					locationView.setText("Latitude: " + location.getLatitude()
							+ " - Longitude: " + location.getLongitude());
				}
			}
		}
	}

	private void onStarten() {
		// Aktuelle zeit bestimmen
		Date jetzt = new Date();

		// Zeit ausgeben
		_Startzeit.setText(_DTF.format(jetzt));

		ContentValues values = new ContentValues();
		values.put(ZeitTabelle.STARTZEIT, ZeitTabelle.SDF.format(jetzt));

		getContentResolver().insert(ZeitContentProvider.CONTENT_URI, values);

		// Andere Elemente steuern
		_Endzeit.setText("");
		_Start.setEnabled(false);
		_Ende.setEnabled(true);

		// Position bestimmen
		Criteria creteria = new Criteria();
		String provider = _LocManager.getBestProvider(creteria, true);

		if (provider != null) {
			Location location = _LocManager.getLastKnownLocation(provider);

			if (location != null) {
				TextView locationView = (TextView) findViewById(R.id.txt_gps);

				locationView.setText("Latitude: " + location.getLatitude()
						+ " - Longitude: " + location.getLongitude());
			}
		}
		// GPS / Positionierung ausgeschaltet
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("GPS inaktiv");
			builder.setMessage("Wollen Sie GPS aktivieren?");
			builder.setNegativeButton("Nein",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.setPositiveButton("Ja",
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							Intent settingIntent = new Intent(
									Settings.ACTION_LOCATION_SOURCE_SETTINGS);

							startActivityForResult(settingIntent, 100);
						}
					});

			builder.create().show();
		}
	}

	private void onBeenden() {
		// Aktuelle Zeit bestimmen
		Date jetzt = new Date();

		// Zeit ausgeben
		_Endzeit.setText(_DTF.format(jetzt));

		// Leeren Eintrag suchen
		Cursor data = getContentResolver().query(
				ZeitContentProvider.CONTENT_URI,
				new String[] { ZeitTabelle.ID },
				"IFNULL(" + ZeitTabelle.ENDZEIT + ",'')=''", null, null);
		if (data.moveToFirst()) {
			long id = data.getLong(0);

			ContentValues values = new ContentValues();
			values.put(ZeitTabelle.ENDZEIT, ZeitTabelle.SDF.format(jetzt));

			getContentResolver().update(
					Uri.withAppendedPath(ZeitContentProvider.CONTENT_URI,
							String.valueOf(id)), values, null, null);

			// Andere Elemente steuern
			_Start.setEnabled(true);
			_Ende.setEnabled(false);
		}
	}

	/**
	 * Den Zustand prüfen
	 */
	private void pruefeZustand() {
		// ID des offenen Datensatzes finden
		Cursor data = getContentResolver().query(
				ZeitContentProvider.CONTENT_URI,
				new String[] { ZeitTabelle.STARTZEIT },
				"IFNULL(" + ZeitTabelle.ENDZEIT + ",'')=''", null, null);

		// Prüfen, ob dieses vorhanden ist
		if (data.moveToFirst()) {
			String start = data.getString(0);
			// Startzeit auslesen
			Date startZeit;
			try {
				startZeit = ZeitTabelle.SDF.parse(start);

				// Startzeit ausgeben
				_Startzeit.setText(_DTF.format(startZeit));
			} catch (ParseException e) {
				e.printStackTrace();
			}

			// Buttons einrichten
			_Start.setEnabled(false);
			_Ende.setEnabled(true);
		} else {
			_Startzeit.setText("");
			_Start.setEnabled(true);
			_Ende.setEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.opt_liste:
			Intent listIntent = new Intent(this, AuflistungActivity.class);
			this.startActivity(listIntent);

			break;

		case R.id.opt_close:
			this.finish();
			break;

		case R.id.opt_pref:
			Intent prefIntent = new Intent(this, EinstellungenActivity.class);
			startActivity(prefIntent);
			break;

		case R.id.opt_issues:
			Intent issuesIntent = new Intent(this, IssuesActivity.class);
			startActivity(issuesIntent);
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub

	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
}
