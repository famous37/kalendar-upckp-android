package com.shlemko.upckpcalendar;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.File;
import java.io.FileOutputStream;
import static org.junit.Assert.*;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

/** Theme switching and visual regression snapshots on a real Android emulator. */
@RunWith(AndroidJUnit4.class)
public class ThemeVisualTest {
    @Rule public ActivityScenarioRule<MainActivity> rule=
        new ActivityScenarioRule<>(MainActivity.class);

    @Test public void screenshotLightAndDarkHomeAndMonth() throws Exception {
        try{
            verify(1,"light",Color.parseColor("#F7F5EF"));
            verify(2,"dark",Color.parseColor("#101722"));
        } finally {
            Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
            context.getSharedPreferences("calendar_settings",Context.MODE_PRIVATE)
                .edit().putInt("theme",0).commit();
        }
    }
    private void verify(int mode,String tag,int expectedBarColor) throws Exception {
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences p=context.getSharedPreferences("calendar_settings",Context.MODE_PRIVATE);
        assertTrue(p.edit().putInt("theme",mode).commit());
        rule.getScenario().onActivity(activity->activity.recreate());
        onView(withText("Календар УПЦ КП")).check(matches(isDisplayed()));
        rule.getScenario().onActivity(activity->
            assertEquals("Status-bar color "+tag,expectedBarColor,
                activity.getWindow().getStatusBarColor()));
        capture(tag+"-home");
        onView(withContentDescription("Календар місяця")).perform(click());
        onView(withText("Календар місяця")).check(matches(isDisplayed()));
        capture(tag+"-month");
        onView(withContentDescription("Повернутися до дня")).perform(click());
    }
    private void capture(String name) throws Exception{
        Bitmap bitmap=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull("Screenshot of "+name,bitmap);
        File dir=InstrumentationRegistry.getInstrumentation().getTargetContext().getExternalFilesDir("screenshots");
        assertNotNull(dir);
        assertTrue(dir.isDirectory()||dir.mkdirs());
        File file=new File(dir,name+".png");
        try(FileOutputStream out=new FileOutputStream(file)){
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
        }finally{
            bitmap.recycle();
        }
    }
}
