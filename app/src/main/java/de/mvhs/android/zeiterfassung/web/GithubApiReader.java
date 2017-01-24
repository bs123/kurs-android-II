package de.mvhs.android.zeiterfassung.web;

import android.os.AsyncTask;
import android.webkit.WebView;

import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;

/**
 * Created by eugen on 24.01.17.
 */

public class GithubApiReader extends AsyncTask<String, Void, String> {
    private WebView _webView;

    public GithubApiReader(WebView webView){

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
        try {
            // Verbinden
            GitHub gitHub = GitHub.connectAnonymously();

            List<GHIssue> issues = gitHub
                    .getRepository("WebDucer-MVHS/kurs-android-II")
                    .getIssues(GHIssueState.ALL);

            // HTML Header aufbauen
            html.append(BaseToHtml.getHtmlHeader());

            // Issues verarbeiten
            for (GHIssue issue : issues) {
                html
                        .append("<h2>#")
                        .append(issue.getNumber())
                        .append(": ")
                        .append(issue.getTitle())
                        .append("</h2>")
                        .append("<h4>")
                        .append(issue.getState())
                        .append("</h4>")
                        .append(issue.getBody());
            }

            // HTML Footer aufbauen
            html.append(BaseToHtml.getHtmlFooter());

        } catch (IOException e) {
            e.printStackTrace();
            html.append(e);
        }

        return html.toString();
    }
}
