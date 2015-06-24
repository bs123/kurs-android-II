package de.mvhs.android.zeiterfassung;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.base.IdlingResourceRegistry;
import android.test.ActivityInstrumentationTestCase2;

import de.mvhs.android.zeiterfassung.db.ZeitContract;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;

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
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        getActivity().getContentResolver().delete(ZeitContract.ZeitDaten.CONTENT_URI, null, null);
    }
}
