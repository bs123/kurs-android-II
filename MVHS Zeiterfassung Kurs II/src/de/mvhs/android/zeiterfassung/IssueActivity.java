package de.mvhs.android.zeiterfassung;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

public class IssueActivity extends Activity {
  private WebView     _ContentView = null;
  private ProgressBar _Progress    = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.info_activity);

    _ContentView = (WebView) findViewById(R.id.issue_view);
    _Progress = (ProgressBar) findViewById(R.id.progress);
  }

  @Override
  protected void onStart() {
    super.onStart();

    init();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    this.getActionBar().setHomeButtonEnabled(true);
    this.getActionBar().setDisplayHomeAsUpEnabled(true);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    // Zurückkehren zum Startbildschirm
      case android.R.id.home:
        Intent homeIntent = new Intent(this, MainActivity.class);
        this.startActivity(homeIntent);
        break;

      default:
        break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void init() {
    // Prüfung, ob Internetverbindung da ist
    ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

    if (networkInfo != null && networkInfo.isConnected()) {
      // Ausführen der Abfrage
      GitHubLoader loader = new GitHubLoader();
      loader.execute(new String[] { "https://api.github.com/repos/WebDucer/MVHS-Android-II/issues" });
    } else {
      _ContentView.setVisibility(View.VISIBLE);
      _Progress.setVisibility(View.GONE);
      _ContentView.loadData("Keine Internet-Vrbindung!", "text/plain", "UTF-8");
    }
  }

  private class GitHubLoader extends AsyncTask<String, Integer, String> {
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      _ContentView.setVisibility(View.GONE);
      _Progress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(String result) {
      super.onPostExecute(result);
      _ContentView.setVisibility(View.VISIBLE);
      _Progress.setVisibility(View.GONE);
      if (result != null && result.length() > 0) {
        _ContentView.loadDataWithBaseURL("fake://not/needed", result, "text/html", "UTF-8", ""); // (result, "text/plain", "UTF-8");
      } else {
        _ContentView.loadData("Keine Daten vorhanden!", "text/plain", "UTF-8");
      }
    }

    @Override
    protected String doInBackground(String... params) {
      // Variable für den Inhalt der Anfrage-Antwort
      StringBuilder builder = new StringBuilder();
      StringBuilder html = new StringBuilder();
      URL url;
      try {
        // Anfrage URL
        url = new URL(params[0]);

        // Erstellen der Verbindung
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        // Setzen der Parameter
        request.setReadTimeout(10000 /* milliseconds */);
        request.setConnectTimeout(15000 /* milliseconds */);
        // API Einstelleung, dass die Daten in JSON, MarkDown, Text und HTML kommen
        request.setRequestProperty("Accept", "application/vnd.github.v3.html+json");
        // Anfrage Typ
        request.setRequestMethod("GET");
        request.setDoInput(true);
        // Starten der Anfrage
        request.connect();
        int responseCode = request.getResponseCode();

        // Prüfen, ob Anfrage korrekt verarbeitet wurde
        if (responseCode == 200) {
          // Auslesen der Antwort
          InputStream content = request.getInputStream();
          BufferedReader reader = new BufferedReader(new InputStreamReader(content, "UTF-8"));
          String line;
          // Umwandlung der Anfrage in ein String
          while ((line = reader.readLine()) != null) {
            builder.append(line);
          }

          // Parsen der JSON-Antwort
          JSONArray issues = new JSONArray(builder.toString());
          int length = issues.length();
          html.append("<!DOCTYPE html>").append("<html>").append("<head>").append("<meta charset=\"UTF-8\">").append("<title>GitHub Issues</title>")
              .append("</head>").append("<body>");

          html.append("<h1>").append(length).append(" Issue(s)</h1>");

          for (int i = 0; i < length; i++) {
            JSONObject issue = issues.getJSONObject(i);
            html.append("<h2>").append("#").append(issue.getInt("number")).append(": ").append(issue.getString("title")).append("</h2>")
                .append(issue.getString("body_html"));
          }

          html.append("</dl>").append("</body>").append("</html>");

          // Schließen der Verbindungen
          content.close();
          reader.close();
          request.disconnect();
        } else {
          Log.e("HTTP", "Verbindung zum Server " + params[0] + " ist gescheitert!");
        }
      } catch (MalformedURLException e) {
        e.printStackTrace();
      } catch (ClientProtocolException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (JSONException e) {
        e.printStackTrace();
      }

      return html.toString();
    }

  }
}
