package de.mvhs.android.zeiterfassung;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;

public class CSVExporter extends AsyncTask<Cursor, Integer, Void> {
  // Klassenvariablen
  private final String         _FileName;
  private final ProgressDialog _Dialog;

  public CSVExporter(String fileName, ProgressDialog dialog) {
    _FileName = fileName;
    _Dialog = dialog;
  }

  @Override
  protected void onPreExecute() {
    if (_Dialog != null) {
      _Dialog.show();
    }
    super.onPreExecute();
  }

  @Override
  protected void onPostExecute(Void result) {
    if (_Dialog != null) {
      _Dialog.dismiss();
    }
    super.onPostExecute(result);
  }

  @Override
  protected void onCancelled() {
    if (_Dialog != null && _Dialog.isShowing()) {
      _Dialog.dismiss();
    }
    super.onCancelled();
  }

  @Override
  protected void onProgressUpdate(Integer... values) {
    if (_Dialog != null && values != null && values.length > 0) {
      _Dialog.setProgress(values[0]);
    }
    super.onProgressUpdate(values);
  }

  @Override
  protected Void doInBackground(Cursor... params) {
    StringBuilder line = new StringBuilder();

    // Prüfung der, ob Daten vorhanden sind
    if (params != null && params.length == 1 && params[0] != null && !isCancelled()) {
      Cursor data = params[0];

      // Max. für Dialog festlegen
      _Dialog.setMax(data.getCount() + 1);

      // Pfade festlegen
      File exportPath = Environment.getExternalStorageDirectory();
      File exportFile = new File(exportPath, _FileName + ".csv");

      // Prüfen, ob externer Verzeichnis vorhanden ist
      if (exportPath.exists() && !isCancelled()) {
        BufferedWriter writer = null;

        try {

          writer = new BufferedWriter(new FileWriter(exportFile));

          // Auslesen der verfügbaren Spalten
          String[] columnNames = data.getColumnNames();
          for (int i = 0; i < columnNames.length; i++) {
            if (i > 0) {
              line.append(';');
            }
            line.append(columnNames[i]);
          }

          writer.append(line);

          // Fortschritt melden
          publishProgress(1);

          // Cursor vor den ersten Datensatz stellen
          data.moveToPosition(-1);

          while (data.moveToNext() && !isCancelled()) {
            line.delete(0, line.length());

            line.append('\n');

            // Spaltenwerte auslesen
            for (int i = 0; i < columnNames.length; i++) {
              if (i > 0) {
                line.append(';');
              }

              if (data.isNull(i)) {
                // Nichts schreiben
              } else {
                line.append(data.getString(i));
              }
            }

            // Zeile festschreiben
            writer.append(line);
            // Forschritt melden
            publishProgress(data.getPosition() + 2);
          }

        } catch (IOException e) {
          e.printStackTrace();
        } finally {
          if (writer != null) {
            try {
              // Writer freigeben
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
