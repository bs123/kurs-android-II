package de.mvhs.android.zeiterfassung.web;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by eugen on 24.01.17.
 */

public class RawJsonToHtml {
    public static StringBuilder convert(StringBuilder content) {
        StringBuilder html = new StringBuilder();

        JSONArray issues = null;

        try {
            // Auslesen der ersten Ebene - Liste von Issues
            issues = new JSONArray(content.toString());
            int count = issues.length();

            // Aufbau des HTML Headers
            html.append(BaseToHtml.getHtmlHeader());

            // Parsen der einzelnen Issues
            for (int i = 0; i < count; i++) {
                JSONObject issue = issues.getJSONObject(i);

                // Überschrift erzeugen
                html
                        .append("<h2>#")
                        .append(issue.getInt("number")) // Auslesen der Nummer des Issues
                        .append(": ")
                        .append(issue.getString("title")) // Auslesen der Überschrift
                        .append("</h2>");

                // Status erzeugen
                html
                        .append("<h4>")
                        .append(issue.getString("state"))
                        .append("</h4>");

                // Inhalt des Issues erzeugen
                html.append(issue.getString("body_html"));
            }

            // HTML Footer
            html.append(BaseToHtml.getHtmlFooter());
        } catch (JSONException e) {
            e.printStackTrace();
            html.append(e);
        }

        return html;
    }
}
