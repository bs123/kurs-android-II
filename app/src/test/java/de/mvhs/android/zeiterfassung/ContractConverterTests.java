package de.mvhs.android.zeiterfassung;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.util.Calendar;

import de.mvhs.android.zeiterfassung.db.TimeDataContract;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

// Monkey Runner
//
// adb shell monkey -p de.mvhs.android.zeiterfassung -v 2000
//

/**
 * Created by eugen on 05.01.17.
 */

public class ContractConverterTests {
  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void formatForDb_withNullParameter_ThrowsException() {
    // Arrange
    exception.expect(NullPointerException.class);

    // Act
    TimeDataContract.Converter.formatForDb(null);
  }

  @Test
  public void formatForDb_returnIso8601String() {
    // Arrange
    final String expected = "2016-01-23T18:35";
    final Calendar inputTime = Calendar.getInstance();
    inputTime.set(2016, 0, 23, 18, 35, 58); // Monat ist "0" basiert

    // Act
    String result = TimeDataContract.Converter.formatForDb(inputTime);

    // Assert
    assertThat(result, is(expected));
  }

  @Test
  public void parseFromDb_withNullParameter_ThrowsException() throws ParseException {
    // Arrange
    exception.expect(NullPointerException.class);

    // Act
      TimeDataContract.Converter.parseFromDb(null);
  }

  @Test
  public void parseFromDb_withWrongFormat_ThrowsException() throws ParseException {
    // Arrange
    final String dbValue = "2016-01-23 18:01"; // Kein "T" zwischen Datum und Uhrzeit
    exception.expect(ParseException.class);

    // Act
    TimeDataContract.Converter.parseFromDb(dbValue);
  }

  @Test
  public void parseFromDb_returnCorrectCaledar() throws ParseException {
    // Arrange
    final String dbValue = "2016-01-23T18:35";

    // Act
    Calendar result = TimeDataContract.Converter.parseFromDb(dbValue);

    // Assert
    assertThat("Year should be 2016", result.get(Calendar.YEAR), is(2016));
    assertThat("Month should be January (0)", result.get(Calendar.MONTH), is(0)); // Monat ist "0" basiert
    assertThat("Day should be 23", result.get(Calendar.DAY_OF_MONTH), is(23));
    assertThat("Hours should be 18", result.get(Calendar.HOUR_OF_DAY), is(18));
    assertThat("Minute should be 35", result.get(Calendar.MINUTE), is(35));
    assertThat("Seconds should be 0, becoause not stored in the database", result.get(Calendar.SECOND), is(0));
    assertThat("Milliseconds should be 0, becoause not stored in the database", result.get(Calendar.MILLISECOND), is(0));
  }
}
