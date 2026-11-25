package com.shlemko.upckpcalendar;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import static com.shlemko.upckpcalendar.CalendarUi.*;

final class MonthPage {
    private static final String[] WEEK = {"Пн","Вт","Ср","Чт","Пт","Сб","Нд"};
    private final MainActivity a;
    private final CalendarUi ui;
    private final int year, month, start;
    MonthPage(MainActivity a) {
        this.a = a;
        ui = a.ui;
        int[] anchor = CalendarMath.fromJulianDayGregorian(a.monthAnchor);
        year = anchor[0];
        month = anchor[1];
        start = CalendarMath.gregorianToJdn(year, month, 1);
    }
    View build() {
        LinearLayout page = ui.page();
        LinearLayout title = ui.row();
        TextView heading = ui.serif(ChurchDate.MONTH_NAMES[month-1] + " " + year, 28, ui.ink);
        heading.setMinimumHeight(ui.dp(48));
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setFocusable(true);
        heading.setContentDescription("Обрати дату, " + heading.getText());
        heading.setOnClickListener(v -> a.picker(a.monthAnchor));
        title.addView(heading, ui.flex());
        title.addView(ui.icon(R.drawable.ic_left, ui.ink, "Попередній місяць", () -> shift(-1)), ui.box(48,48));
        title.addView(ui.icon(R.drawable.ic_right, ui.ink, "Наступний місяць", () -> shift(1)), ui.box(48,48));
        page.addView(title);
        LinearLayout grid = ui.column();
        ui.pad(grid, 3, 7, 3, 8);
        grid.setBackground(ui.shape(ui.surface, 16, ui.border));
        LinearLayout weekdays = ui.row();
        for (int i = 0; i < 7; i++) {
            TextView name = ui.small(WEEK[i], i == 6 ? ui.accent : ui.muted);
            name.setGravity(Gravity.CENTER);
            ui.pad(name,0,7,0,7);
            weekdays.addView(name, ui.flex());
        }
        grid.addView(weekdays);
        ui.rule(grid);
        int[] cells = MonthGrid.days(year, month);
        int today = ChurchDate.today();
        ArrayList<Integer> feasts = new ArrayList<>();
        for (int offset = 0; offset < cells.length; offset += 7) {
            if (offset > 0 && cells[offset] == 0) break;
            LinearLayout week = ui.row();
            for (int i = 0; i < 7; i++) {
                int d = cells[offset + i];
                LinearLayout cell = ui.column();
                cell.setGravity(Gravity.CENTER);
                ui.pad(cell,0,4,0,4);
                cell.setMinimumHeight(ui.dp(54));
                if (d > 0) {
                    ChurchDate date = new ChurchDate(start + d - 1);
                    boolean feast = !date.record(a.data).feast.isEmpty() || !date.movable().isEmpty();
                    if (feast) feasts.add(date.jdn);
                    boolean selected = date.jdn == a.date.jdn;
                    boolean current = date.jdn == today;
                    cell.setBackground(ui.pressed(selected ? WINE : 0, 11, current ? (selected ? GOLD : ui.accent) : 0));
                    cell.addView(ui.serif(String.valueOf(d), 20, selected ? PAPER : i == 6 ? ui.accent : ui.ink), ui.box(WRAP, WRAP));
                    cell.addView(ui.text(String.valueOf(date.old[2]), 11, selected ? GOLD : ui.muted), ui.box(WRAP, WRAP));
                    View dot = new View(a);
                    int color = feast ? selected ? GOLD : ui.accent : 0;
                    dot.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    dot.setBackground(ui.shape(color, 2, 0));
                    cell.addView(dot, ui.box(4,3));
                    cell.setContentDescription(date.weekday() + ", " + ChurchDate.full(date.civil) +
                            ", за старим стилем " + ChurchDate.format(date.old) +
                            (current ? ", сьогодні" : "") + (feast ? ", свято" : ""));
                    cell.setFocusable(true);
                    cell.setSelected(selected);
                    cell.setOnClickListener(v -> a.selectDay(date.jdn));
                }
                week.addView(cell, ui.flex());
            }
            grid.addView(week);
        }
        ui.above(page, grid, 8);
        ui.above(page, ui.text("Верхнє число — цивільна дата, нижнє — за старим стилем. Крапкою позначено свята.", 13, ui.muted), 9);
        if (!feasts.isEmpty()) {
            ui.heading(page, "Свята місяця");
            for (int jdn : feasts) {
                ChurchDate d = new ChurchDate(jdn);
                String movable = d.movable(), fixed = d.record(a.data).feast;
                String name = movable.isEmpty() ? fixed : fixed.isEmpty() ? movable : movable + " · " + fixed;
                page.addView(ui.link(0, name, ChurchDate.format(d.civil) + " · " + d.weekday(), () -> a.selectDay(jdn)));
                ui.rule(page);
            }
        }
        page.addView(ui.link(R.drawable.ic_cross_book, "Пасхалія на " + year + " рік", "", () -> InfoPages.pascha(a, year)));
        return ui.scroll(page);
    }
    private void shift(int delta) {
        a.monthAnchor = delta < 0 ? start - 1 : start + MonthGrid.daysInMonth(year, month);
        a.render();
    }
}
