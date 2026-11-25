package com.shlemko.upckpcalendar;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

final class ChurchDate {
    private static final String[] WEEK = {"Понеділок", "Вівторок", "Середа", "Четвер", "П’ятниця", "Субота", "Неділя"};
    static final String[] MONTH_NAMES = {"Січень", "Лютий", "Березень", "Квітень", "Травень", "Червень",
            "Липень", "Серпень", "Вересень", "Жовтень", "Листопад", "Грудень"};
    final int jdn;
    final int[] civil, old;
    ChurchDate(int jdn) {
        this.jdn = jdn;
        civil = CalendarMath.fromJulianDayGregorian(jdn);
        old = CalendarMath.gregorianToJulian(civil[0], civil[1], civil[2]);
    }
    static int fromMillis(long value) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(value);
        return CalendarMath.gregorianToJdn(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
    }
    static int today() { return fromMillis(System.currentTimeMillis()); }
    static String format(int[] date) { return date[2] + " " + CalendarData.MONTHS[date[1] - 1]; }
    static String full(int[] date) { return format(date) + " " + date[0]; }
    String weekday() { return WEEK[Math.floorMod(jdn, 7)]; }
    String favoriteKey() { return String.format(Locale.ROOT, "%02d-%02d", old[1], old[2]); }
    Calendar asCalendar() { return new GregorianCalendar(civil[0], civil[1] - 1, civil[2], 12, 0); }
    String movable() { return LiturgicalCalendar.movableFeast(civil[0], civil[1], civil[2]); }
    String fasting() { return LiturgicalCalendar.fastSummary(civil[0], civil[1], civil[2]); }
    CalendarData.Day record(CalendarData data) { return data.getOld(old[1], old[2]); }
    static int favoriteInYear(String key, int oldYear) {
        String[] parts = key.split("-");
        if (parts.length != 2) return 0;
        try {
            int m = Integer.parseInt(parts[0]), d = Integer.parseInt(parts[1]);
            if (m < 1 || m > 12 || d < 1 || d > 31) return 0;
            if (m == 2 && d == 29) oldYear += Math.floorMod(-oldYear, 4);
            int jdn = CalendarMath.julianToJdn(oldYear, m, d);
            int[] old = new ChurchDate(jdn).old;
            return old[1] == m && old[2] == d ? jdn : 0;
        } catch (NumberFormatException e) { return 0; }
    }
}
