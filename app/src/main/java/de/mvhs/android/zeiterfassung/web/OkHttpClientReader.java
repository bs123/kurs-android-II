package de.mvhs.android.zeiterfassung.web;

import android.os.AsyncTask;
import android.webkit.WebView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by eugen on 24.01.17.
 */

public class OkHttpClientReader extends AsyncTask<String, Void, String> {
    private WebView _webView;

    public OkHttpClientReader(WebView webView) {

        _webView = webView;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        _webView.loadData(s, "text/html", "UTF-8");
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder html = new StringBuilder();

        OkHttpClient client = new OkHttpClient();

        // Synchrones lesen
        Request request = new Request.Builder()
                .url(params[0])
                .addHeader("Accept", "application/vnd.github.v3.html+json")
                .build();

        try {
            Response response = client.newCall(request).execute();

            // Prüfen auf den erfolgreichen Zugriff
            if (response.isSuccessful()) {
                html.append(response.body().string());
                html = RawJsonToHtml.convert(html);
            }

        } catch (IOException e) {
            e.printStackTrace();
            html.append(e);
        }

        return html.toString();
    }
}
