package com.shlemko.upckpcalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

/** Modern, SDK-only Android UI. No legacy DEX, storage permission, or network dependency. */
public final class MainActivity extends Activity {
    private static final Locale UK=Locale.forLanguageTag("uk-UA");
    private final Calendar selected=Calendar.getInstance();
    private CalendarData data;
    private SharedPreferences prefs;
    private LinearLayout root, content;
    private TextView title, oldDate, newDate, greeting, badge, saints, specialNote, movableFeast;
    private Button favoriteButton;
    private boolean dark;
    private int fontSize;
    private int background, surface, ink, subtle, accent, border;
    private static final String[] THEME_NAMES={"Як у системі","Світла","Темна"};
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        prefs=getSharedPreferences("calendar_settings",MODE_PRIVATE);
        try { data=new CalendarData(this); }
        catch(Exception e) {
            new AlertDialog.Builder(this).setTitle("Помилка читання календаря")
                .setMessage(e.getMessage()).setPositiveButton("Закрити",(d,w)->finish()).show();return;
        }
        if(saved!=null)selected.setTimeInMillis(saved.getLong("day",selected.getTimeInMillis()));
        build();
    }
    @Override protected void onSaveInstanceState(Bundle out) {
        out.putLong("day",selected.getTimeInMillis());super.onSaveInstanceState(out);
    }
    @Override protected void onResume(){super.onResume();CalendarWidget.refreshAll(this);}
    private boolean darkSystem() {
        int night=getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return night==android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }
    private void palette() {
        int theme=prefs.getInt("theme",0);
        dark=theme==2||(theme==0&&darkSystem());
        fontSize=prefs.getInt("fontSize",18);
        background=Color.parseColor(dark?"#0D1420":"#F6F4ED");
        surface=Color.parseColor(dark?"#192335":"#FFFFFF");
        ink=Color.parseColor(dark?"#F4F5F7":"#1C2E47");
        subtle=Color.parseColor(dark?"#ADBBCF":"#66738A");
        accent=Color.parseColor(dark?"#D4AD65":"#A6752A");
        border=Color.parseColor(dark?"#304056":"#E9E6DE");
        getWindow().setStatusBarColor(background);getWindow().setNavigationBarColor(background);
        int flags=dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if(Build.VERSION.SDK_INT>=26&&!dark)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        if(Build.VERSION.SDK_INT>=29)getWindow().setNavigationBarContrastEnforced(false);
    }
    private int dp(float a){return (int)(a*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable shape(int color,int radius,int stroke) {
        GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));
        if(stroke!=0)g.setStroke(dp(1),stroke);return g;
    }
    private TextView text(String value,float size,int color,boolean bold) {
        TextView t=new TextView(this);t.setText(value);t.setTextColor(color);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP,size);
        t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
        t.setIncludeFontPadding(true);
        t.setLineSpacing(dp(2),1.07f);
        if(Build.VERSION.SDK_INT>=23)t.setBreakStrategy(android.text.Layout.BREAK_STRATEGY_HIGH_QUALITY);
        return t;
    }
    private LinearLayout vertical(){LinearLayout a=new LinearLayout(this);a.setOrientation(LinearLayout.VERTICAL);return a;}
    private LinearLayout horizontal(){LinearLayout a=new LinearLayout(this);a.setGravity(Gravity.CENTER_VERTICAL);a.setOrientation(LinearLayout.HORIZONTAL);return a;}
    private LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
    private LinearLayout.LayoutParams margin(int w,int h,int l,int t,int r,int b){
        LinearLayout.LayoutParams p=lp(w,h);p.setMargins(dp(l),dp(t),dp(r),dp(b));return p;
    }
    private Button button(String label,boolean filled){
        Button b=new Button(this);b.setAllCaps(false);b.setText(label);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP,14);b.setTextColor(filled?Color.WHITE:ink);
        b.setBackground(shape(filled?Color.parseColor("#253B59"):surface,14,filled?0:border));
        b.setPadding(dp(10),0,dp(10),0);b.setMinHeight(dp(50));return b;
    }
    private void build(){
        palette();
        root=vertical();root.setBackgroundColor(background);
        setContentView(root);
        if(Build.VERSION.SDK_INT>=35){
            root.setOnApplyWindowInsetsListener((v,insets)->{
                android.graphics.Insets s=insets.getInsets(WindowInsets.Type.systemBars());
                v.setPadding(0,s.top,0,s.bottom);return insets;
            });root.requestApplyInsets();
        }
        LinearLayout toolbar=horizontal();toolbar.setPadding(dp(18),dp(14),dp(12),dp(12));
        LinearLayout texts=vertical();
        TextView heading=text("Календар УПЦ КП",19,ink,true);
        TextView caption=text("Юліанський календар · офлайн",12,subtle,false);
        texts.addView(heading);texts.addView(caption,margin(-1,-2,0,2,0,0));
        toolbar.addView(texts,new LinearLayout.LayoutParams(0,-2,1));
        favoriteButton=button("☆",false);favoriteButton.setContentDescription("Додати день до вибраного");
        favoriteButton.setOnClickListener(v->toggleFavorite());toolbar.addView(favoriteButton,margin(43,49,3,0,0,0));
        Button dateIcon=button("▦",false);dateIcon.setContentDescription("Календар місяця");
        dateIcon.setOnClickListener(v->showMonthPicker());toolbar.addView(dateIcon,margin(43,49,3,0,0,0));
        Button menu=button("⋮",false);menu.setContentDescription("Відкрити меню й налаштування");
        menu.setOnClickListener(v->showMenu(menu));toolbar.addView(menu,margin(43,49,3,0,0,0));
        root.addView(toolbar,lp(-1,-2));
        View line=new View(this);line.setBackgroundColor(border);root.addView(line,lp(-1,1));
        ScrollView scrolling=new ScrollView(this);scrolling.setFillViewport(true);scrolling.setVerticalScrollBarEnabled(false);
        content=vertical();content.setPadding(dp(18),dp(18),dp(18),dp(20));
        scrolling.addView(content,new ScrollView.LayoutParams(-1,-2));
        root.addView(scrolling,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout dateCard=vertical();dateCard.setPadding(dp(20),dp(18),dp(20),dp(20));
        dateCard.setBackground(shape(surface,23,border));
        TextView eyebrow=text("ДЕНЬ ЗА СТАРИМ СТИЛЕМ",11,accent,true);
        eyebrow.setLetterSpacing(.10f);dateCard.addView(eyebrow);
        LinearLayout dateRow=horizontal();dateRow.setGravity(Gravity.CENTER_VERTICAL);
        oldDate=text("",29,ink,true);dateRow.addView(oldDate,new LinearLayout.LayoutParams(0,-2,1));
        ImageView cross=new ImageView(this);cross.setImageResource(R.drawable.krst_oker);
        cross.setScaleType(ImageView.ScaleType.FIT_CENTER);dateRow.addView(cross,lp(40,52));
        dateCard.addView(dateRow,margin(-1,-2,0,11,0,0));
        newDate=text("",14,subtle,false);dateCard.addView(newDate,margin(-1,-2,0,4,0,0));
        greeting=text("",13,subtle,false);dateCard.addView(greeting,margin(-1,-2,0,7,0,0));
        content.addView(dateCard,lp(-1,-2));

        LinearLayout badges=horizontal();badges.setPadding(0,dp(15),0,0);
        badge=text("",12,accent,true);badge.setPadding(dp(12),dp(7),dp(12),dp(7));
        badge.setBackground(shape(dark?Color.parseColor("#342A20"):Color.parseColor("#F4E9D4"),14,0));
        badges.addView(badge);content.addView(badges);
        TextView section=text("Свята та пам’яті святих",20,ink,true);
        content.addView(section,margin(-1,-2,0,23,0,10));
        LinearLayout memCard=vertical();memCard.setBackground(shape(surface,22,border));
        memCard.setPadding(dp(18),dp(18),dp(18),dp(18));
        movableFeast=text("",19,accent,true);
        memCard.addView(movableFeast,margin(-1,-2,0,0,0,10));
        title=text("",20,ink,true);memCard.addView(title);
        specialNote=text("",15,accent,false);memCard.addView(specialNote,margin(-1,-2,0,7,0,0));
        saints=text("",fontSize,ink,false);saints.setTextIsSelectable(true);
        memCard.addView(saints,margin(-1,-2,0,14,0,0));
        content.addView(memCard,lp(-1,-2));
        TextView footer=text("Пам’яті святих: база наданого календаря 1.1uk.\n"
            + "Рухомі свята обчислено за православною пасхалією. Правила посту — довідкові, "
            + "не замінюють щорічних офіційних богослужбових вказівок УПЦ КП.",11,subtle,false);
        content.addView(footer,margin(-1,-2,3,20,3,6));

        LinearLayout nav=horizontal();nav.setPadding(dp(15),dp(10),dp(15),dp(12));
        nav.setBackgroundColor(background);
        Button previous=button("‹  Попередній",false);previous.setOnClickListener(v->changeDay(-1));
        nav.addView(previous,new LinearLayout.LayoutParams(0,dp(52),1));
        Button today=button("Сьогодні",true);today.setOnClickListener(v->goToday());
        nav.addView(today,margin(104,52,8,0,8,0));
        Button next=button("Наступний  ›",false);next.setOnClickListener(v->changeDay(1));
        nav.addView(next,new LinearLayout.LayoutParams(0,dp(52),1));
        root.addView(nav,lp(-1,-2));
        update();
    }
    private static String cleanHeading(String a){return a.replaceFirst("^(r\\+|[+_%&-])+\\s*","").trim();}
    private void update(){
        int y=selected.get(Calendar.YEAR),m=selected.get(Calendar.MONTH)+1,d=selected.get(Calendar.DAY_OF_MONTH);
        int[] old=CalendarMath.gregorianToJulian(y,m,d);
        String oldTitle=old[2]+" "+CalendarData.MONTHS[old[1]-1]+" "+old[0];
        oldDate.setText(oldTitle);
        newDate.setText("Новий стиль: "+new SimpleDateFormat("d MMMM yyyy",UK).format(selected.getTime()));
        greeting.setText(new SimpleDateFormat("EEEE",UK).format(selected.getTime()));
        String fasting=LiturgicalCalendar.fastSummary(y,m,d);
        badge.setText(fasting);
        String move=LiturgicalCalendar.movableFeast(y,m,d);
        movableFeast.setText(move);
        movableFeast.setVisibility(move.isEmpty()?View.GONE:View.VISIBLE);
        String favKey=keyForOld(old[1],old[2]);
        boolean favorite=getFavorites().contains(favKey);
        favoriteButton.setText(favorite?"★":"☆");
        favoriteButton.setContentDescription(favorite?"Прибрати день із вибраного":"Додати день до вибраного");
        CalendarData.Day day=data.getOld(old[1],old[2]);
        String headline=cleanHeading(day.feast);
        boolean hasFeast=!headline.isEmpty();
        title.setText(hasFeast?headline:"Пам’яті цього дня");
        specialNote.setVisibility(day.note.isEmpty()?View.GONE:View.VISIBLE);
        specialNote.setText(day.note);
        String body=CalendarData.tidy(day.saints);
        if(hasFeast&&body.equals("• "+headline))body="";
        saints.setVisibility(body.isEmpty()?View.GONE:View.VISIBLE);
        saints.setText(body);
    }
    private void changeDay(int delta){selected.add(Calendar.DAY_OF_MONTH,delta);update();}
    private void goToday(){selected.setTimeInMillis(System.currentTimeMillis());update();}
    private void showDatePicker(){
        DatePickerDialog picker=new DatePickerDialog(this,(v,y,m,d)->{
            selected.set(y,m,d,12,0,0);update();
        },selected.get(Calendar.YEAR),selected.get(Calendar.MONTH),selected.get(Calendar.DAY_OF_MONTH));
        picker.setTitle("Оберіть дату за новим стилем");picker.show();
    }
    private void showMenu(View anchor){
        PopupMenu p=new PopupMenu(this,anchor);
        p.getMenu().add(0,1,0,"Сьогодні");
        p.getMenu().add(0,2,1,"Календар місяця");
        p.getMenu().add(0,3,2,"Пошук у календарі");
        p.getMenu().add(0,4,3,"Розмір тексту");
        p.getMenu().add(0,5,4,"Оформлення");
        p.getMenu().add(0,6,5,"Поділитися днем");
        p.getMenu().add(0,7,6,"Про застосунок");
        p.getMenu().add(0,8,7,"Вибрані дні");
        p.getMenu().add(0,9,8,"Пасха та рухомі свята");
        p.setOnMenuItemClickListener(item->{
            switch(item.getItemId()){
                case 1:goToday();break;
                case 2:showMonthPicker();break;
                case 3:showSearchDialog();break;
                case 4:showFontDialog();break;
                case 5:showThemeDialog();break;
                case 6:shareToday();break;
                case 7:showAbout();break;
                case 8:showFavorites();break;
                case 9:showPaschaInfo();break;
            }return true;
        });p.show();
    }
    private void showFontDialog(){
        String[] sizes={"16 sp · компактний","18 sp · стандартний","20 sp · збільшений","24 sp · великий","28 sp · дуже великий"};
        int[] vals={16,18,20,24,28};int picked=1;
        for(int i=0;i<vals.length;i++)if(vals[i]==fontSize)picked=i;
        new AlertDialog.Builder(this).setTitle("Розмір тексту пам’ятей")
            .setSingleChoiceItems(sizes,picked,(dialog,which)->{
                prefs.edit().putInt("fontSize",vals[which]).apply();dialog.dismiss();build();
            }).setNegativeButton("Закрити",null).show();
    }
    private void showThemeDialog(){
        new AlertDialog.Builder(this).setTitle("Оформлення")
            .setSingleChoiceItems(THEME_NAMES,prefs.getInt("theme",0),(dialog,index)->{
                prefs.edit().putInt("theme",index).apply();dialog.dismiss();build();
            }).setNegativeButton("Закрити",null).show();
    }
    private void showSearchDialog(){
        EditText input=new EditText(this);input.setSingleLine(true);input.setHint("Наприклад: Параскеви");
        input.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        LinearLayout form=vertical();form.setPadding(dp(24),dp(8),dp(24),0);form.addView(input);
        new AlertDialog.Builder(this).setTitle("Пошук пам’ятей святих")
            .setView(form).setPositiveButton("Знайти",(di,i)->showResults(input.getText().toString()))
            .setNegativeButton("Скасувати",null).show();
    }
    private void showResults(String query){
        ArrayList<CalendarData.SearchHit> hits=data.search(query);
        if(hits.isEmpty()){
            new AlertDialog.Builder(this).setTitle("Результати пошуку")
                .setMessage("Нічого не знайдено. Перевірте написання.")
                .setPositiveButton("OK",null).show();return;
        }
        String[] options=new String[hits.size()];
        for(int i=0;i<hits.size();i++){
            CalendarData.SearchHit h=hits.get(i);
            String hline=h.value.headline();
            if(hline.length()>90)hline=hline.substring(0,90)+"…";
            options[i]=h.day+" "+CalendarData.MONTHS[h.month-1]+" · "+hline;
        }
        new AlertDialog.Builder(this).setTitle("Знайдено: "+hits.size())
            .setItems(options,(di,i)->{
                CalendarData.SearchHit h=hits.get(i);
                goToOldDate(h.month,h.day);
            }).setNegativeButton("Закрити",null).show();
    }
    private void shareToday(){
        int[] old=CalendarMath.gregorianToJulian(selected.get(Calendar.YEAR),selected.get(Calendar.MONTH)+1,selected.get(Calendar.DAY_OF_MONTH));
        CalendarData.Day d=data.getOld(old[1],old[2]);
        String mobile=LiturgicalCalendar.movableFeast(selected.get(Calendar.YEAR),selected.get(Calendar.MONTH)+1,selected.get(Calendar.DAY_OF_MONTH));
        String msg=old[2]+" "+CalendarData.MONTHS[old[1]-1]+" (ст. ст.)\n\n"+
            (mobile.isEmpty()?"":mobile+"\n\n")+
            (d.feast.isEmpty()?"":cleanHeading(d.feast)+"\n\n")+CalendarData.tidy(d.saints);
        Intent send=new Intent(Intent.ACTION_SEND);send.setType("text/plain");send.putExtra(Intent.EXTRA_TEXT,msg);
        startActivity(Intent.createChooser(send,"Поділитися днем"));
    }
    private void showAbout(){
        new AlertDialog.Builder(this).setTitle("Календар УПЦ КП · 3.0")
            .setMessage("Новий нативний застосунок для Android 7 (API 24) і новіших.\n\n"+
                "Розробник: Vasyl Shlemko\n"+
                "Оригінальна база пам’ятей: наданий календар 1.1uk.\n\n"+
                "Дати — за юліанським (старим) стилем, із точним перерахунком на григоріанський календар. " +
                "Пости й рухомі свята розраховано довідково; звіряйте з офіційним річним календарем УПЦ КП. Застосунок не є офіційним виданням церкви.\n\n"+
                "Застосунок працює повністю офлайн. Дані не передаються в мережу.")
            .setPositiveButton("Гаразд",null).show();
    }

    private void showMonthPicker() {
        CalendarView month = new CalendarView(this);
        month.setDate(selected.getTimeInMillis());
        final Calendar candidate=(Calendar) selected.clone();
        month.setOnDateChangeListener((view,y,m,d)->candidate.set(y,m,d,12,0,0));
        LinearLayout container=vertical();container.setPadding(dp(10),dp(4),dp(10),0);
        container.addView(month,lp(-1,-2));
        new AlertDialog.Builder(this).setTitle("Оберіть день місяця")
            .setView(container)
            .setPositiveButton("Перейти",(dialog,which)->{
                selected.setTimeInMillis(candidate.getTimeInMillis());update();
            }).setNeutralButton("Ввести дату",(dialog,which)->showDatePicker())
            .setNegativeButton("Скасувати",null).show();
    }

    private static String keyForOld(int month,int day) {
        return String.format(Locale.ROOT,"%02d-%02d",month,day);
    }
    private Set<String> getFavorites() {
        return new HashSet<>(prefs.getStringSet("favorite_old_dates",new HashSet<>()));
    }
    private void toggleFavorite() {
        int[] old=CalendarMath.gregorianToJulian(selected.get(Calendar.YEAR),
            selected.get(Calendar.MONTH)+1,selected.get(Calendar.DAY_OF_MONTH));
        String key=keyForOld(old[1],old[2]);
        Set<String> favorites=getFavorites();
        boolean added;
        if (favorites.contains(key)) {favorites.remove(key);added=false;}
        else {favorites.add(key);added=true;}
        prefs.edit().putStringSet("favorite_old_dates",favorites).apply();
        Toast.makeText(this,added?"День додано до вибраного":"День прибрано з вибраного",Toast.LENGTH_SHORT).show();
        update();
    }
    private void showFavorites() {
        Set<String> favoriteDates=new TreeSet<>(getFavorites());
        if(favoriteDates.isEmpty()) {
            new AlertDialog.Builder(this).setTitle("Вибрані дні")
                .setMessage("Натисніть ☆ угорі, щоб зберегти дату за старим стилем.")
                .setPositiveButton("Гаразд",null).show();return;
        }
        ArrayList<String> keys=new ArrayList<>(favoriteDates);
        String[] captions=new String[keys.size()];
        for(int i=0;i<keys.size();i++) {
            String[] parts=keys.get(i).split("-");
            int month=Integer.parseInt(parts[0]),day=Integer.parseInt(parts[1]);
            captions[i]=day+" "+CalendarData.MONTHS[month-1]+" — "+data.getOld(month,day).headline();
        }
        new AlertDialog.Builder(this).setTitle("Вибрані дні")
            .setItems(captions,(dialog,which)->{
                String[] parts=keys.get(which).split("-");
                goToOldDate(Integer.parseInt(parts[0]),Integer.parseInt(parts[1]));
            }).setNegativeButton("Закрити",null).show();
    }
    private void goToOldDate(int month,int day) {
        int[] julian=CalendarMath.gregorianToJulian(selected.get(Calendar.YEAR),
            selected.get(Calendar.MONTH)+1,selected.get(Calendar.DAY_OF_MONTH));
        int year=julian[0];
        if(month==2 && day==29)while(year%4!=0)year++;
        int[] civil=CalendarMath.julianToGregorian(year,month,day);
        selected.set(civil[0],civil[1]-1,civil[2],12,0,0);update();
    }
    private void showPaschaInfo() {
        int year=selected.get(Calendar.YEAR);
        int p=CalendarMath.orthodoxPaschaJdn(year);
        int[] easter=CalendarMath.fromJulianDayGregorian(p);
        int[] palms=CalendarMath.fromJulianDayGregorian(p-7);
        int[] ascension=CalendarMath.fromJulianDayGregorian(p+39);
        int[] pentecost=CalendarMath.fromJulianDayGregorian(p+49);
        String message=year+" рік\n\n"
            + "Вербна неділя: "+shortDate(palms)+"\n"
            + "Пасха: "+shortDate(easter)+"\n"
            + "Вознесіння: "+shortDate(ascension)+"\n"
            + "П’ятидесятниця: "+shortDate(pentecost)
            + "\n\nДати обчислено за православною пасхалією (юліанським календарем).";
        new AlertDialog.Builder(this).setTitle("Рухомі свята")
            .setMessage(message).setPositiveButton("Гаразд",null).show();
    }
    private String shortDate(int[] v) {
        return v[2]+" "+CalendarData.MONTHS[v[1]-1]+" "+v[0];
    }

}
