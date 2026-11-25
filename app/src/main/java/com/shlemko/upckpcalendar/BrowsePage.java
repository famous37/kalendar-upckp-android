package com.shlemko.upckpcalendar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import static com.shlemko.upckpcalendar.CalendarUi.*;

final class BrowsePage {
    private final MainActivity a;
    private final CalendarUi ui;
    private CalendarIndex index;
    private ListView list;
    private TextView section, emptyTitle, emptyHint;
    private LinearLayout empty;
    private Rows rows;
    private ImageButton clear;

    BrowsePage(MainActivity a) { this.a = a; ui = a.ui; }
    View build(boolean search) {
        LinearLayout page = ui.page();
        page.setPadding(page.getPaddingLeft(), page.getPaddingTop(), page.getPaddingRight(), 0);
        page.addView(ui.serif(search ? "Пошук" : "Вибрані дні", 32, ui.ink));
        if (search) addSearchBox(page);
        section = ui.small("", ui.accent);
        ui.above(page, section, 16);
        list = new ListView(a);
        list.setDivider(new ColorDrawable(ui.border));
        list.setDividerHeight(Math.max(1, ui.dp(.75f)));
        list.setVerticalScrollBarEnabled(false);
        list.setCacheColorHint(Color.TRANSPARENT);
        rows = new Rows();
        list.setAdapter(rows);
        list.setOnItemClickListener((parent, v, pos, id) -> a.selectDay(rows.items.get(pos).date.jdn));
        page.addView(list, new LinearLayout.LayoutParams(MATCH, 0, 1));
        empty = ui.column();
        empty.setGravity(Gravity.CENTER);
        ui.pad(empty, 16, 18, 16, 32);
        empty.addView(ui.image(search ? R.drawable.ic_search : R.drawable.ic_bookmark, ui.accent), ui.box(34,34));
        emptyTitle = ui.serif("", 23, ui.ink);
        emptyTitle.setGravity(Gravity.CENTER);
        ui.above(empty, emptyTitle, 15);
        emptyHint = ui.text("", 15, ui.muted);
        emptyHint.setGravity(Gravity.CENTER);
        ui.above(empty, emptyHint, 7);
        page.addView(empty, new LinearLayout.LayoutParams(MATCH, 0, 1));

        if (search) {
            if (index == null || index.year != a.date.civil[0]) index = new CalendarIndex(a.data, a.date.civil[0]);
            filter(a.query);
        } else {
            ArrayList<CalendarIndex.Result> hits = new ArrayList<>();
            for (String key : new TreeSet<>(a.favorites())) {
                int jdn = ChurchDate.favoriteInYear(key, a.date.old[0]);
                if (jdn == 0) continue;
                ChurchDate date = new ChurchDate(jdn);
                String movable = date.movable();
                hits.add(new CalendarIndex.Result(date, movable.isEmpty() ? date.record(a.data).headline() : movable));
            }
            display(hits, "ЗА СТАРИМ СТИЛЕМ", "Ще немає вибраних днів", "Торкніться закладки на сторінці дня, щоб зберегти його тут.");
        }
        list.setSelectionFromTop(a.browseFirst, a.browseTop);
        return page;
    }
    private void addSearchBox(LinearLayout page) {
        LinearLayout box = ui.row();
        box.setBackground(ui.shape(ui.surface, 12, ui.border));
        ui.pad(box, 13, 0, 0, 0);
        box.addView(ui.image(R.drawable.ic_search, ui.muted), ui.box(22,22));
        EditText input = new EditText(a);
        input.setSingleLine();
        input.setTextSize(16);
        input.setTextColor(ui.ink);
        input.setHintTextColor(ui.muted);
        input.setHint("Ім’я святого або назва свята");
        input.setContentDescription("Пошук у календарі");
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        input.setBackgroundColor(Color.TRANSPARENT);
        ui.pad(input, 10, 6, 0, 6);
        input.setMinHeight(ui.dp(50));
        input.setText(a.query);
        input.setSelection(input.length());
        input.setOnEditorActionListener((v, action, event) -> {
            if (action != EditorInfo.IME_ACTION_SEARCH) return false;
            ((InputMethodManager)a.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
            return true;
        });
        box.addView(input, ui.flex());
        clear = ui.icon(R.drawable.ic_close, ui.muted, "Очистити пошук", () -> input.setText(""));
        box.addView(clear, ui.box(48,48));
        ui.above(page, box, 13);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                a.query = s.toString();
                filter(a.query);
                list.setSelection(0);
            }
            @Override public void afterTextChanged(Editable e) {}
        });
    }
    private void filter(String value) {
        List<CalendarIndex.Result> hits = index.find(value);
        clear.setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
        String title = value.trim().isEmpty() ? "СВЯТА · " + index.year : "ЗНАЙДЕНО " + hits.size();
        display(hits, title, "Нічого не знайдено", "Спробуйте частину імені, наприклад «Микола».");
    }
    private void display(List<CalendarIndex.Result> hits, String title, String headline, String hint) {
        rows.items = hits;
        rows.notifyDataSetChanged();
        section.setText(title);
        section.setVisibility(hits.isEmpty() ? View.GONE : View.VISIBLE);
        list.setVisibility(hits.isEmpty() ? View.GONE : View.VISIBLE);
        empty.setVisibility(hits.isEmpty() ? View.VISIBLE : View.GONE);
        emptyTitle.setText(headline);
        emptyHint.setText(hint);
    }
    void remember() {
        if (list == null) return;
        a.browseFirst = list.getFirstVisiblePosition();
        a.browseTop = list.getChildCount() == 0 ? 0 : list.getChildAt(0).getTop();
    }
    private final class Rows extends BaseAdapter {
        List<CalendarIndex.Result> items = Collections.emptyList();
        @Override public int getCount() { return items.size(); }
        @Override public CalendarIndex.Result getItem(int index) { return items.get(index); }
        @Override public long getItemId(int index) { return getItem(index).date.jdn; }
        @Override public View getView(int index, View recycled, ViewGroup parent) {
            LinearLayout row;
            TextView label, dates;
            if (recycled == null) {
                row = ui.column();
                ui.pad(row, 0, 13, 4, 13);
                dates = ui.text("", 13, ui.accent);
                dates.setTypeface(MEDIUM);
                label = ui.serif("", 18, ui.ink);
                label.setMaxLines(3);
                label.setEllipsize(TextUtils.TruncateAt.END);
                row.addView(dates);
                ui.above(row, label, 4);
            } else {
                row = (LinearLayout) recycled;
                dates = (TextView)row.getChildAt(0);
                label = (TextView)row.getChildAt(1);
            }
            CalendarIndex.Result value = getItem(index);
            ChurchDate d = value.date;
            dates.setText(ChurchDate.format(d.old) + " ст. ст. · " + ChurchDate.full(d.civil));
            label.setText(value.label);
            return row;
        }
    }
}
