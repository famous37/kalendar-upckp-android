package com.shlemko.upckpcalendar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class CalendarIndex {
    final int year;
    private final ArrayList<Entry> entries = new ArrayList<>();

    CalendarIndex(CalendarData data, int year) {
        this.year = year;
        int end = CalendarMath.gregorianToJdn(year + 1, 1, 1);
        boolean hasLeapMemorial = false;
        for (int jdn = CalendarMath.gregorianToJdn(year, 1, 1); jdn < end; jdn++) {
            ChurchDate date = new ChurchDate(jdn);
            if (date.old[1] == 2 && date.old[2] == 29) hasLeapMemorial = true;
            CalendarData.Day record = date.record(data);
            ArrayList<String> lines = new ArrayList<>();
            String movable = date.movable();
            if (!movable.isEmpty()) lines.add(movable);
            if (!record.feast.isEmpty()) lines.add(record.feast);
            if (!record.note.isEmpty()) lines.add(record.note);
            lines.addAll(record.saints);
            boolean feast = !record.feast.isEmpty() || (!movable.isEmpty() && !movable.equals("Світлий тиждень"));
            entries.add(new Entry(date, lines, feast));
        }
        if (!hasLeapMemorial) {
            ChurchDate leap = new ChurchDate(ChurchDate.favoriteInYear("02-29", year));
            entries.add(new Entry(leap, leap.record(data).saints, false));
        }
    }
    static String fold(String text) {
        return text.trim().toLowerCase(Locale.ROOT).replace('i','і')
                .replace('’','\'').replace('ʼ','\'').replace("'", "").replace('‑','-');
    }
    List<Result> find(String query) {
        String q = fold(query);
        String[] words = q.split("\\s+");
        ArrayList<Result> hits = new ArrayList<>();
        for (Entry e : entries) {
            if (q.isEmpty()) {
                if (e.feast) hits.add(new Result(e.date, e.lines.get(0)));
                continue;
            }
            for (int i = 0; i < e.normalized.size(); i++) {
                boolean matched = true;
                for (String word : words) {
                    if (!e.normalized.get(i).contains(word)) { matched = false; break; }
                }
                if (matched) { hits.add(new Result(e.date,e.lines.get(i))); break; }
            }
        }
        return hits;
    }
    static final class Result {
        final ChurchDate date;
        final String label;
        Result(ChurchDate date, String label) { this.date = date; this.label = label; }
    }
    private static final class Entry {
        final ChurchDate date;
        final List<String> lines;
        final ArrayList<String> normalized = new ArrayList<>();
        final boolean feast;
        Entry(ChurchDate date, List<String> lines, boolean feast) {
            this.date = date; this.lines = lines; this.feast = feast;
            for (String line : lines) normalized.add(fold(line));
        }
    }
}
