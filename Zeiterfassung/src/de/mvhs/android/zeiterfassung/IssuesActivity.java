package de.mvhs.android.zeiterfassung;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockActivity;

public class IssuesActivity extends SherlockActivity {
	// Klassenvariablen
	private WebView _ContentView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_issues);

		// Initialisierung der Elemente
		_ContentView = (WebView) findViewById(R.id.issue_view);
	}

	@Override
	protected void onStart() {
		super.onStart();
		loadData();
	}

	private void loadData() {
		ConnectivityManager connMng = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = connMng.getActiveNetworkInfo();

		if (netInfo != null && netInfo.isConnected()) {
			// Daten laden
			StringBuilder builder = new StringBuilder();
			StringBuilder html = new StringBuilder();
			URL url = null;

			try {
				// Anfrage URL
				url = new URL(
						"https://api.github.com/repos/WebDucer-MVHS/kurs-android-II/issues");

				// Erstellen der Verbindung
				HttpURLConnection request = (HttpURLConnection) url
						.openConnection();

				// Setzen der Parameter
				request.setReadTimeout(10000 /* Millisekunden */);
				request.setConnectTimeout(15000);

				// API Einstellung, dass Daten als JSON, und HTML kommen
				request.setRequestProperty("Accept",
						"application/vnd.github.v3.html+json");

				// Anfragetyp setzen
				request.setRequestMethod("GET");
				request.setDoInput(true);

				// Anfrage starten
				request.connect();

				// Antwort-Code
				int resposeCode = request.getResponseCode();

				// Prüfen, ob Abfrage erfolgreich war
				if (resposeCode == 200) {
					// Auslesen der Daten
					InputStream content = request.getInputStream();
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(content, "UTF-8"));

					String line = null;
					while ((line = reader.readLine()) != null) {
						builder.append(line);
					}

					// Parsen der JSON-Antwort
					JSONArray issues = new JSONArray(builder.toString());
					int length = issues.length();

					// HTML Kopf aufbauen
					html.append("<!DOCTYPE html")
							.append("<html><head><meta charset=\"UTF-8\">")
							.append("<title>Bekannte Fehler</title>")
							.append("</head><body>");

					// Überschrift hinzufügen
					html.append("<h1>").append("Issues").append("</h1");

					// Issue-Objekte verarbeiten
					for (int i = 0; i < length; i++) {
						JSONObject issue = issues.getJSONObject(i);

						// Objekt rendern
						html.append("<h2>#").append(issue.getInt("number"))
								.append(": ").append(issue.getString("title"))
								.append("</h2>");

						html.append(issue.getString("body_html"));
					}

					// HTML abschließen
					html.append("</body></html>");

					_ContentView.loadDataWithBaseURL("fake://not/needed",
							html.toString(), "text/html", "UTF-8", "");
				}

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			_ContentView.loadData("Keine Internetverbindung!!!", "text/plain",
					"UTF-8");
		}
	}
	/*
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */
}
