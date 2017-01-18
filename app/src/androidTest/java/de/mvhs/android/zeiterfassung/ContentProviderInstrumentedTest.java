package de.mvhs.android.zeiterfassung;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import de.mvhs.android.zeiterfassung.db.TimeDataContract;
import de.mvhs.android.zeiterfassung.db.TimeDataProvider;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by javadev on 11.01.17.
 */


@RunWith(AndroidJUnit4.class)
public class ContentProviderInstrumentedTest extends ProviderTestCase2<TimeDataProvider> {

    public ContentProviderInstrumentedTest() {
        super(TimeDataProvider.class, TimeDataContract.AUTHORITY);
    }



    // PitFall public and Before for JUnit4 !!!
    @Override
    @Before
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @Test
    public void newDataBase_hasNoEntries() {
        //arrange none
        //act
        Cursor data = getMockContentResolver().query(TimeDataContract.TimeData.CONTENT_URI, null, null, null, null);

        //Assert
        assertThat(data.getCount(), is(0));
    }
    @Test
    public void newDataBase_hasAllColoums() {
        //arrange
        final String[] expectedColumns = {
                BaseColumns._ID,
                TimeDataContract.TimeData.Columns.START,
                TimeDataContract.TimeData.Columns.END,
                TimeDataContract.TimeData.Columns.PAUSE,
                TimeDataContract.TimeData.Columns.COMMENT
        };
        //act
        Cursor data = getMockContentResolver().query(TimeDataContract.TimeData.CONTENT_URI, null, null, null, null);

        //Assert
        assertThat(data.getColumnNames(), arrayContainingInAnyOrder(expectedColumns));
    }

}

