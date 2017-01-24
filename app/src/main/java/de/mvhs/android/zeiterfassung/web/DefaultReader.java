package de.mvhs.android.zeiterfassung.web;

import android.os.AsyncTask;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by eugen on 24.01.17.
 */

public class DefaultReader extends AsyncTask<String, Void, String> {
    private WebView _webView;

    public DefaultReader(WebView webView){

        _webView = webView;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        _webView.loadData(s, "text/html", "UTF-8");
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder output = new StringBuilder();

        try {
            // Verbindung definieren
            URL url = new URL(params[0]);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setReadTimeout(10000); // 10 Sekunden zum Lesen
            connection.setConnectTimeout(15000); // 15 Sekunden für die Verbindung

            // Github Header
            connection.setRequestProperty("Accept", "application/vnd.github.v3.html+json");
            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            // Verbinden
            connection.connect();

            // Antwort prüfen
            int httpCode = connection.getResponseCode();
            if(httpCode != 200){
                return "Seite antwortet mit dem Code: " + httpCode;
            }

            // Laden der Daten
            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            // Auslesen der Daten als String
            String line = null;
            while((line = reader.readLine()) != null){
                output.append(line);
            }

            // Resourcen freigeben
            reader.close();
            is.close();

            // JSON händisch auslesen
            //output = RawJsonToHtml.convert(output);

            // Auslesen mit GSON
            output = GsonToHtml.convert(output);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return e.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return e.toString();
        }

        return output.toString();
    }
}
