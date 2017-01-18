package de.mvhs.android.zeiterfassung;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by javadev on 11.01.17.
 */


// adb monkey
    // adb shell monkey -p de.mvhs.android.zeiterfassung -v 1000


@RunWith(AndroidJUnit4.class)
public class ListActivityViewInstrumentedTest  {

    //Start bei Activity
    @Rule
    public ActivityTestRule<ListDataActivity> _activity = new ActivityTestRule<ListDataActivity>(ListDataActivity.class);

    @Test
    public void pressOmAdd_StartEditActivity_ForViewEntry() {
        // arrange none

        //act
        onView(withId(R.id.MenuAddNew)).perform(click());

        //assert
        onView(withText(R.string.NewDataActivityTitle)).check(matches(isDisplayed()));
    }



}
