package com.shlemko.upckpcalendar;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

final class CalendarUi {
    static final int MATCH = -1, WRAP = -2;
    static final int WINE = 0xff552534, GOLD = 0xffedc996, PAPER = 0xfffff5e5;
    final Activity activity;
    final boolean dark;
    final int bg, surface, ink, muted, accent, border, blush;
    static final Typeface SERIF = Typeface.create("serif", Typeface.NORMAL);
    static final Typeface MEDIUM = Typeface.create("sans-serif-medium", Typeface.NORMAL);

    CalendarUi(Activity activity) {
        this.activity = activity;
        dark = UiTheme.isDark(activity);
        bg = Color.parseColor(ThemeTokens.background(dark));
        surface = Color.parseColor(ThemeTokens.surface(dark));
        ink = Color.parseColor(ThemeTokens.ink(dark));
        muted = Color.parseColor(ThemeTokens.subtle(dark));
        accent = Color.parseColor(ThemeTokens.accent(dark));
        border = Color.parseColor(ThemeTokens.border(dark));
        blush = Color.parseColor(ThemeTokens.badge(dark));
    }
    int dp(float n) { return Math.round(activity.getResources().getDisplayMetrics().density * n); }
    private int length(int n) { return n < 0 ? n : dp(n); }
    LinearLayout.LayoutParams box(int width, int height) { return new LinearLayout.LayoutParams(length(width), length(height)); }
    LinearLayout.LayoutParams flex() { return new LinearLayout.LayoutParams(0, WRAP, 1); }
    void pad(View v, int l, int t, int r, int b) { v.setPadding(dp(l), dp(t), dp(r), dp(b)); }
    void above(LinearLayout into, View child, int top) {
        LinearLayout.LayoutParams params = box(MATCH, WRAP);
        params.topMargin = dp(top);
        into.addView(child, params);
    }
    LinearLayout column() {
        LinearLayout v = new LinearLayout(activity);
        v.setOrientation(LinearLayout.VERTICAL);
        return v;
    }
    LinearLayout row() {
        LinearLayout v = new LinearLayout(activity);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }
    LinearLayout page() {
        LinearLayout v = column();
        int width = activity.getResources().getConfiguration().screenWidthDp;
        int gutter = Math.max(width < 360 ? 14 : 20, (width - 640) / 2);
        pad(v, gutter, 16, gutter, 28);
        return v;
    }
    ScrollView scroll(LinearLayout body) {
        ScrollView scroll = new ScrollView(activity);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setFillViewport(true);
        scroll.addView(body);
        return scroll;
    }
    TextView text(CharSequence value, float size, int color) {
        TextView v = new TextView(activity);
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setIncludeFontPadding(false);
        v.setLineSpacing(dp(2), 1f);
        return v;
    }
    TextView serif(String value, float size, int color) {
        TextView v = text(value, size, color);
        v.setTypeface(SERIF);
        return v;
    }
    TextView small(String value, int color) {
        TextView v = text(value, 12, color);
        v.setTypeface(MEDIUM);
        v.setLetterSpacing(0.075f);
        return v;
    }
    GradientDrawable shape(int fill, int radius, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(radius));
        if (stroke != Color.TRANSPARENT) d.setStroke(dp(1), stroke);
        return d;
    }
    Drawable pressed(int fill, int radius, int stroke) {
        return new RippleDrawable(ColorStateList.valueOf(dark ? 0x33ffffff : 0x2274354a),
                shape(fill, radius, stroke), shape(Color.WHITE, radius, 0));
    }
    ImageView image(int resource, int color) {
        ImageView v = new ImageView(activity);
        v.setImageResource(resource);
        if (color != 0) v.setImageTintList(ColorStateList.valueOf(color));
        v.setScaleType(ImageView.ScaleType.FIT_CENTER);
        v.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        return v;
    }
    ImageButton icon(int resource, int color, String description, Runnable click) {
        ImageButton b = new ImageButton(activity);
        b.setImageResource(resource);
        b.setImageTintList(ColorStateList.valueOf(color));
        b.setScaleType(ImageView.ScaleType.CENTER);
        b.setContentDescription(description);
        b.setBackground(pressed(0, 24, 0));
        b.setOnClickListener(v -> click.run());
        return b;
    }
    TextView button(String label, Runnable click) {
        TextView b = text(label, 15, accent);
        b.setTypeface(MEDIUM);
        b.setGravity(Gravity.CENTER);
        b.setMinHeight(dp(48));
        pad(b, 10, 4, 10, 4);
        b.setBackground(pressed(blush, 14, 0));
        b.setFocusable(true);
        b.setOnClickListener(v -> click.run());
        return b;
    }
    void rule(LinearLayout parent) {
        View line = new View(activity);
        line.setBackgroundColor(border);
        parent.addView(line, new LinearLayout.LayoutParams(MATCH, Math.max(1, dp(.75f))));
    }
    void heading(LinearLayout parent, String title) {
        TextView h = serif(title, 26, ink);
        if (Build.VERSION.SDK_INT >= 28) h.setAccessibilityHeading(true);
        above(parent, h, 12);
    }
    LinearLayout link(int symbol, String title, String detail, Runnable action) {
        LinearLayout row = row();
        pad(row, 0, 11, 0, 11);
        row.setMinimumHeight(dp(58));
        if (symbol != 0) {
            row.addView(image(symbol, accent), box(24,24));
        }
        LinearLayout words = column();
        pad(words, symbol == 0 ? 0 : 14, 0, 8, 0);
        TextView label = text(title, 16, ink);
        label.setTypeface(MEDIUM);
        words.addView(label);
        if (!detail.isEmpty()) above(words, text(detail, 14, muted), 3);
        row.addView(words, flex());
        row.addView(image(R.drawable.ic_right, muted), box(18,18));
        row.setBackground(pressed(0, 8, 0));
        row.setFocusable(true);
        row.setOnClickListener(v -> action.run());
        return row;
    }
}
