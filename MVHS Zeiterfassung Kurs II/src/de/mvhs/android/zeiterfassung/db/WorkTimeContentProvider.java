package de.mvhs.android.zeiterfassung.db;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import de.mvhs.android.zeiterfassung.R;

public class WorkTimeContentProvider extends ContentProvider {
  // Authority
  private final static String     _AUTHORITY                         = "de.mvhs.android.worktime.contentprovider.worktime";
  // DB Helper
  private DBHelper                _DBHelper;
  // Uri Matcher
  private final static int        _WORK_TIMES                        = 10;
  private final static int        _WORK_TIME_ITEM                    = 20;
  private final static int        _SELECT_WORK_TIMES                 = 30;
  private final static int        _SELECT_WORK_TIME_ITEM             = 40;
  // Base Path
  private final static String     _BASE_PATH                         = "work_times";
  private final static String     _SELECT_BASE_PATH                  = "work_times/select";
  // Content Uris
  public final static Uri         CONTENT_URI_WORK_TIME              = Uri.parse("content://" + _AUTHORITY + "/" + _BASE_PATH);
  public final static Uri         CONTENT_URI_WORK_TIME_SELECT       = Uri.parse("content://" + _AUTHORITY + "/" + _SELECT_BASE_PATH);
  // Content Type
  public final static String      CONTENT_TYPE_WORK_TIMES            = ContentResolver.CURSOR_DIR_BASE_TYPE + "/work_times";
  public final static String      CONTENT_ITEM_TYPE_WORK_TIME        = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/work_time";
  public final static String      CONTENT_TYPE_SELECT_WORK_TIMES     = ContentResolver.CURSOR_DIR_BASE_TYPE + "/select/work_times";
  public final static String      CONTENT_ITEM_SELECT_TYPE_WORK_TIME = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/select/work_time";
  // Uri Matcher
  private final static UriMatcher _URI_MATCHER                       = new UriMatcher(UriMatcher.NO_MATCH) {
                                                                       {
                                                                         addURI(_AUTHORITY, _BASE_PATH, _WORK_TIMES);
                                                                         addURI(_AUTHORITY, _BASE_PATH + "/#", _WORK_TIME_ITEM);
                                                                         addURI(_AUTHORITY, _SELECT_BASE_PATH, _SELECT_WORK_TIMES);
                                                                         addURI(_AUTHORITY, _SELECT_BASE_PATH + "/#", _SELECT_WORK_TIME_ITEM);
                                                                       }
                                                                     };

  @Override
  public boolean onCreate() {
    // Initialisierung des DB Helpers
    _DBHelper = new DBHelper(getContext());
    return false;
  }

  @Override
  public void shutdown() {
    _DBHelper.close();
    super.shutdown();
  }

