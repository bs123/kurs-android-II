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
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void mainActivityUITest() {
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.StartTimeValue), isDisplayed()));
        appCompatEditText.perform(click());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.StartCommand), withText("Start"), isDisplayed()));
        appCompatButton.perform(click());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.EndCommand), withText("End"), isDisplayed()));
        appCompatButton2.perform(click());

        ViewInteraction actionMenuItemView = onView(
                allOf(withId(R.id.MenuAddNew), withContentDescription("Add"), isDisplayed()));
        actionMenuItemView.perform(click());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.CommentValue),
                        withParent(withId(R.id.fragment2)),
                        isDisplayed()));
      //  appCompatEditText2.perform(replaceText("ttttt"), closeSoftKeyboard());

    }

}
