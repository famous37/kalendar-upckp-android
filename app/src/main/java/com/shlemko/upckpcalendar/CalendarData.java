package com.shlemko.upckpcalendar;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public final class CalendarData {
    public static final String[] MONTHS = {"січня", "лютого", "березня", "квітня", "травня", "червня",
            "липня", "серпня", "вересня", "жовтня", "листопада", "грудня"};
    private static final Pattern MARKS = Pattern.compile("^(?:r\\+|[+_%&-])+\\s*");
    private static volatile CalendarData cached;
    private final Day[][] months = new Day[12][];

    public static CalendarData load(Context context) throws Exception {
        CalendarData result = cached;
        if (result == null) {
            synchronized (CalendarData.class) {
                if (cached == null) cached = new CalendarData(context);
                result = cached;
            }
        }
        return result;
    }

    private CalendarData(Context context) throws Exception {
        try (InputStream in = context.getAssets().open("calendar.json");
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            JSONArray records = new JSONObject(out.toString(StandardCharsets.UTF_8.name())).getJSONArray("months");
            for (int m = 0; m < months.length; m++) {
                JSONArray days = records.getJSONArray(m);
                months[m] = new Day[days.length()];
                for (int d = 0; d < days.length(); d++) {
                    JSONObject item = days.getJSONObject(d);
                    months[m][d] = new Day(clean(item.optString("feast")),
                            TextCorrections.saints(m + 1, d + 1, item.optString("saints")),
                            clean(item.optString("note")));
                }
            }
        }
    }

    public Day getOld(int month, int day) { return months[month - 1][day - 1]; }

    private static String clean(String text) {
        return MARKS.matcher(TextCorrections.display(text).trim()).replaceFirst("").trim();
    }

    public static final class Day {
        public final String feast, note;
        public final List<String> saints;
        Day(String feast, String memorials, String note) {
            this.feast = feast;
            this.note = note;
            ArrayList<String> list = new ArrayList<>();
            for (String raw : memorials.split(";")) {
                String saint = clean(raw);
                if (!saint.isEmpty() && !saint.equals(feast)) list.add(saint);
            }
            saints = Collections.unmodifiableList(list);
        }
        public String headline() {
            if (!feast.isEmpty()) return feast;
            return saints.isEmpty() ? note : saints.get(0);
        }
    }
}
