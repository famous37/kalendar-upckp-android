package com.shlemko.upckpcalendar;

import android.content.Context;
import android.widget.EditText;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.GregorianCalendar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.startsWith;

@RunWith(AndroidJUnit4.class)
public class CalendarUiTest {
    @Rule public ActivityScenarioRule<MainActivity> rule = new ActivityScenarioRule<>(MainActivity.class);

    @Test public void dataAndSearchAreComplete() throws Exception {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        CalendarData data = CalendarData.load(ctx);
        int count = 0;
        for (int m = 1; m <= 12; m++) {
            int last = MonthGrid.daysInMonth(2024,m);
            for (int d = 1; d <= last; d++) {
                assertFalse(data.getOld(m,d).headline().isEmpty());
                count++;
            }
        }
        assertEquals(366,count);
        SourceYear2018 source = new SourceYear2018(ctx);
        GregorianCalendar day = new GregorianCalendar(2018, 0, 1);
        for (int i=0; i<365; i++) {
            assertNotNull(source.get(day));
            day.add(java.util.Calendar.DAY_OF_MONTH,1);
        }
        assertNull(source.get(day));
        CalendarIndex index = new CalendarIndex(data, 2026);
        assertTrue(index.find("ПАСХА").stream().anyMatch(r -> r.date.jdn == CalendarMath.gregorianToJdn(2026,4,12)));
        assertTrue(index.find("Касіяна Римлянина").stream().anyMatch(r -> r.date.old[1] == 2 && r.date.old[2] == 29));
        assertEquals(CalendarIndex.fold("пам’ять"),CalendarIndex.fold("пам'ять"));
    }

    @Test public void monthSelectionAndBackKeepMonth() {
        onView(withText("Церковний календар")).check(matches(isDisplayed()));
        rule.getScenario().onActivity(a -> a.selectDay(CalendarMath.gregorianToJdn(2026,11,25)));
        onView(withContentDescription("Місяць")).perform(click());
        onView(withText("Листопад 2026")).check(matches(isDisplayed()));
        onView(withContentDescription("Наступний місяць")).perform(click());
        onView(withText("Грудень 2026")).check(matches(isDisplayed()));
        onView(withContentDescription(startsWith("Четвер, 3 грудня 2026,"))).check((v, error) -> {
            if (error != null) throw error;
            android.view.ViewGroup cell = (android.view.ViewGroup)v;
            for (int i = 0; i < 2; i++) {
                android.view.View number = cell.getChildAt(i);
                assertEquals(cell.getWidth(), number.getLeft() + number.getRight(), 2);
            }
        }).perform(click());
        onView(withText("20 листопада")).check(matches(isDisplayed()));
        pressBack();
        onView(withText("Грудень 2026")).check(matches(isDisplayed()));
        onView(withContentDescription("Попередній місяць")).perform(click());
        onView(withText("Листопад 2026")).check(matches(isDisplayed()));
    }

    @Test public void searchBookmarksAndAbout() {
        rule.getScenario().onActivity(a -> {
            a.prefs.edit().remove("favorite_old_dates").commit();
            a.selectDay(CalendarMath.gregorianToJdn(2026,4,12));
        });
        onView(withContentDescription("Додати день до вибраного")).perform(click());
        onView(withContentDescription("Вибране")).perform(click());
        onView(withText("Вибрані дні")).check(matches(isDisplayed()));
        onData(org.hamcrest.Matchers.anything()).atPosition(0).perform(click());
        onView(withContentDescription("Прибрати день із вибраного")).perform(click());
        pressBack();
        onView(withText("Ще немає вибраних днів")).check(matches(isDisplayed()));
        onView(withContentDescription("Пошук")).perform(click());
        onView(isAssignableFrom(EditText.class)).perform(replaceText("Пасха"), closeSoftKeyboard());
        onData(org.hamcrest.Matchers.anything()).atPosition(0).perform(click());
        onView(withText("30 березня")).check(matches(isDisplayed()));
        pressBack();
        onView(withContentDescription("Пошук у календарі")).check(matches(withText("Пасха")));
        onView(withContentDescription("Налаштування")).perform(click());
        onView(withText("Про застосунок")).perform(click());
        onView(withText(R.string.dedication)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText(R.string.calendar_source)).perform(scrollTo()).check(matches(isDisplayed()));
        onView(withText("Розробник: Василь Шлемко")).perform(scrollTo()).check(matches(isDisplayed()));
        rule.getScenario().recreate();
        onView(withText(R.string.developer_credit)).perform(scrollTo()).check(matches(isDisplayed()));
    }
}
