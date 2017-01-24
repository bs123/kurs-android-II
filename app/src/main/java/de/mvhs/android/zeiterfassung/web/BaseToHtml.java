package de.mvhs.android.zeiterfassung.web;

/**
 * Created by eugen on 24.01.17.
 */

public class BaseToHtml {
    public static StringBuilder getHtmlHeader() {
        StringBuilder header = new StringBuilder();

        header
                .append("<!DOCTYPE html>")
                .append("<html>")
                .append("<head>")
                .append("<meta charset\"UTF-8\">")
                .append("<title>GitHub Issues</title>")
                .append("</head>")
                .append("<body>");

        return header;
    }

    public static StringBuilder getHtmlFooter() {
        StringBuilder footer = new StringBuilder();

        footer
                .append("</body>")
                .append("</html>");

        return footer;
    }
}
