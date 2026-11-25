package com.shlemko.upckpcalendar;

import android.content.Context;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Build;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.OutputStream;
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
            verify(1,"light",Color.parseColor("#F8F5EF"));
            verify(2,"dark",Color.parseColor("#1A1719"));
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
        onView(withText("Церковний календар")).check(matches(isDisplayed()));
        rule.getScenario().onActivity(activity->
            assertEquals("Page background "+tag,expectedBarColor,activity.ui.bg));
        capture(tag+"-home");
        onView(withContentDescription("Місяць")).perform(click());
        onView(withContentDescription("Попередній місяць")).check(matches(isDisplayed()));
        capture(tag+"-month");
        onView(withContentDescription("Налаштування")).perform(click());
        onView(withText("Про застосунок")).perform(click());
        onView(withText(R.string.dedication)).check(matches(isDisplayed()));
        capture(tag+"-about");
        onView(withContentDescription("День")).perform(click());
    }
    private void capture(String name) throws Exception{
        Bitmap bitmap=InstrumentationRegistry.getInstrumentation().getUiAutomation().takeScreenshot();
        assertNotNull("Screenshot of "+name,bitmap);
        assertTrue("Emulator must support MediaStore scoped storage",Build.VERSION.SDK_INT>=29);
        Context context=InstrumentationRegistry.getInstrumentation().getTargetContext();
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME,name+".png");
        values.put(MediaStore.Images.Media.MIME_TYPE,"image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/KalendarThemeQA");
        Uri uri=context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        assertNotNull("MediaStore insert",uri);
        try(OutputStream out=context.getContentResolver().openOutputStream(uri)){
            assertNotNull(out);
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,out));
        }finally{
            bitmap.recycle();
        }
    }
}
