package com.shlemko.upckpcalendar;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import static com.shlemko.upckpcalendar.CalendarUi.*;

final class InfoPages {
    private static final String[] THEMES = {"Системна", "Світла", "Темна"};
    private static final String[] SIZES = {"Малий", "Стандартний", "Збільшений", "Великий", "Дуже великий"};
    private static final int[] SP = {16,18,20,24,28};
    private final MainActivity a;
    private final CalendarUi ui;
    InfoPages(MainActivity a) { this.a = a; ui = a.ui; }
    View settings() {
        LinearLayout page = ui.page();
        int theme = a.prefs.getInt("theme", 0);
        if (theme < 0 || theme >= THEMES.length) theme = 0;
        int size = 1;
        for (int i = 0; i < SP.length; i++) if (a.fontSize() == SP[i]) size = i;
        final int selectedTheme = theme, selectedSize = size;
        page.addView(ui.link(R.drawable.ic_sun, "Оформлення", THEMES[theme], () ->
                new AlertDialog.Builder(a).setTitle("Оформлення").setSingleChoiceItems(THEMES, selectedTheme, (d,i) -> {
                    a.prefs.edit().putInt("theme",i).apply();
                    d.dismiss();
                    CalendarWidget.refreshAll(a);
                    a.recreate();
                }).setNegativeButton("Скасувати", null).show()));
        ui.rule(page);
        page.addView(ui.link(R.drawable.ic_text_size, "Розмір тексту", SIZES[size], () ->
                new AlertDialog.Builder(a).setTitle("Розмір тексту").setSingleChoiceItems(SIZES, selectedSize, (d,i) -> {
                    a.prefs.edit().putInt("fontSize",SP[i]).apply();
                    d.dismiss();
                    a.render();
                }).setNegativeButton("Скасувати", null).show()));
        ui.rule(page);
        page.addView(ui.link(R.drawable.ic_info, "Про застосунок", "", a::about));
        return ui.scroll(page);
    }
    View about() {
        LinearLayout page = ui.page();
        LinearLayout cover = ui.column();
        cover.setGravity(Gravity.CENTER);
        ui.pad(cover, 18, 20, 18, 22);
        cover.setBackground(ui.shape(WINE,18,0));
        cover.addView(ui.image(R.drawable.church_engraving, 0), ui.box(105,98));
        TextView name = ui.serif(a.getString(R.string.app_name), 28, PAPER);
        name.setGravity(Gravity.CENTER);
        ui.above(cover, name, 12);
        TextView version = ui.text("Версія " + BuildConfig.VERSION_NAME.replace("-beta.", " · Бета "), 14, GOLD);
        version.setGravity(Gravity.CENTER);
        ui.above(cover, version, 6);
        page.addView(cover);
        TextView dedication = ui.serif(a.getString(R.string.dedication), 23, ui.ink);
        dedication.setLineSpacing(ui.dp(3), 1f);
        ui.pad(dedication, 0, 22, 0, 18);
        page.addView(dedication);
        ui.rule(page);
        ui.above(page, ui.text(a.getString(R.string.calendar_source), 16, ui.muted), 18);
        TextView author = ui.text(a.getString(R.string.developer_credit), 16, ui.ink);
        author.setTypeface(MEDIUM);
        ui.above(page, author, 20);
        return ui.scroll(page);
    }
    static void pascha(MainActivity a, int year) {
        int easter = CalendarMath.orthodoxPaschaJdn(year);
        String[] names = {"Вербна неділя", "Пасха", "Вознесіння Господнє", "П’ятидесятниця"};
        int[] offsets = {-7, 0, 39, 49};
        String[] options = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            ChurchDate d = new ChurchDate(easter + offsets[i]);
            options[i] = names[i] + "\n" + ChurchDate.full(d.civil);
        }
        new AlertDialog.Builder(a).setTitle("Пасхалія · " + year)
                .setItems(options, (dialog,i) -> a.selectDay(easter+offsets[i]))
                .setNegativeButton("Закрити", null).show();
    }
}