  @Override
  public String getType(Uri uri) {
    switch (_URI_MATCHER.match(uri)) {
      case _WORK_TIMES:
        return CONTENT_TYPE_WORK_TIMES;

      case _WORK_TIME_ITEM:
        return CONTENT_ITEM_TYPE_WORK_TIME;

      case _SELECT_WORK_TIMES:
        return CONTENT_TYPE_SELECT_WORK_TIMES;

      case _SELECT_WORK_TIME_ITEM:
        return CONTENT_ITEM_SELECT_TYPE_WORK_TIME;

      default:
        throw new IllegalArgumentException(getContext().getString(R.string.illegal_uri));
    }
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int returnValue = 0;
    switch (_URI_MATCHER.match(uri)) {
      case _WORK_TIMES:
        returnValue = _DBHelper.getWritableDatabase().delete(WorktimeTable.TABLE_NAME, selection, selectionArgs);
        break;

      case _WORK_TIME_ITEM:
        String id = uri.getLastPathSegment();
        String[] temp;
        // Prüfen, ob Bedingung leer ist
        if (selection.isEmpty()) {
          returnValue = _DBHelper.getWritableDatabase().delete(WorktimeTable.TABLE_NAME, WorktimeTable.COLUMN_ID + "=?", new String[] { id });
        }
        // Wnn nicht leer, muss die Bedingung und Paramter erweitert werden
        else {
          if (selectionArgs != null) {
            temp = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
            temp[selectionArgs.length] = id;
          } else {
            temp = new String[] { id };
          }
          returnValue = _DBHelper.getWritableDatabase().delete(WorktimeTable.TABLE_NAME, selection + " AND " + WorktimeTable.COLUMN_ID + "=?", temp);
        }
        break;

      default:
        throw new IllegalArgumentException(getContext().getString(R.string.illegal_uri));
    }

    getContext().getContentResolver().notifyChange(uri, null);

    return returnValue;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    Uri returnValue = null;
    long id = -1;
    switch (_URI_MATCHER.match(uri)) {
      case _WORK_TIMES:
        id = _DBHelper.getWritableDatabase().insert(WorktimeTable.TABLE_NAME, null, values);
        returnValue = Uri.withAppendedPath(uri, String.valueOf(id));
        break;

      default:
        throw new IllegalArgumentException(getContext().getString(R.string.illegal_uri));
    }

    getContext().getContentResolver().notifyChange(uri, null);

    return returnValue;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    // Überprüfen, ob alle Spalten zugelassen sind
    checkColumns(uri, projection);

    Cursor returnValue = null;
    String id = uri.getLastPathSegment();
    String[] temp;

    switch (_URI_MATCHER.match(uri)) {
      case _WORK_TIMES:
        returnValue = _DBHelper.getReadableDatabase().query(WorktimeTable.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        break;

      case _WORK_TIME_ITEM:
        // Prüfen, ob die Bedingung leer ist
        if (selection.isEmpty()) {
          returnValue = _DBHelper.getReadableDatabase().query(WorktimeTable.TABLE_NAME, projection, WorktimeTable.COLUMN_ID + "=?",
              new String[] { id }, null, null, sortOrder);
        }
        // Wenn nicht leer, muss die Bedingung und Parameter erweitert werden
        else {
          if (selectionArgs != null) {
            temp = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
            temp[selectionArgs.length] = id;
          } else {
            temp = new String[] { id };
          }
          returnValue = _DBHelper.getReadableDatabase().query(WorktimeTable.TABLE_NAME, projection,
              selection + " AND " + WorktimeTable.COLUMN_ID + "=?", temp, null, null, sortOrder);
        }
        break;

      case _SELECT_WORK_TIMES:
        returnValue = _DBHelper.getReadableDatabase().query("view_" + WorktimeTable.TABLE_NAME, projection, selection, selectionArgs, null, null,
            sortOrder);
        break;

      case _SELECT_WORK_TIME_ITEM:
        if (selection.isEmpty()) {
          returnValue = _DBHelper.getReadableDatabase().query("view_" + WorktimeTable.TABLE_NAME, projection, WorktimeTable.COLUMN_ID + "=?",
              new String[] { id }, null, null, sortOrder);
        } else {
          if (selectionArgs != null) {
            temp = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
            temp[selectionArgs.length] = id;
          } else {
            temp = new String[] { id };
          }
          returnValue = _DBHelper.getReadableDatabase().query("view_" + WorktimeTable.TABLE_NAME, projection,
              selection + " AND " + WorktimeTable.COLUMN_ID + "=?", temp, null, null, sortOrder);
        }
        break;

      default:
        throw new IllegalArgumentException(getContext().getString(R.string.illegal_uri));
    }

    if (returnValue != null) {
      returnValue.setNotificationUri(getContext().getContentResolver(), uri);
    }

    return returnValue;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    int returnValue = 0;
    switch (_URI_MATCHER.match(uri)) {
      case _WORK_TIMES:
        returnValue = _DBHelper.getWritableDatabase().update(WorktimeTable.TABLE_NAME, values, selection, selectionArgs);
        break;

      case _WORK_TIME_ITEM:
        String id = uri.getLastPathSegment();
        String[] temp;
        if (selection.isEmpty()) {
          returnValue = _DBHelper.getWritableDatabase().update(WorktimeTable.TABLE_NAME, values, WorktimeTable.COLUMN_ID + "=?", new String[] { id });
        } else {
          if (selectionArgs != null) {
            temp = Arrays.copyOf(selectionArgs, selectionArgs.length + 1);
            temp[selectionArgs.length] = id;
          } else {
            temp = new String[] { id };
          }
          returnValue = _DBHelper.getWritableDatabase().update(WorktimeTable.TABLE_NAME, values,
              selection + " AND " + WorktimeTable.COLUMN_ID + "=?", temp);
        }
        break;

      default:
        throw new IllegalArgumentException(getContext().getString(R.string.illegal_uri));
    }

    getContext().getContentResolver().notifyChange(uri, null);

    return returnValue;
  }

  private void checkColumns(Uri uri, String[] projection) {
    String[] available;
    switch (_URI_MATCHER.match(uri)) {
      case _WORK_TIMES:
      case _WORK_TIME_ITEM:
        available = new String[] { WorktimeTable.COLUMN_ID, WorktimeTable.COLUMN_START_TIME, WorktimeTable.COLUMN_END_TIME };
        break;

      case _SELECT_WORK_TIMES:
      case _SELECT_WORK_TIME_ITEM:
        available = new String[] { WorktimeTable.COLUMN_ID, WorktimeTable.COLUMN_SELECT_START_DATE, WorktimeTable.COLUMN_SELECT_START_TIME,
            WorktimeTable.COLUMN_SELECT_END_DATE, WorktimeTable.COLUMN_SELECT_END_TIME, WorktimeTable.COLUMN_SELECT_WORK_TIME };
        break;

      default:
        available = null;
        break;
    }
    if (projection != null) {
      HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
      HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
      // Check if all columns which are requested are available
      if (!availableColumns.containsAll(requestedColumns)) {
        throw new IllegalArgumentException("Unknown columns in projection");
      }
    }
  }
}
