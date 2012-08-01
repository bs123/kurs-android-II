package de.mvhs.android.zeiterfassung.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class DownloadService extends IntentService {
  // Variablen
  /**
   * Name des Services
   */
  public final static String SERVICE_NAME = "DownloadFileService";
  private int                result       = Activity.RESULT_CANCELED;

  public DownloadService() {
    super(SERVICE_NAME);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    // Pfad zur Datei, in die gespeichert werden soll
    Uri data = intent.getData();
    // Pfad zum auslesen der Daten
    String urlPath = intent.getStringExtra("urlpath");

    File output = new File(data.getPath());
    // Löschen der Datei, falls diese existiert
    if (output.exists()) {
      output.delete();
    }

    // Ordner anlegen, falls dieser noch nicht existiert
    File outputDir = new File(output.getParent());
    outputDir.mkdirs();

    InputStream stream = null;
    FileOutputStream fos = null;
    try {
      URL url = new URL(urlPath);
      // Aufbauen der Verbindung
      HttpURLConnection request = (HttpURLConnection) url.openConnection();
      request.setInstanceFollowRedirects(true);
      request.connect();
      stream = request.getInputStream();
      // Daten in den Reader laden
      BufferedInputStream reader = new BufferedInputStream(stream);
      // Writer initialisieren
      fos = new FileOutputStream(output.getPath());
      int next = -1;
      while ((next = reader.read()) != -1) {
        fos.write(next);
      }
      // Erfolgsmeldung, wenn Download abgeschlossen wurde
      result = Activity.RESULT_OK;

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if (fos != null) {
        try {
          fos.flush();
          fos.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    // Senden der Erfolgsmeldung (URI zur heruntergeladenen Datei)
    Bundle extras = intent.getExtras();
    if (extras != null) {
      Messenger messenger = (Messenger) extras.get("MESSENGER");
      Message msg = Message.obtain();
      msg.arg1 = result;
      msg.obj = output.getAbsolutePath();
      try {
        messenger.send(msg);
      } catch (android.os.RemoteException e1) {
        Log.w(getClass().getName(), "Exception sending message", e1);
      }

    }
  }

}
