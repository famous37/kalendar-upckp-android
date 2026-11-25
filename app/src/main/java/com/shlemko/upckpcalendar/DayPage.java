package com.shlemko.upckpcalendar;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONObject;
import static com.shlemko.upckpcalendar.CalendarUi.*;

final class DayPage {
    private final MainActivity a;
    private final CalendarUi ui;
    private final ChurchDate date;
    DayPage(MainActivity a) {
        this.a = a;
        ui = a.ui;
        date = a.date;
    }
    View build() {
        LinearLayout page = ui.page();
        page.addView(cover());
        LinearLayout controls = ui.row();
        controls.addView(ui.icon(R.drawable.ic_left, ui.ink, "Попередній день", () -> a.selectDay(date.jdn - 1)), ui.box(48,48));
        TextView today = ui.button("Сьогодні", () -> a.selectDay(ChurchDate.today()));
        today.setBackground(ui.pressed(0, 12, 0));
        controls.addView(today, ui.flex());
        controls.addView(ui.icon(R.drawable.ic_right, ui.ink, "Наступний день", () -> a.selectDay(date.jdn + 1)), ui.box(48,48));
        page.addView(controls);
        ui.rule(page);

        CalendarData.Day record = date.record(a.data);
        String movable = date.movable();
        if (!movable.isEmpty() || !record.feast.isEmpty() || !record.note.isEmpty()) {
            LinearLayout feast = ui.column();
            if (!movable.isEmpty() || !record.feast.isEmpty())
                feast.addView(ui.small("СВЯТО ДНЯ", ui.accent));
            if (!movable.isEmpty()) ui.above(feast, ui.serif(movable, a.fontSize()+3, ui.ink), 6);
            if (!record.feast.isEmpty()) ui.above(feast, ui.serif(record.feast, a.fontSize()+3, ui.ink), 6);
            if (!record.note.isEmpty()) ui.above(feast, ui.text(record.note, a.fontSize()-1, ui.accent), 4);
            ui.above(page, feast, 18);
        }
        String fasting = date.fasting();
        if (!LiturgicalCalendar.NO_FAST_MARK.equals(fasting)) {
            LinearLayout fast = ui.row();
            ui.pad(fast,12,10,12,10);
            fast.setBackground(ui.shape(ui.blush, 10, 0));
            fast.addView(ui.image(R.drawable.ic_leaf, ui.accent), ui.box(19,19));
            TextView text = ui.text(fasting, 14, ui.accent);
            ui.pad(text,9,0,0,0);
            fast.addView(text, ui.flex());
            ui.above(page, fast, 14);
        }
        if (!record.saints.isEmpty()) {
            ui.heading(page, "Святі дня");
            LinearLayout entries = ui.column();
            for (int i = 0; i < record.saints.size(); i++) {
                if (i > 0) ui.rule(entries);
                TextView line = ui.serif(record.saints.get(i), a.fontSize() + (i == 0 ? 2 : 0), ui.ink);
                line.setTextIsSelectable(true);
                ui.pad(line, 0, 12, 0, 12);
                entries.addView(line);
            }
            page.addView(entries);
        }
        if (date.civil[0] == 2018) addReadings(page);
        ui.rule(page);
        page.addView(ui.link(R.drawable.ic_share, "Поділитися днем", "", a::share));
        return ui.scroll(page);
    }
    private LinearLayout cover() {
        LinearLayout card = ui.column();
        GradientDrawable cover = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xff49212e, 0xff602a39, 0xff71384a});
        cover.setCornerRadius(ui.dp(18));
        card.setBackground(cover);
        ui.pad(card, 18, 16, 16, 10);

        LinearLayout head = ui.row();
        TextView weekday = ui.small(date.weekday().toUpperCase(MainActivity.UK), GOLD);
        head.addView(weekday, ui.flex());
        boolean saved = a.favorites().contains(date.favoriteKey());
        ImageButton bookmark = ui.icon(R.drawable.ic_bookmark, GOLD, "", () -> {});
        bookmark.setBackground(ui.pressed(0, 24, 0));
        paintBookmark(bookmark, saved);
        bookmark.setOnClickListener(v -> paintBookmark(bookmark, a.toggleFavorite()));
        head.addView(bookmark, ui.box(48,48));
        card.addView(head);

        LinearLayout main = ui.row();
        LinearLayout type = ui.column();
        type.addView(ui.serif(ChurchDate.format(date.old), 36, PAPER));
        ui.above(type, ui.text("За старим стилем · " + date.old[0], 14, GOLD), 5);
        main.addView(type, ui.flex());
        int icon = FeastIcon.forDate(date);
        ImageView church = ui.image(icon == 0 ? R.drawable.church_engraving : icon, 0);
        int available = a.getResources().getConfiguration().screenWidthDp;
        int w = available < 360 ? 66 : 88;
        if (a.getResources().getConfiguration().fontScale > 1.35f) w = 50;
        ui.pad(church, 6, 0, 0, 0);
        main.addView(church, ui.box(w, 85));
        card.addView(main);

        View rule = new View(a);
        rule.setBackgroundColor(0x55e6c290);
        LinearLayout.LayoutParams line = new LinearLayout.LayoutParams(MATCH, Math.max(1, ui.dp(.75f)));
        line.topMargin = ui.dp(12);
        card.addView(rule, line);
        LinearLayout civil = ui.row();
        ui.pad(civil,0,6,0,6);
        civil.setMinimumHeight(ui.dp(48));
        civil.addView(ui.image(R.drawable.ic_month, GOLD), ui.box(18,18));
        TextView label = ui.text(ChurchDate.full(date.civil) + " · цивільна дата", 14, PAPER);
        ui.pad(label,9,0,6,0);
        civil.addView(label, ui.flex());
        civil.addView(ui.image(R.drawable.ic_right, GOLD), ui.box(17,17));
        civil.setContentDescription("Обрати цивільну дату, " + ChurchDate.full(date.civil));
        civil.setFocusable(true);
        civil.setBackground(ui.pressed(0, 6, 0));
        civil.setOnClickListener(v -> a.picker(date.jdn));
        card.addView(civil);
        return card;
    }
    private void paintBookmark(ImageButton button, boolean saved) {
        button.setImageResource(saved ? R.drawable.ic_bookmark_saved : R.drawable.ic_bookmark);
        button.setImageTintList(ColorStateList.valueOf(GOLD));
        button.setContentDescription(saved ? "Прибрати день із вибраного" : "Додати день до вибраного");
        button.setSelected(saved);
    }
    private void addReadings(LinearLayout page) {
        try {
            JSONObject entry = a.readings();
            if (entry == null) return;
            String lines = entry.optString("scripture_lines_2018").replace("\\n", "\n").trim();
            if (lines.isEmpty()) return;
            ui.heading(page, "Читання дня");
            TextView text = ui.serif(lines, a.fontSize(), ui.ink);
            text.setTextIsSelectable(true);
            ui.above(page, text, 9);
            JSONArray pages = entry.getJSONArray("source_pages");
            int first = pages.getInt(0), last = pages.getInt(1);
            TextView source = ui.text("Календар УПЦ КП, 2018 · с. " + first + (first == last ? "" : "–" + last), 13, ui.muted);
            ui.pad(source,0,6,0,14);
            page.addView(source);
        } catch (Exception e) {
            ui.above(page, ui.text("Не вдалося відкрити читання дня.", 14, ui.muted), 9);
        }
    }
}
