package de.mvhs.android.zeiterfassung;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GitHub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class IssueActivityFragment extends Fragment {

    private WebView _webContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_issue, container, false);

        _webContent = (WebView) rootView.findViewById(R.id.WebContent);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_issue, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_load:
                // Laden aus dem Internet
                tryLoadWebContent();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void tryLoadWebContent() {
        ConnectivityManager manager = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo defaultInfo = manager.getActiveNetworkInfo();

        // Spezialisierungen
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (wifiInfo != null && wifiInfo.isConnected()) {
            // Web Inhalt laden
            new WebDownloader().execute();
        } else {
            Toast.makeText(getActivity(),
                    getActivity().getString(R.string.error_no_network),
                    Toast.LENGTH_LONG).show();
        }
    }

    // Background Worker
    private class WebDownloader extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s == null) {
                _webContent.loadData("Keine Daten vorhanden", "text/plain", "UTF-8");
            } else {
                _webContent.loadDataWithBaseURL("fake://not/needed", s, "text/html", "UTF-8", "");
            }
        }

        @Override
        protected String doInBackground(Void... params) {
            String uri = "https://api.github.com/repos/WebDucer-MVHS/kurs-android-II/issues";

            StringBuilder html = new StringBuilder();
            HttpURLConnection connection;

            try {
                URL url = new URL(uri);

                // Vebindung definieren
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000); /* 10 Sekunden für Datenübertragung */
                connection.setConnectTimeout(15000); /* 15 Sekunden für Verbindung */

                // GitHub API Header
                connection.setRequestProperty("Accept", "application/vnd.github.v3.html+json");

                connection.setRequestMethod("GET");
                connection.setDoInput(true);

                // Verbinden
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    return null;
                }

                // Lesen der Daten
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                // Umwandeln der Daten in String
                StringBuilder content = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }

                is.close();
                reader.close();

                html = content;

                // html = parseJsonRaw(content);
                // html = parseWithGson(content);
                html = workWithAPIImplementation();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return html.toString();
        }

        private StringBuilder workWithAPIImplementation() {
            StringBuilder html = new StringBuilder();

            try {
                GitHub gh = GitHub.connectAnonymously();

                List<GHIssue> issues = gh
                        .getRepository("WebDucer-MVHS/kurs-android-II")
                        .getIssues(GHIssueState.OPEN);

                // HTML Header
                html
                        .append("<!DOCTYPE html>")
                        .append("<html>")
                        .append("<head>")
                        .append("<meta charset\"UTF-8\">")
                        .append("<title>GitHub Issues</title>")
                        .append("</head>")
                        .append("<body>");

                // Parsen der Issues
                for (GHIssue issue : issues) {

                    html
                            .append("<h2>")
                            .append("#")
                            .append(issue.getNumber())
                            .append(": ")
                            .append(issue.getTitle())
                            .append("</h2>")
                            .append(issue.getBody());
                }


                // HTML Footer
                html
                        .append("</body>")
                        .append("</html>");

            } catch (IOException e) {
                e.printStackTrace();
            }

            return html;
        }

        private StringBuilder parseWithGson(StringBuilder content) {
            StringBuilder html = new StringBuilder();

            Gson gson = new Gson();
            Issue[] issues = gson.fromJson(content.toString(), new TypeToken<Issue[]>() {
            }.getType());

            // HTML Header
            html
                    .append("<!DOCTYPE html>")
                    .append("<html>")
                    .append("<head>")
                    .append("<meta charset\"UTF-8\">")
                    .append("<title>GitHub Issues</title>")
                    .append("</head>")
                    .append("<body>");

            // Parsen der Issues
            for (Issue issue : issues) {

                html
                        .append("<h2>")
                        .append("#")
                        .append(issue.getNumber())
                        .append(": ")
                        .append(issue.getTitle())
                        .append("</h2>")
                        .append(issue.getHtmlBody());
            }


            // HTML Footer
            html
                    .append("</body>")
                    .append("</html>");

            return html;
        }

        private StringBuilder parseJsonRaw(StringBuilder content) {
            StringBuilder html = new StringBuilder();

            JSONArray issues = null;

            try {
                issues = new JSONArray(content.toString());
                int count = issues.length();

                // HTML Header
                html
                        .append("<!DOCTYPE html>")
                        .append("<html>")
                        .append("<head>")
                        .append("<meta charset\"UTF-8\">")
                        .append("<title>GitHub Issues</title>")
                        .append("</head>")
                        .append("<body>");

                // Parsen der Issues
                for (int i = 0; i < count; i++) {
                    JSONObject issue = issues.getJSONObject(i);

                    html
                            .append("<h2>")
                            .append("#")
                            .append(issue.getInt("number"))
                            .append(": ")
                            .append(issue.getString("title"))
                            .append("</h2>")
                            .append(issue.getString("body_html"));
                }


                // HTML Footer
                html
                        .append("</body>")
                        .append("</html>");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return html;
        }
    }


    public class Issue {
        private String title;
        private int number;
        @SerializedName("body_html")
        private String htmlBody;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public String getHtmlBody() {
            return htmlBody;
        }

        public void setHtmlBody(String htmlBody) {
            this.htmlBody = htmlBody;
        }
    }


}
