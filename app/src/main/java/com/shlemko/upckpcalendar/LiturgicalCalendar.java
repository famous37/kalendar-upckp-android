package com.shlemko.upckpcalendar;

/**
 * Computed movable commemorations and overview of fasting periods in the
 * traditional Julian Orthodox calendar. Not a replacement for the Typikon or
 * the yearly approved liturgical calendar of a particular jurisdiction.
 * No Android dependencies, so formulas are independently testable on the JVM.
 */
public final class LiturgicalCalendar {
    static final String NO_FAST_MARK = "Пісний період не позначено";
    private LiturgicalCalendar() { }

    /** A name for major movable days. Empty string for ordinary days. */
    public static String movableFeast(int year, int month, int day) {
        int jdn = CalendarMath.gregorianToJdn(year, month, day);
        int p = paschaForJdn(jdn);
        int delta = jdn - p;
        switch (delta) {
            case -70: return "Неділя про митаря і фарисея";
            case -63: return "Неділя про блудного сина";
            case -56: return "Неділя м’ясопусна";
            case -49: return "Неділя сиропусна · Прощена неділя";
            case -48: return "Початок Великого посту";
            case -8: return "Лазарева субота";
            case -7: return "Вхід Господній у Єрусалим · Вербна неділя";
            case -3: return "Великий четвер";
            case -2: return "Велика п’ятниця";
            case -1: return "Велика субота";
            case 0: return "СВІТЛЕ ХРИСТОВЕ ВОСКРЕСІННЯ · ПАСХА";
            case 7: return "Антипасха · Фомина неділя";
            case 24: return "Переполовення П’ятидесятниці";
            case 39: return "ВОЗНЕСІННЯ ГОСПОДНЄ";
            case 49: return "ДЕНЬ СВЯТОЇ ТРОЙЦІ · П’ЯТИДЕСЯТНИЦЯ";
            case 50: return "День Святого Духа";
            case 56: return "Неділя всіх святих";
            default: return (delta >= 1 && delta <= 6) ? "Світлий тиждень" : "";
        }
    }

    private static int paschaForJdn(int jdn) {
        int[] civil = CalendarMath.fromJulianDayGregorian(jdn);
        int year = civil[0];
        int current = CalendarMath.orthodoxPaschaJdn(year);
        // Computed day may be in January/February: pick nearest applicable
        // Easter-year context for movable observances near year boundaries.
        if (jdn < current - 70) return CalendarMath.orthodoxPaschaJdn(year - 1);
        if (jdn > current + 300) return CalendarMath.orthodoxPaschaJdn(year + 1);
        return current;
    }

    /** An informational summary, not precise food permissions. */
    public static String fastSummary(int year, int month, int day) {
        int jdn = CalendarMath.gregorianToJdn(year, month, day);
        int[] old = CalendarMath.gregorianToJulian(year, month, day);
        int m = old[1], d = old[2];
        int p = CalendarMath.orthodoxPaschaJdn(year);
        if (jdn >= p - 6 && jdn < p) return "Страсний тиждень";
        if (jdn >= p - 48 && jdn < p - 6) return "Великий піст";
        if (m == 8 && d <= 14) return "Успенський піст";
        if ((m == 11 && d >= 15) || (m == 12 && d <= 24)) return "Різдвяний піст";
        int apostlesStart = p + 57;
        int apostlesEnd = CalendarMath.julianToJdn(year, 6, 28);
        if (apostlesStart <= apostlesEnd && jdn >= apostlesStart && jdn <= apostlesEnd) return "Петрів піст";
        if ((m == 1 && d == 5) || (m == 8 && d == 29) || (m == 9 && d == 14))
            return "Одноденний піст";
        // Fast-free periods; the above fast checks take priority.
        if ((m == 12 && d >= 25) || (m == 1 && d <= 4)) return "Святки · суцільний період";
        if ((jdn >= p - 69 && jdn <= p - 63) ||
            (jdn >= p && jdn <= p + 6) ||
            (jdn >= p + 50 && jdn <= p + 56)) return "Суцільний тиждень";
        if (jdn >= p - 55 && jdn <= p - 49) return "Сирний тиждень · без м’яса";
        // ISO independent weekday: JDN % 7 = 0 Monday, 2 Wednesday, 4 Friday.
        int weekday = Math.floorMod(jdn, 7);
        if (weekday == 2) return "Середа · піст за загальним правилом";
        if (weekday == 4) return "П’ятниця · піст за загальним правилом";
        return NO_FAST_MARK;
    }
}
