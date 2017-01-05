package de.mvhs.android.zeiterfassung;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import java.util.Calendar;

import de.mvhs.android.zeiterfassung.db.TimeDataContract;
import de.mvhs.android.zeiterfassung.db.TimeDataProvider;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * Created by eugen on 05.01.17.
 */

@RunWith(AndroidJUnit4.class)
public class ProviderTests extends ProviderTestCase2<TimeDataProvider> {
  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  /**
   * Constructor.
   */
  public ProviderTests() {
    super(TimeDataProvider.class, TimeDataContract.AUTHORITY);
  }

  @Override
  @Before
  public void setUp() throws Exception {
    setContext(InstrumentationRegistry.getTargetContext());
    super.setUp();
  }

  @Override
  @After
  public void tearDown() throws Exception {
    //getMockContentResolver().delete(TimeDataContract.TimeData.CONTENT_URI, null, null);
    super.tearDown();
  }

  @Test
  public void newDatabaseHaveNoEntries(){
    // Arrange


    // Act
    Cursor data = getMockContentResolver().query(TimeDataContract.TimeData.CONTENT_URI, null, null, null, null);

    // Assert
    assertThat(data.getCount(), is(0));
  }

  @Test
  public void databaseResultHaveAllColumns(){
    //Arrange
    final String[] expectedColumns = {
        TimeDataContract.TimeData.Columns._ID,
        TimeDataContract.TimeData.Columns.START,
        TimeDataContract.TimeData.Columns.END,
        TimeDataContract.TimeData.Columns.PAUSE,
        TimeDataContract.TimeData.Columns.COMMENT
    };

    // Act
    Cursor data = getMockContentResolver().query(TimeDataContract.TimeData.CONTENT_URI, null, null, null, null);

    // Assert
    assertThat(data.getColumnNames(), arrayContainingInAnyOrder(expectedColumns));
  }

  @Test
  public void insertWithoutStart_DoesntStoreEntry(){
    // Arrange
    ContentValues values = new ContentValues();
    values.put(TimeDataContract.TimeData.Columns.END, TimeDataContract.Converter.formatForDb(Calendar.getInstance()));

    // Act
    Uri insertUri = getMockContentResolver().insert(TimeDataContract.TimeData.CONTENT_URI, values);

    // Assert
    long id = ContentUris.parseId(insertUri);
    assertThat("No new ID generated (-1)", id, is(-1l));
  }

  @Test
  public void insertWithStart_StoreNewEntry(){
    // Arrange
    ContentValues values = new ContentValues();
    values.put(TimeDataContract.TimeData.Columns.START, TimeDataContract.Converter.formatForDb(Calendar.getInstance()));

    // Act
    Uri insertUri = getMockContentResolver().insert(TimeDataContract.TimeData.CONTENT_URI, values);

    // Assert
    long id = ContentUris.parseId(insertUri);
    assertThat("New ID generated (-1)", id, greaterThan(0l));

    Cursor data = getMockContentResolver().query(TimeDataContract.TimeData.CONTENT_URI, null, null, null, null);
    assertThat("Only one item is in database", data.getCount(), is(1));
  }
}
