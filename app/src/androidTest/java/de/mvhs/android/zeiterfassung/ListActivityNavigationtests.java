package de.mvhs.android.zeiterfassung;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static android.support.test.espresso.matcher.ViewMatchers.*;

/**
 * Created by eugen on 05.01.17.
 */

@RunWith(AndroidJUnit4.class)
public class ListActivityNavigationtests {
  @Rule
  public ActivityTestRule<ListDataActivity> _activity = new ActivityTestRule<ListDataActivity>(ListDataActivity.class);

  @Test
  public void pressOnAdd_StartAddActivity_ForNewEntry(){
    // Arrange

    // Act
    onView(withId(R.id.MenuAddNew))
        .perform(click());

    // Assert
    onView(withText(R.string.NewDataActivityTitle)).check(matches(isDisplayed()));
  }
}
