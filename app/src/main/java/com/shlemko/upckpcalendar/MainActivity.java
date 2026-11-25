package com.shlemko.upckpcalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import static com.shlemko.upckpcalendar.CalendarUi.*;

public final class MainActivity extends Activity {
    enum Screen { DAY, MONTH, SEARCH, FAVORITES, SETTINGS, ABOUT }
    private static final Screen[] TABS = {Screen.DAY, Screen.MONTH, Screen.SEARCH, Screen.FAVORITES};
    private static final String[] TAB_NAMES = {"День", "Місяць", "Пошук", "Вибране"};
    private static final int[] TAB_ICONS = {R.drawable.ic_book, R.drawable.ic_month, R.drawable.ic_search, R.drawable.ic_bookmark};
    static final Locale UK = Locale.forLanguageTag("uk-UA");
    CalendarUi ui;
    CalendarData data;
    SharedPreferences prefs;
    ChurchDate date;
    int monthAnchor, dayScroll, browseFirst, browseTop;
    String query = "";
    Screen screen = Screen.DAY;
    private Screen detailOrigin = Screen.DAY, settingsOrigin = Screen.DAY;
    private int observedToday;
    private LinearLayout root, bar, tabs;
    private FrameLayout stage;
    private View pageView;
    private BrowsePage browse;
    private SourceYear2018 readings;

