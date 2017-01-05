package de.mvhs.android.zeiterfassung;


import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Test generiert mit dem test-Recorder und optimiert für eigene Asserts
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

  @Rule
  public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

  @Test
  public void mainActivityUITest() {
    ViewInteraction appCompatButton = onView(withId(R.id.StartCommand));
    appCompatButton.perform(click());

    ViewInteraction editText = onView(withId(R.id.StartTimeValue));
    editText.check(matches(not(withText(""))));

    ViewInteraction editText2 = onView(withId(R.id.EndTimeValue));
    editText2.check(matches(withText("")));

    ViewInteraction appCompatButton2 = onView(withId(R.id.EndCommand));
    appCompatButton2.perform(click());

    ViewInteraction editText3 = onView(withId(R.id.StartTimeValue));
    editText3.check(matches(not(withText(""))));

    ViewInteraction editText4 = onView(withId(R.id.EndTimeValue));
    editText4.check(matches(not(withText(""))));

  }

}
