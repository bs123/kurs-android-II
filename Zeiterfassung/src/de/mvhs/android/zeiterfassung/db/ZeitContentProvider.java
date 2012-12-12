package de.mvhs.android.zeiterfassung.db;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class ZeitContentProvider extends ContentProvider {
  // Klassen Variablen
  private final static String     _AUTHORITY          = "de.mvhs.android.zeiterfassung.cp";
  private DBHelper                _DBHelper;

  // URI Matches
  private final static int        _ZEITEN             = 10;
  private final static int        _ZEIT               = 20;

  // Basisordner der Tabelle
  private final static String     _BASE_PATH          = "zeit";

  // Content URI
  public final static Uri         CONTENT_URI         = Uri.parse("content://" + _AUTHORITY + "/" + _BASE_PATH);

  // Content Typen
  public final static String      CONTENT_TYPE_ZEITEN = ContentResolver.CURSOR_DIR_BASE_TYPE + "/zeit";
  public final static String      CONTENT_TYPE_ZEIT   = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/zeit";

  // Initialisierung des URI Matchers
  private final static UriMatcher _URI_MATCHER        = new UriMatcher(UriMatcher.NO_MATCH) {
                                                        {
                                                          addURI(_AUTHORITY, _BASE_PATH, _ZEITEN);
                                                          addURI(_AUTHORITY, _BASE_PATH + "/#", _ZEIT);
                                                        }
                                                      };

  @Override
  public boolean onCreate() {
    // Initialisierung des DB Helpers
    _DBHelper = new DBHelper(getContext());

    return true;
  }

  @Override
  public void shutdown() {
    if (_DBHelper != null) {
      _DBHelper.close();
    }
    super.shutdown();
  }

  @Override
  public String getType(Uri uri) {
    switch (_URI_MATCHER.match(uri)) {
      case _ZEITEN:
        return CONTENT_TYPE_ZEITEN;

      case _ZEIT:
        return CONTENT_TYPE_ZEIT;

      default:
        throw new IllegalArgumentException("Diese URI wird nicht unterstützt!");
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    Uri returnValue = null;
    long id = -1;
    switch (_URI_MATCHER.match(uri)) {
      case _ZEITEN:
        id = _DBHelper.getWritableDatabase().insert(ZeitTabelle._TABLE_NAME, null, values);
        returnValue = Uri.withAppendedPath(CONTENT_URI, String.valueOf(id));
        break;

      default:
        throw new IllegalArgumentException("Diese URI wird nicht unterstützt!");
    }

    getContext().getContentResolver().notifyChange(returnValue, null);

    return returnValue;
  }

  @Override
  public int update(Uri uri, ContentValues values, String where, String[] whereParams) {
    int returnValue = 0;
    switch (_URI_MATCHER.match(uri)) {
      case _ZEITEN:
        returnValue = _DBHelper.getWritableDatabase().update(ZeitTabelle._TABLE_NAME, values, where, whereParams);
        break;

      case _ZEIT:
        String id = uri.getLastPathSegment();
        if (where == null || where.isEmpty()) {
          returnValue = _DBHelper.getWritableDatabase().update(ZeitTabelle._TABLE_NAME, values, ZeitTabelle.ID + "=" + id, null);
        } else {
          String newWhere = where + " AND " + ZeitTabelle.ID + "=" + id;

          returnValue = _DBHelper.getWritableDatabase().update(ZeitTabelle._TABLE_NAME, values, newWhere, whereParams);
        }
        break;

      default:
        throw new IllegalArgumentException("Diese URI wird nicht unterstützt!");
    }

    getContext().getContentResolver().notifyChange(uri, null);

    return returnValue;
  }

  @Override
  public int delete(Uri uri, String where, String[] whereParams) {
    int returnValue = 0;
    switch (_URI_MATCHER.match(uri)) {
      case _ZEITEN:
        returnValue = _DBHelper.getWritableDatabase().delete(ZeitTabelle._TABLE_NAME, where, whereParams);
        break;

      case _ZEIT:
        String id = uri.getLastPathSegment();
        // Prüfen, ob Bedingung leer ist
        if (where == null || where.isEmpty()) {
          returnValue = _DBHelper.getWritableDatabase().delete(ZeitTabelle._TABLE_NAME, ZeitTabelle.ID + "=" + id, null);
        }
        // Wnn nicht leer, muss die Bedingung und Paramter erweitert werden
        else {
          String newWhere = where + " AND " + ZeitTabelle.ID + "=" + id;

          returnValue = _DBHelper.getWritableDatabase().delete(ZeitTabelle._TABLE_NAME, newWhere, whereParams);
        }
        break;

      default:
        throw new IllegalArgumentException("Diese URI wird nicht unterstützt!");
    }

    getContext().getContentResolver().notifyChange(uri, null);

    return returnValue;
  }

  @Override
  public Cursor query(Uri uri, String[] select, String where, String[] whereParams, String sortOrder) {
    Cursor returnValue = null;

    String id = uri.getLastPathSegment();

    switch (_URI_MATCHER.match(uri)) {
      case _ZEITEN:
        returnValue = _DBHelper.getReadableDatabase().query(ZeitTabelle._TABLE_NAME, // Tabellenname
                select, // Spalten
                where, // Bedingung
                whereParams, // Parameter für die Bedingung
                null, // Gruppierung
                null, // Having
                sortOrder); // Sortierung
        break;

      case _ZEIT:
        if (where == null || where.isEmpty()) {
          returnValue = _DBHelper.getReadableDatabase().query(ZeitTabelle._TABLE_NAME, select, ZeitTabelle.ID + "=" + id, null, null, null, null);
        } else {
          String newWhere = where + " AND " + ZeitTabelle.ID + "=" + id;

          returnValue = _DBHelper.getReadableDatabase().query(ZeitTabelle._TABLE_NAME, select, newWhere, whereParams, null, null, null);
        }

        break;
      default:
        throw new IllegalArgumentException("Diese URI wird nicht unterstützt!");
    }

    return returnValue;
  }

}