    private final BroadcastReceiver clock = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) { checkToday(); }
    };

    @Override protected void attachBaseContext(Context base) {
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(UK);
        super.attachBaseContext(base.createConfigurationContext(config));
    }
    @Override public void onCreate(Bundle saved) {
        UiTheme.apply(this);
        super.onCreate(saved);
        prefs = getSharedPreferences("calendar_settings", MODE_PRIVATE);
        try { data = CalendarData.load(this); }
        catch (Exception e) {
            new AlertDialog.Builder(this).setMessage("Не вдалося відкрити календар.")
                    .setPositiveButton("Закрити", (dialog, which) -> finish()).show();
            return;
        }
        observedToday = ChurchDate.today();
        int last = prefs.contains("civil_jdn") ? prefs.getInt("civil_jdn", observedToday)
                : ChurchDate.fromMillis(prefs.getLong("last_selected_day", System.currentTimeMillis()));
        if (last == prefs.getInt("seen_today", observedToday) || getIntent().getBooleanExtra("open_today", false)) last = observedToday;
        date = new ChurchDate(last);
        dayScroll = last == prefs.getInt("civil_jdn", last) ? prefs.getInt("last_scroll", 0) : 0;
        monthAnchor = date.jdn;
        if (saved != null) {
            date = new ChurchDate(saved.getInt("day", date.jdn));
            monthAnchor = saved.getInt("month", date.jdn);
            screen = Screen.valueOf(saved.getString("screen", Screen.DAY.name()));
            detailOrigin = Screen.valueOf(saved.getString("detailOrigin", Screen.DAY.name()));
            settingsOrigin = Screen.valueOf(saved.getString("settingsOrigin", Screen.DAY.name()));
            dayScroll = saved.getInt("scroll", 0);
            query = saved.getString("query", "");
            browseFirst = saved.getInt("browseFirst");
            browseTop = saved.getInt("browseTop");
        }
        ui = new CalendarUi(this);
        browse = new BrowsePage(this);
        buildShell();
        render();
        if (Build.VERSION.SDK_INT >= 33)
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, this::back);
    }
    private void buildShell() {
        root = ui.column();
        root.setBackgroundColor(ui.bg);
        root.setFocusableInTouchMode(true);
        bar = ui.row();
        ui.pad(bar, 14, 3, 8, 3);
        root.addView(bar, ui.box(MATCH, WRAP));
        ui.rule(root);
        stage = new FrameLayout(this);
        root.addView(stage, new LinearLayout.LayoutParams(MATCH, 0, 1));
        ui.rule(root);
        tabs = ui.row();
        ui.pad(tabs, 6, 4, 6, 4);
        tabs.setBackgroundColor(ui.bg);
        root.addView(tabs, ui.box(MATCH, WRAP));
        if (Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        getWindow().setStatusBarColor(ui.bg);
        getWindow().setNavigationBarColor(!ui.dark && Build.VERSION.SDK_INT < 26 ? WINE : ui.bg);
        int flags = ui.dark ? 0 : View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if (!ui.dark && Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
        if (Build.VERSION.SDK_INT >= 29) getWindow().setNavigationBarContrastEnforced(false);
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener((v, inset) -> {
                android.graphics.Insets bars = inset.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                int keyboard = inset.getInsets(WindowInsets.Type.ime()).bottom;
                v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, keyboard));
                tabs.setVisibility(keyboard > bars.bottom ? View.GONE : View.VISIBLE);
                return inset;
            });
            root.requestApplyInsets();
        }
    }
    private void toolbar() {
        bar.removeAllViews();
        boolean nested = screen == Screen.SETTINGS || screen == Screen.ABOUT;
        if (nested) bar.addView(ui.icon(R.drawable.ic_left, ui.ink, "Назад", this::back), ui.box(48,48));
        else {
            ImageView mark = ui.image(R.drawable.ic_cross_book, ui.accent);
            ui.pad(mark, 0, 1, 0, 1);
            bar.addView(mark, ui.box(30,32));
        }
        String title = screen == Screen.ABOUT ? "Про застосунок" : screen == Screen.SETTINGS ? "Налаштування" : getString(R.string.app_name);
        TextView name = ui.serif(title, 21, ui.ink);
        ui.pad(name, nested ? 2 : 11, 5, 0, 5);
        bar.addView(name, ui.flex());
        if (!nested) bar.addView(ui.icon(R.drawable.ic_settings, ui.ink, "Налаштування", () -> {
            settingsOrigin = screen;
            navigate(Screen.SETTINGS);
        }), ui.box(48,48));
    }
    private void navigation() {
        tabs.removeAllViews();
        for (int i = 0; i < TABS.length; i++) {
            Screen target = TABS[i];
            boolean active = screen == target;
            LinearLayout item = ui.column();
            item.setGravity(Gravity.CENTER);
            item.setMinimumHeight(ui.dp(52));
            item.setBackground(ui.pressed(0, 14, 0));
            ImageView glyph = ui.image(TAB_ICONS[i], active ? ui.accent : ui.muted);
            ui.pad(glyph, 12, 2, 12, 2);
            if (active) glyph.setBackground(ui.shape(ui.blush, 14, 0));
            item.addView(glyph, ui.box(50,26));
            TextView label = ui.text(TAB_NAMES[i], 12, active ? ui.accent : ui.muted);
            label.setGravity(Gravity.CENTER);
            if (active) label.setTypeface(MEDIUM);
            item.addView(label);
            item.setSelected(active);
            item.setFocusable(true);
            item.setContentDescription(TAB_NAMES[i]);
            item.setOnClickListener(v -> {
                if (target == screen) return;
                detailOrigin = Screen.DAY;
                if (target == Screen.MONTH) monthAnchor = date.jdn;
                navigate(target, target == Screen.SEARCH || target == Screen.FAVORITES);
            });
            tabs.addView(item, ui.flex());
        }
    }
    private void remember() {
        if (screen == Screen.DAY && pageView instanceof ScrollView) dayScroll = pageView.getScrollY();
        if (screen == Screen.SEARCH || screen == Screen.FAVORITES) browse.remember();
    }
    void render() {
        toolbar();
        navigation();
        stage.removeAllViews();
        switch (screen) {
            case MONTH: pageView = new MonthPage(this).build(); break;
            case SEARCH: case FAVORITES: pageView = browse.build(screen == Screen.SEARCH); break;
            case SETTINGS: pageView = new InfoPages(this).settings(); break;
            case ABOUT: pageView = new InfoPages(this).about(); break;
            default: pageView = new DayPage(this).build();
        }
        stage.addView(pageView, new FrameLayout.LayoutParams(MATCH, MATCH));
        if (screen == Screen.DAY && dayScroll > 0) {
            View dayPage = pageView;
            int y = dayScroll;
            dayPage.post(() -> dayPage.scrollTo(0, y));
        }
    }
    void navigate(Screen target) { navigate(target, false); }
    private void navigate(Screen target, boolean resetList) {
        remember();
        if (resetList) { browseFirst = 0; browseTop = 0; }
        if (screen == Screen.SEARCH) {
            ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(root.getWindowToken(), 0);
            root.requestFocus();
        }
        screen = target;
        render();
    }
    void selectDay(int jdn) {
        if (screen != Screen.DAY) {
            detailOrigin = screen;
            navigateToDay(jdn);
        } else {
            date = new ChurchDate(jdn);
            dayScroll = 0;
            render();
        }
    }
    private void navigateToDay(int jdn) {
        date = new ChurchDate(jdn);
        dayScroll = 0;
        navigate(Screen.DAY);
    }
    void picker(int initial) {
        int[] c = CalendarMath.fromJulianDayGregorian(initial);
        DatePickerDialog picker = new DatePickerDialog(this, (v, y, m, d) ->
                selectDay(CalendarMath.gregorianToJdn(y, m + 1, d)), c[0], c[1]-1, c[2]);
        picker.setTitle("Цивільна дата");
        picker.show();
    }
    void about() { navigate(Screen.ABOUT); }
    private void back() {
        switch (screen) {
            case ABOUT: navigate(Screen.SETTINGS); break;
            case SETTINGS: navigate(settingsOrigin); break;
            case DAY:
                if (detailOrigin == Screen.DAY) { finish(); return; }
                Screen origin = detailOrigin;
                detailOrigin = Screen.DAY;
                navigate(origin);
                break;
            default:
                detailOrigin = Screen.DAY;
                navigate(Screen.DAY);
        }
    }
    @Override public void onBackPressed() { back(); }
    Set<String> favorites() { return prefs.getStringSet("favorite_old_dates", Collections.emptySet()); }
    boolean toggleFavorite() {
        Set<String> keys = new HashSet<>(favorites());
        boolean added = keys.add(date.favoriteKey());
        if (!added) keys.remove(date.favoriteKey());
        prefs.edit().putStringSet("favorite_old_dates", keys).apply();
        Toast.makeText(this, added ? "День додано до вибраного" : "День вилучено з вибраного", Toast.LENGTH_SHORT).show();
        return added;
    }
    org.json.JSONObject readings() throws Exception {
        if (date.civil[0] != 2018) return null;
        if (readings == null) readings = new SourceYear2018(this);
        return readings.get(date.asCalendar());
    }
    void share() {
        CalendarData.Day item = date.record(data);
        StringBuilder msg = new StringBuilder(date.weekday()).append(", ").append(ChurchDate.full(date.civil))
                .append("\n").append(ChurchDate.full(date.old)).append(" за старим стилем");
        for (String line : new String[]{date.movable(), item.feast, item.note})
            if (!line.isEmpty()) msg.append("\n\n").append(line);
        for (String saint : item.saints) msg.append("\n\n").append(saint);
        Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, msg.toString());
        startActivity(Intent.createChooser(send, "Поділитися днем"));
    }
    int fontSize() { return prefs.getInt("fontSize", 18); }
    @Override protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        if (date == null) return;
        remember();
        out.putInt("day", date.jdn); out.putInt("month", monthAnchor);
        out.putString("screen", screen.name()); out.putString("detailOrigin", detailOrigin.name());
        out.putString("settingsOrigin", settingsOrigin.name()); out.putInt("scroll", dayScroll);
        out.putString("query", query); out.putInt("browseFirst", browseFirst); out.putInt("browseTop", browseTop);
    }
    @Override protected void onPause() {
        if (date != null) {
            remember();
            prefs.edit().putInt("civil_jdn", date.jdn).putInt("seen_today", observedToday)
                    .putInt("last_scroll", dayScroll).apply();
        }
        super.onPause();
    }
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.getBooleanExtra("open_today", false) && date != null) {
            detailOrigin = Screen.DAY;
            if (screen == Screen.DAY) selectDay(ChurchDate.today());
            else navigateToDay(ChurchDate.today());
        }
    }
    @Override protected void onStart() {
        super.onStart();
        if (date == null) return;
        IntentFilter f = new IntentFilter(Intent.ACTION_DATE_CHANGED);
        f.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        f.addAction(Intent.ACTION_TIME_CHANGED);
        registerReceiver(clock, f);
        checkToday();
    }
    private void checkToday() {
        int now = ChurchDate.today();
        if (observedToday == now || date == null) return;
        if (date.jdn == observedToday) {
            date = new ChurchDate(now);
            if (monthAnchor == observedToday) monthAnchor = now;
            dayScroll = 0;
        }
        observedToday = now;
        render();
    }
    @Override protected void onStop() {
        if (date != null) unregisterReceiver(clock);
        super.onStop();
    }
}
