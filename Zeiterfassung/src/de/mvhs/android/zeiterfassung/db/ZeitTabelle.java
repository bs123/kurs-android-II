package de.mvhs.android.zeiterfassung.db;

import java.text.SimpleDateFormat;

import android.database.sqlite.SQLiteDatabase;

public class ZeitTabelle {
	// Konstanten
	private final static String _CREATE_TABLE = "CREATE TABLE zeit (_id INTEGER PRIMARY KEY  AUTOINCREMENT NOT NULL,"
			+ "startzeit TEXT NOT NULL," + "endzeit TEXT, kontakt TEXT)";

	private final static String _DROP_TABLE = "DROP TABLE IF EXISTS zeit";

	private final static String _UPDATE_V1_V2 = "ALTER TABLE zeit ADD COLUMN kontakt TEXT";

	public final static SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm");

	// Tabellendefinition
	protected final static String _TABLE_NAME = "zeit";
	/**
	 * ID Spalte
	 */
	public static final String ID = "_id";
	/**
	 * Spaltenname für die Startzeit
	 */
	public final static String STARTZEIT = "startzeit";
	/**
	 * Spaltenname für die Endzeit
	 */
	public final static String ENDZEIT = "endzeit";
	/**
	 * Spaltenname für den zugeordneten Kontakt (Anzeigename)
	 */
	public final static String KONTAKT = "kontakt";

	/**
	 * Erstellen der neuen Tabelle
	 * 
	 * @param db
	 *            Datenbank Instanz
	 */
	public static void CreateTable(SQLiteDatabase db) {
		db.execSQL(_CREATE_TABLE);
	}

	/**
	 * Löschen der Tabelle
	 * 
	 * @param db
	 *            Datenbank Instanz
	 */
	public static void DropTable(SQLiteDatabase db) {
		db.execSQL(_DROP_TABLE);
	}

	public static void UpdateTable(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		switch (oldVersion) {
		case 1:
			db.execSQL(_UPDATE_V1_V2);

		case 2:
			// Update von V2 auf V3
			break;

		default:
			break;
		}
	}
}
