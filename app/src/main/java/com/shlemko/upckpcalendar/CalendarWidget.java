package com.shlemko.upckpcalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.util.Calendar;
import java.util.Locale;

/** Stateless provider: re-reads current date on each system update. */
public final class CalendarWidget extends AppWidgetProvider {
    @Override public void onReceive(Context context,Intent intent){
        super.onReceive(context,intent);
        String a=intent.getAction();
        if(Intent.ACTION_DATE_CHANGED.equals(a)||Intent.ACTION_TIME_CHANGED.equals(a)||
          Intent.ACTION_TIMEZONE_CHANGED.equals(a)||
          Intent.ACTION_BOOT_COMPLETED.equals(a))refreshAll(context);
    }
    @Override public void onUpdate(Context context,AppWidgetManager manager,int[] ids){
        for(int id:ids)update(context,manager,id);
    }
    public static void refreshAll(Context context){
        AppWidgetManager am=AppWidgetManager.getInstance(context);
        int[] ids=am.getAppWidgetIds(new ComponentName(context,CalendarWidget.class));
        for(int id:ids)update(context,am,id);
    }
    private static void update(Context context,AppWidgetManager am,int id){
        Calendar now=Calendar.getInstance();
        int[] old=CalendarMath.gregorianToJulian(now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1,now.get(Calendar.DAY_OF_MONTH));
        RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.widget_calendar);
        views.setTextViewText(R.id.widget_date,String.format(Locale.forLanguageTag("uk-UA"),"%d %s • ст. стиль",old[2],CalendarData.MONTHS[old[1]-1]));
        try {
            CalendarData.Day data=new CalendarData(context).getOld(old[1],old[2]);
            String major=LiturgicalCalendar.movableFeast(now.get(Calendar.YEAR),now.get(Calendar.MONTH)+1,now.get(Calendar.DAY_OF_MONTH));
            String headline=major.isEmpty()?data.headline():major;
            views.setTextViewText(R.id.widget_saints,headline);
        }catch(Exception e){views.setTextViewText(R.id.widget_saints,"Відкрити календар");}
        Intent open=new Intent(context,MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi=PendingIntent.getActivity(context,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root,pi);
        am.updateAppWidget(id,views);
    }
}
