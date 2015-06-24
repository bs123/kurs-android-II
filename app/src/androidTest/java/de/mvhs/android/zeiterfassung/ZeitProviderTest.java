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

    public void test_insertInvalidValueItem_ReturnInvalidId(){
        // Arrange
        ContentValues values = new ContentValues();
        values.put(ZeitContract.ZeitDaten.Columns.END_TIME, "2015-06-24T18:00");

        // Act
        Uri insertUri = getMockContentResolver().insert(ZeitContract.ZeitDaten.CONTENT_URI, values);

        // Assert
        assertNull(insertUri);
    }
}
