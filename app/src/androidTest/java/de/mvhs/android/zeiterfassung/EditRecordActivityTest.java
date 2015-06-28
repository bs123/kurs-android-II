package de.mvhs.android.zeiterfassung;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.isEmptyOrNullString;

/**
 * Created by kurs on 24.06.15.
 */
public class EditRecordActivityTest extends ActivityInstrumentationTestCase2<EditRecordActivity> {
    public EditRecordActivityTest() {
        super(EditRecordActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        Intent intent = new Intent(getActivity(), EditRecordActivity.class);
        intent.putExtra(EditRecordFragment.IS_EDITABLE_KEY, true);
        launchActivityWithIntent("de.mvhs.android.zeiterfassung", EditRecordActivity.class, intent);
    }

    public void test_TypeComment_ResultsInCommentText(){
        onView(withId(R.id.Comment))
                .perform(typeText("Hallo Welt!"))
                .check(matches(withText("Hallo Welt!")));
    }

    public void test_NegativePause_ResultsInPositiveInput(){
        onView(withId(R.id.Pause))
                .perform(typeText("-1"))
                .check(matches(withText("1")));
    }

    public void test_LetterPause_ResultsInNoInput(){
        onView(withId(R.id.Pause))
                .perform(typeText("Lorem Ipsum"))
                .check(matches(withText(isEmptyOrNullString())));
    }
}
