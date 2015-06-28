package de.mvhs.android.zeiterfassung;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.base.IdlingResourceRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.EditText;

import de.mvhs.android.zeiterfassung.db.ZeitContract;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.typeCompatibleWith;

/**
 * Created by kurs on 24.06.15.
 */
public class TimeTrackingTest extends ActivityInstrumentationTestCase2<TimeTrackingActivity> {
    private TimeTrackingActivity _activity;

    public TimeTrackingTest() {
        super(TimeTrackingActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        _activity = getActivity();
    }

    public void test_ListAction_NavigateTo_ListActivity(){
        // Act
        onView(withId(R.id.action_list))
                .perform(click());

        // Assert
        onView(withId(R.id.StartTimeColumn))
                .check(matches(withText(R.string.StartTimeColumn)));

        pressBack();
    }

    public void test_StartButtonClicked(){
        // Act
        onView(withId(R.id.StartCommand)).check(matches(isEnabled()));
        onView(withId(R.id.EndCommand)).check(matches(not(isEnabled())));

        onView(withId(R.id.StartCommand)).perform(click());

        // Assert
        onView(withId(R.id.StartCommand)).check(matches(not(isEnabled())));
        onView(withId(R.id.EndCommand)).check(matches(isEnabled()));
        onView(withId(R.id.StartTime)).check(matches(withText(not(isEmptyOrNullString()))));

        _activity.getContentResolver().delete(ZeitContract.ZeitDaten.CONTENT_URI, null, null);
    }

    public void test_ClickOnNew_ResultsInNewAsEditDialog(){
        // Act
        onView(withId(R.id.action_new)).perform(click());

        // Assert
        onView(withText(R.string.title_activity_edit_record)).check(matches(isDisplayed()));

        pressBack();
    }

    public void test_ClickOnEdit_ResultsInEditAsEditDialog(){
        // Arrange
        onView(withId(R.id.StartCommand)).perform(click());
        onView(withId(R.id.EndCommand)).perform(click());

        // Act
        onView(withId(R.id.action_list)).perform(click());
        onData(anything()).atPosition(0).perform(longClick());
        onView(withText(R.string.action_edit)).check(matches(isDisplayed())).perform(click());

        // Assert
        onView(withText(R.string.activity_title_edit)).check(matches(isDisplayed()));

        _activity.getContentResolver().delete(ZeitContract.ZeitDaten.CONTENT_URI, null, null);

        pressBack();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
