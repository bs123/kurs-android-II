package de.mvhs.android.zeiterfassung.db;

import java.text.SimpleDateFormat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
  /*
   * Versionsgeschichte
   * ==================
   * 1: Initial DB Entwurf
   * 2: Erweiterung um ein View für bessere UI-Darstellung
   * 3: Erweiterung der tabelle um Kontakte, GPS und Bild Spalten
   */
  // Variablen
  private static final String          DATABASE_NAME    = "database.db";
  private static final int             DATABASE_VERSION = 3;
  /**
   * Formater für die Formatierung und Parsen des Datums aus und in die Datenbank
   */
  public static final SimpleDateFormat DB_DATE_FORMAT   = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

  /**
   * Standard-Constructor
   * 
   * @param context
   *          App Context
   */
  public DBHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    // Erstellen der Tabelle
    db.execSQL(WorktimeTable.CREATE_TABLE);
    // Erstellen des Views
    db.execSQL(WorktimeTable.CREATE_VIEW);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Löschen der Tabelle
    db.execSQL(WorktimeTable.DROP_TABLE);
    // Löschen des Views
    db.execSQL(WorktimeTable.DROP_VIEW);
    onCreate(db);
  }

}
