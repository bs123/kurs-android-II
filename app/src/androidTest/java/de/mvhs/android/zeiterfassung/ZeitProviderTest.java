package de.mvhs.android.zeiterfassung;

import android.content.ContentUris;
import android.content.ContentValues;
import android.net.Uri;
import android.test.ProviderTestCase2;

import de.mvhs.android.zeiterfassung.db.ZeitContract;
import de.mvhs.android.zeiterfassung.db.ZeitenProvider;

/**
 * Created by kurs on 24.06.15.
 */
public class ZeitProviderTest extends ProviderTestCase2<ZeitenProvider> {
    /**
     * Constructor.
     */
    public ZeitProviderTest() {
        super(ZeitenProvider.class, ZeitContract.AUTHORITY);
    }

    public void test_getType_WithEmptyContentUri_ReturnItemType() {
        // Arrange

        // Act
        String type = getMockContentResolver().getType(ZeitContract.ZeitDaten.EMPTY_CONTENT_URI);

        // Assert
        assertEquals("Empty uri should return always max. one item",
                ZeitContract.ZeitDaten.CONTENT_ITEM_TYPE, type);
    }

    public void test_insertValidItem_ReturnValidId() {
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.START_TIME, "2015-06-24T18:00");

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);

        // Assert
        long id = ContentUris.parseId(insertUri);
        assertTrue(id > 0);
    }

    public void test_insertInvalidValueItem_ReturnInvalidId() {
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.END_TIME, "2015-06-24T18:00");

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);

        // Assert
        assertNull(insertUri);
    }

    public void test_insertNegativePause_ReturnInvalidId() {
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.START_TIME, "2015-06-24T18:00");
        values.put(ZeitContract.ZeitDaten.Columns.PAUSE, -1);

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);

        // Assert
        assertNull(insertUri);
    }

    public void test_insertZeroPause_ReturnValidId() {
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.START_TIME, "2015-06-24T18:00");
        values.put(ZeitContract.ZeitDaten.Columns.PAUSE, 0);

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);

        // Assert
        long id = ContentUris.parseId(insertUri);
        assertTrue(id > 0);
    }

    public void test_insertPositivePause_ReturnValidId() {
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.START_TIME, "2015-06-24T18:00");
        values.put(ZeitContract.ZeitDaten.Columns.PAUSE, 1);

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);

        // Assert
        long id = ContentUris.parseId(insertUri);
        assertTrue(id > 0);
    }

    public void test_insertInvalidStartTime_ReturnInvalidId() {
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.START_TIME, "2015-06-24 18:00");
        values.put(ZeitContract.ZeitDaten.Columns.PAUSE, 1);

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);

        // Assert
        assertNull(insertUri);
    }

    public void test_updateNegativePause_ReturnInvalidId() {
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.START_TIME, "2015-06-24T18:00");

        ContentValues updateValues = new ContentValues();
        updateValues.put(ZeitContract.ZeitDaten.Columns.PAUSE, -1);

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);
        int updated = getMockContentResolver().update(insertUri, updateValues, null, null);

        // Assert
        assertEquals(0, updated);
    }

    public void test_updateZeroPause_ReturnValidId() {
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.START_TIME, "2015-06-24T18:00");

        ContentValues updateValues = new ContentValues();
        updateValues.put(ZeitContract.ZeitDaten.Columns.PAUSE, 0);

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);
        int updated = getMockContentResolver().update(insertUri, updateValues, null, null);

        // Assert
        assertEquals(1, updated);
    }

    public void test_updatePositivePause_ReturnValidId() {
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.START_TIME, "2015-06-24T18:00");

        ContentValues updateValues = new ContentValues();
        updateValues.put(ZeitContract.ZeitDaten.Columns.PAUSE, 1);

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);
        int updated = getMockContentResolver().update(insertUri, updateValues, null, null);

        // Assert
        assertEquals(1, updated);
    }
}
