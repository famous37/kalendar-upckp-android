package com.shlemko.upckpcalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** Three stateless launcher widgets sharing the same calendar calculations. */
public class CalendarWidget extends AppWidgetProvider {
    protected int layoutResource(){return R.layout.widget_calendar;}
    @Override public void onReceive(Context context,Intent intent){
        super.onReceive(context,intent);
        String action=intent.getAction();
        if(Intent.ACTION_DATE_CHANGED.equals(action)||Intent.ACTION_TIME_CHANGED.equals(action)||
           Intent.ACTION_TIMEZONE_CHANGED.equals(action)||Intent.ACTION_BOOT_COMPLETED.equals(action)||
           Intent.ACTION_CONFIGURATION_CHANGED.equals(action)){
            refreshAll(context);
        }
    }
    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
        for(int id:ids)render(context,manager,id,layoutResource());
    }
    public static void refreshAll(Context context){
        AppWidgetManager manager=AppWidgetManager.getInstance(context);
        updateProvider(context,manager,CalendarWidget.class,R.layout.widget_calendar);
        updateProvider(context,manager,CompactCalendarWidget.class,R.layout.widget_calendar_compact);
        updateProvider(context,manager,LargeCalendarWidget.class,R.layout.widget_calendar_large);
    }
    private static void updateProvider(Context context,AppWidgetManager manager,Class<?> type,int layout){
        for(int id:manager.getAppWidgetIds(new ComponentName(context,type)))render(context,manager,id,layout);
    }
    private static void render(Context context,AppWidgetManager manager,int id,int layout){
        Calendar now=Calendar.getInstance();
        int y=now.get(Calendar.YEAR),m=now.get(Calendar.MONTH)+1,d=now.get(Calendar.DAY_OF_MONTH);
        int[] old=CalendarMath.gregorianToJulian(y,m,d);
        RemoteViews views=new RemoteViews(context.getPackageName(),layout);
        boolean dark=UiTheme.isDark(context);
        views.setInt(R.id.widget_root,"setBackgroundResource",
            dark?R.drawable.widget_background:R.drawable.widget_background_light);
        views.setTextColor(R.id.widget_title,Color.parseColor(ThemeTokens.accent(dark)));
        views.setTextColor(R.id.widget_date,Color.parseColor(ThemeTokens.ink(dark)));
        views.setTextColor(R.id.widget_saints,Color.parseColor(ThemeTokens.ink(dark)));
        if(layout==R.layout.widget_calendar_large){
            views.setTextColor(R.id.widget_civil,Color.parseColor(ThemeTokens.subtle(dark)));
            views.setTextColor(R.id.widget_fast,Color.parseColor(ThemeTokens.accent(dark)));
        }
        views.setTextViewText(R.id.widget_date,String.format(Locale.forLanguageTag("uk-UA"),
            "%d %s",old[2],CalendarData.MONTHS[old[1]-1]));
        try{
            CalendarData.Day data=new CalendarData(context).getOld(old[1],old[2]);
            String movable=LiturgicalCalendar.movableFeast(y,m,d);
            views.setTextViewText(R.id.widget_saints,movable.isEmpty()?data.headline():movable);
        }catch(Exception e){views.setTextViewText(R.id.widget_saints,"Відкрити календар");}
        if(layout==R.layout.widget_calendar_large){
            views.setTextViewText(R.id.widget_civil,"Новий стиль: "+
                new SimpleDateFormat("d MMMM yyyy",Locale.forLanguageTag("uk-UA")).format(now.getTime()));
            views.setTextViewText(R.id.widget_fast,LiturgicalCalendar.fastSummary(y,m,d));
        }
        Intent open=new Intent(context,MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        open.putExtra("open_today",true);
        PendingIntent click=PendingIntent.getActivity(context,id,open,
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root,click);
        manager.updateAppWidget(id,views);
    }
}
