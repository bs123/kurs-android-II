package de.mvhs.android.zeiterfassung.db;

import android.database.sqlite.SQLiteDatabase;

public class ZeitTable {
	private final static String _CREATE_TABLE = "CREATE TABLE zeit ("
			+ "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
			+ "start_time TEXT NOT NULL," + "end_time TEXT,"
			+ "pause INTEGER DEFAULT 0," + "comment TEXT," + "lat TEXT,"
			+ "lan TEXT" + ")";

	// Generierung von SQL-Scripts mit SQLite Manger (Firefox)
	private final static String _UPGRADE_1_TO_2 = "ALTER TABLE [zeit] ADD COLUMN [pause] INTEGER NOT NULL  DEFAULT 0;";
	private final static String _UPGRADE_2_TO_3 = "ALTER TABLE [zeit] ADD COLUMN [comment] TEXT;";
	private final static String _UPGRADE_3_TO_4_1 = "ALTER TABLE [zeit] ADD COLUMN [lat] TEXT;";
	private final static String _UPGRADE_3_TO_4_2 = "ALTER TABLE [zeit] ADD COLUMN [lan] TEXT;";

	/**
	 * ID für die Auflistung URI im Content Provider
	 */
	public final static int ITEMS = 100;
	/**
	 * ID für einen einzelnen Datensatz in Content Proveder
	 */
	public final static int ITEM_ID = 105;

	/**
	 * Name der Tabelle in der Datenbank
	 */
	public final static String TABLE_NAME = "zeit";

	public static void onCreate(SQLiteDatabase db) {
		// Initialisierung der Tabelle (Erstellung)
		db.execSQL(_CREATE_TABLE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		// Migration der Tabelle
		switch (oldVersion) {
		case 1:
			db.execSQL(_UPGRADE_1_TO_2);

		case 2:
			db.execSQL(_UPGRADE_2_TO_3);

		case 3:
			db.execSQL(_UPGRADE_3_TO_4_1);
			db.execSQL(_UPGRADE_3_TO_4_2);

		default:
			break;
		}
	}
}
