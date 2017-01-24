package de.mvhs.android.zeiterfassung.web;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

/**
 * Created by eugen on 24.01.17.
 */

public class GsonToHtml {
    public static StringBuilder convert(StringBuilder content) {
        StringBuilder html = new StringBuilder();

        Gson gson = new Gson();
        Issue[] issues = gson.fromJson(content.toString(), new TypeToken<Issue[]>() {
        }.getType());

        // HTML Header
        html.append(BaseToHtml.getHtmlHeader());

        // Parsen der Issues
        for (Issue issue : issues) {
            // Überschrift erzeugen
            html
                    .append("<h2>#")
                    .append(issue.getNumber())
                    .append(": ")
                    .append(issue.getTitle())
                    .append("</h2>");

            // Status erzeugen
            html
                    .append("<h4>")
                    .append(issue.getState())
                    .append("</h4>");

            // Inhalt erzeugen
            html
                    .append(issue.getHtmlBody());
        }


        // HTML Footer
        html.append(BaseToHtml.getHtmlFooter());

        return html;
    }
}

