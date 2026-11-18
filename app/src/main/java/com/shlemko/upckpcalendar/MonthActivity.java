package com.shlemko.upckpcalendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/** A complete offline Gregorian month overview with Julian dates and feast markers. */
public final class MonthActivity extends Activity {
    public static final String EXTRA_DATE="selected_date_millis";
    private static final Locale UK=Locale.forLanguageTag("uk-UA");
    private final Calendar selected=Calendar.getInstance(),month=Calendar.getInstance();
    private int bg,card,ink,subtle,gold,border;
    private TextView heading;
    private GridLayout dates;
    private CalendarData data;

    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+0.5f);}
    private GradientDrawable shape(int fill,int radius,int stroke){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));
        if(stroke!=0)d.setStroke(dp(1.5f),stroke);return d;
    }
    private TextView label(String value,int sp,int color,boolean bold){
        TextView t=new TextView(this);t.setText(value);t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP,sp);
        t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
        t.setGravity(Gravity.CENTER);t.setIncludeFontPadding(true);return t;
    }
    private Button action(String value){
        Button b=new Button(this);b.setAllCaps(false);b.setText(value);
        b.setTextColor(ink);b.setTextSize(16);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        b.setBackground(shape(card,14,border));b.setMinHeight(dp(48));return b;
    }
    private LinearLayout vertical(){LinearLayout v=new LinearLayout(this);v.setOrientation(1);return v;}
    private LinearLayout horizontal(){LinearLayout v=new LinearLayout(this);v.setOrientation(0);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    private void colors(){
        SharedPreferences prefs=getSharedPreferences("calendar_settings",MODE_PRIVATE);
        int theme=prefs.getInt("theme",0);
        boolean dark=theme==2||(theme==0&&(getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES);
        bg=Color.parseColor(dark?"#101722":"#F7F5EF");
        card=Color.parseColor(dark?"#1D2936":"#FFFEFB");
        ink=Color.parseColor(dark?"#F5F5F2":"#202D3C");
        subtle=Color.parseColor(dark?"#B7C1CA":"#526273");
        gold=Color.parseColor(dark?"#E7C27C":"#91631F");
        border=Color.parseColor(dark?"#354454":"#DCD9D1");
        getWindow().setStatusBarColor(bg);getWindow().setNavigationBarColor(bg);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        selected.setTimeInMillis(getIntent().getLongExtra(EXTRA_DATE,System.currentTimeMillis()));
        if(state!=null)selected.setTimeInMillis(state.getLong("selected_date",selected.getTimeInMillis()));
        month.setTimeInMillis(state!=null?state.getLong("month_date",selected.getTimeInMillis()):selected.getTimeInMillis());
        month.set(Calendar.DAY_OF_MONTH,1);
        try{data=new CalendarData(this);}catch(Exception e){finish();return;}
        colors();
        LinearLayout root=vertical();root.setPadding(dp(12),dp(12),dp(12),dp(12));root.setBackgroundColor(bg);
        setContentView(root);
        if(android.os.Build.VERSION.SDK_INT>=35){
            root.setOnApplyWindowInsetsListener((v,insets)->{
                android.graphics.Insets i=insets.getInsets(android.view.WindowInsets.Type.systemBars());
                v.setPadding(dp(12),i.top+dp(12),dp(12),i.bottom+dp(12));return insets;
            });root.requestApplyInsets();
        }
        LinearLayout top=horizontal();
        Button back=action("‹");back.setContentDescription("Повернутися до дня");
        back.setOnClickListener(v->finish());top.addView(back,new LinearLayout.LayoutParams(dp(50),dp(50)));
        TextView ttl=label("Календар місяця",21,ink,true);ttl.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(52),1);p.leftMargin=dp(12);top.addView(ttl,p);
        root.addView(top);
        LinearLayout controls=horizontal();controls.setPadding(0,dp(12),0,dp(16));
        Button previous=action("‹");previous.setContentDescription("Попередній місяць");previous.setOnClickListener(v->shift(-1));
        controls.addView(previous,new LinearLayout.LayoutParams(dp(52),dp(52)));
        heading=label("",19,ink,true);heading.setOnClickListener(v->chooseMonth());
        controls.addView(heading,new LinearLayout.LayoutParams(0,dp(52),1));
        Button next=action("›");next.setContentDescription("Наступний місяць");next.setOnClickListener(v->shift(1));
        controls.addView(next,new LinearLayout.LayoutParams(dp(52),dp(52)));
        root.addView(controls);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);
        LinearLayout contents=vertical();contents.setPadding(dp(1),0,dp(1),dp(12));
        scroll.addView(contents);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView legend=label("Число вгорі — новий стиль   ·   унизу — старий",12,subtle,false);
        legend.setGravity(Gravity.LEFT);contents.addView(legend);
        dates=new GridLayout(this);dates.setColumnCount(7);dates.setUseDefaultMargins(false);
        contents.addView(dates,new LinearLayout.LayoutParams(-1,-2));
        Button today=action("Сьогодні");today.setOnClickListener(v->select(System.currentTimeMillis()));
        root.addView(today,new LinearLayout.LayoutParams(-1,dp(52)));
        render();
    }
    @Override protected void onSaveInstanceState(Bundle state){
        state.putLong("selected_date",selected.getTimeInMillis());
        state.putLong("month_date",month.getTimeInMillis());
        super.onSaveInstanceState(state);
    }
    private void chooseMonth(){
        new DatePickerDialog(this,(view,y,m,d)->{
            month.set(y,m,1,12,0,0);render();
        },month.get(Calendar.YEAR),month.get(Calendar.MONTH),1).show();
    }
    private void shift(int delta){month.add(Calendar.MONTH,delta);month.set(Calendar.DAY_OF_MONTH,1);render();}
    private GridLayout.LayoutParams cellParams(int height){
        GridLayout.LayoutParams p=new GridLayout.LayoutParams(
            GridLayout.spec(GridLayout.UNDEFINED),GridLayout.spec(GridLayout.UNDEFINED,1f));
        p.width=0;p.height=dp(height);p.setMargins(dp(2),dp(2),dp(2),dp(2));return p;
    }
    private void render(){
        heading.setText(new SimpleDateFormat("LLLL yyyy",UK).format(month.getTime()));
        dates.removeAllViews();
        String[] week={"Пн","Вт","Ср","Чт","Пт","Сб","Нд"};
        for(String name:week)dates.addView(label(name,12,subtle,true),cellParams(29));
        int year=month.get(Calendar.YEAR),m=month.get(Calendar.MONTH)+1;
        int[] cells=MonthGrid.days(year,m);
        Calendar current=Calendar.getInstance();
        for(int day:cells){
            LinearLayout tile=vertical();tile.setGravity(Gravity.CENTER);
            if(day>0){
                Calendar civil=new GregorianCalendar(year,m-1,day,12,0,0);
                int[] old=CalendarMath.gregorianToJulian(year,m,day);
                boolean isSelected=year==selected.get(Calendar.YEAR)&&m==selected.get(Calendar.MONTH)+1&&day==selected.get(Calendar.DAY_OF_MONTH);
                boolean isToday=year==current.get(Calendar.YEAR)&&m==current.get(Calendar.MONTH)+1&&day==current.get(Calendar.DAY_OF_MONTH);
                boolean feast=!LiturgicalCalendar.movableFeast(year,m,day).isEmpty()||!data.getOld(old[1],old[2]).feast.isEmpty();
                tile.setBackground(shape(isSelected?gold:card,12,isToday?gold:border));
                tile.addView(label(String.valueOf(day),17,isSelected?Color.WHITE:ink,true));
                tile.addView(label(old[2]+" ст.",10,isSelected?Color.WHITE:subtle,false));
                TextView dot=label(feast?"●":"",10,isSelected?Color.WHITE:gold,true);tile.addView(dot);
                tile.setContentDescription(day+" "+new SimpleDateFormat("LLLL",UK).format(civil.getTime())+
                    ", за старим стилем "+old[2]+", "+(feast?"свято або пам'ять":"звичайний день"));
                tile.setOnClickListener(v->select(civil.getTimeInMillis()));
            }
            dates.addView(tile,cellParams(71));
        }
        dates.animate().cancel();dates.setAlpha(.94f);
        dates.animate().alpha(1f).setDuration(160).start();
    }
    private void select(long millis){
        Intent result=new Intent();result.putExtra(EXTRA_DATE,millis);
        setResult(RESULT_OK,result);finish();
    }
}
