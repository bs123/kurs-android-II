package de.mvhs.android.zeiterfassung.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
	// Konstanten
	private final static String DB_NAME = "zeiterfassung1b.db";
	private final static int DB_VERSION = 2;

	public DBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		ZeitTabelle.CreateTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		ZeitTabelle.UpdateTable(db, oldVersion, newVersion);
	}

}
