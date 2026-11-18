package com.shlemko.upckpcalendar;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

/** Applies a consistent framework theme before Activity.onCreate / dialogs. */
public final class UiTheme {
    private UiTheme(){}
    public static boolean isDark(Context context) {
        SharedPreferences prefs=context.getSharedPreferences("calendar_settings",Context.MODE_PRIVATE);
        int chosen=prefs.getInt("theme",0);
        boolean systemDark=(context.getResources().getConfiguration().uiMode
            & Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;
        return chosen==2||(chosen==0&&systemDark);
    }
    public static void apply(Activity activity){
        activity.setTheme(isDark(activity)?R.style.AppThemeDark:R.style.AppTheme);
    }
}
