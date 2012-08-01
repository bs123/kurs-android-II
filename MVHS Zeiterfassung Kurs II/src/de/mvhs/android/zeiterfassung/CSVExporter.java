package de.mvhs.android.zeiterfassung;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

public class CSVExporter extends AsyncTask<Cursor, Integer, Void> {
  // Klassenvariablen
  private final String         _ExportFileName;
  private final ProgressDialog _Progress;
  private final String         _ToEmail;
  private final Context        _Context;
  private File                 _ExportFile;

  public CSVExporter(String exportFileName, ProgressDialog dialog, String toEmail, Context context) {
    _ExportFileName = exportFileName;
    _Progress = dialog;
    _ToEmail = toEmail;
    _Context = context;
  }

  @Override
  protected void onCancelled() {
    if (_Progress != null && _Progress.isShowing()) {
      _Progress.dismiss();
    }
    super.onCancelled();
  }

  @Override
  protected void onPreExecute() {
    if (_Progress != null) {
      _Progress.show();
    }
    super.onPreExecute();
  }

  @Override
  protected void onPostExecute(Void result) {
    if (_Progress != null && _Progress.isShowing()) {
      _Progress.dismiss();
    }
    if (_ToEmail != null && !_ToEmail.isEmpty() && _ExportFile != null && _ExportFile.exists()) {
      Intent sendEmail = new Intent(Intent.ACTION_SEND);
      sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[] { _ToEmail });
      sendEmail.putExtra(Intent.EXTRA_SUBJECT, "Arbeitszeiterfassung");
      sendEmail.setType("text/plain");
      sendEmail.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + _ExportFile));
      _Context.startActivity(sendEmail);
    }
    super.onPostExecute(result);
  }

  @Override
  protected void onProgressUpdate(Integer... values) {
    if (values != null && values.length > 0) {
      _Progress.setProgress(values[0]);
    }
    super.onProgressUpdate(values);
  }

  @Override
  protected Void doInBackground(Cursor... data) {
    // Variablen
    StringBuilder line = new StringBuilder();

    // Prüfung, ob Daten da sind
    if (data != null && data.length == 1 && data[0] != null) {

      Cursor exportData = data[0];

      // Fortschrits-Maximum setzen
      _Progress.setMax(exportData.getCount() + 1);

      File exportPath = Environment.getExternalStorageDirectory();
      _ExportFile = new File(exportPath, _ExportFileName);
      exportPath.mkdirs();

      if (exportPath.exists()) {
        BufferedWriter writer = null;
        try {
          writer = new BufferedWriter(new FileWriter(_ExportFile));

          // Auslesen der Spaltennamen aus dem Cursor
          String[] columnNames = exportData.getColumnNames();
          for (String column : columnNames) {
            if (line.length() > 0) {
              line.append(";");
            }
            line.append(column);
          }

          // Entfernen des Semikolons
          line.append("\n");

          // Speichern in die Datei
          writer.append(line);

          // Aktualisieren des Fortschritts
          publishProgress(new Integer[] { 1 });

          // Vor dem ersten Eintrag positionieren
          exportData.moveToPosition(-1);
          while (exportData.moveToNext() && isCancelled() == false) {
            // Löschen des Zeilen-Platzhalters
            line.delete(0, line.length());

            for (int i = 0; i < columnNames.length; i++) {
              if (line.length() > 0) {
                line.append(";");
              }

              if (exportData.isNull(i)) {
                // Nichts Schreiben
              } else {
                // Wert der Spalte schreiben
                line.append(exportData.getString(i));
              }
            }

            line.append("\n");

            writer.append(line);

            // Fortschritt melden
            publishProgress(new Integer[] { exportData.getPosition() + 2 });
          }

        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          // Schließen der Datei
          if (writer != null) {
            try {
              writer.flush();
              writer.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }

    return null;
  }
}
