package com.shlemko.upckpcalendar;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.GregorianCalendar;
import static org.junit.Assert.*;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

/** Instrumented UI smoke tests for navigation and the day details screen. */
@RunWith(AndroidJUnit4.class)
public class CalendarUiTest {
    @Rule public ActivityScenarioRule<MainActivity> rule=
        new ActivityScenarioRule<>(MainActivity.class);
    @Test public void source2018IsCompleteAndYearScoped() throws Exception {
        SourceYear2018 source=new SourceYear2018(
            InstrumentationRegistry.getInstrumentation().getTargetContext());
        assertNotNull(source.get(new GregorianCalendar(2018,0,1)));
        assertNotNull(source.get(new GregorianCalendar(2018,11,31)));
        assertNull(source.get(new GregorianCalendar(2026,10,18)));
    }
    @Test public void mainScreenAndOfflineMonthNavigation(){
        onView(withText("Календар УПЦ КП")).check(matches(isDisplayed()));
        onView(withContentDescription("Календар місяця")).perform(click());
        onView(withText("Календар місяця")).check(matches(isDisplayed()));
        onView(withContentDescription("Наступний місяць")).perform(click());
        onView(withContentDescription("Попередній місяць")).perform(click());
        onView(withContentDescription("Повернутися до дня")).perform(click());
        onView(withText("Свята та пам’яті святих")).check(matches(isDisplayed()));
    }
}
