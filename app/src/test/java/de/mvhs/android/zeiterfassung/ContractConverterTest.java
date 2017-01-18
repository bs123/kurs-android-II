package de.mvhs.android.zeiterfassung;


import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.util.Calendar;

import de.mvhs.android.zeiterfassung.db.TimeDataContract;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Created by javadev on 11.01.17.
 */

public class ContractConverterTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void formatForDb_returnISO8601String() {
        // Arrange
        final String expected = "2016-01-23T18:35";
        final Calendar inputDateTime = Calendar.getInstance();
        inputDateTime.set(2016, 0, 23, 18, 35); //Monat mit 0


        // act
        String result = TimeDataContract.Converter.formatForDb(inputDateTime);

        // Assert
        assertThat(result, is(expected));

    }

    @Test
    public void formatForDb_withNull_ThrowsException() {
        // arrange
        expectedException.expect(NullPointerException.class);
        //act
        TimeDataContract.Converter.formatForDb(null);

        // assert
        fail(" should not come to here");
    }

    @Test
    public void parsFromDb_withnullParameter_throws() throws ParseException {

        //arrange
        expectedException.expect(NullPointerException.class);
        //act
        TimeDataContract.Converter.parseFromDb(null);

        //assert;
        fail(" should not come to here");
    }

    @Test
    public void parseFromDb_withWrongForamtNotIso_ThrowsException() throws ParseException {
        //arrange
        final String inputValue = "2016-05-01 23:44" ;
        expectedException.expect(ParseException.class);

        // act
        TimeDataContract.Converter.parseFromDb(inputValue);

        //assert
        fail(" should not come to here");
    }

    @Test
    public void parseFromDb_returnCalnder() throws ParseException {
        //arrange
        final String dbValue ="2016-05-01T23:44" ;
        // act
        Calendar result = TimeDataContract.Converter.parseFromDb(dbValue);

        //asssert
        assertThat(result.get(Calendar.YEAR), is(2016));
    }
}
