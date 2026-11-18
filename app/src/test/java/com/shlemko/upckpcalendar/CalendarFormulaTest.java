package com.shlemko.upckpcalendar;

/** No Android/JUnit deps. Run: java -ea ...CalendarFormulaTest */
public final class CalendarFormulaTest {
    private static void eq(int[] actual, int y, int m, int d) {
        if (actual[0]!=y || actual[1]!=m || actual[2]!=d) {
            throw new AssertionError("Expected "+y+"-"+m+"-"+d+", got "+java.util.Arrays.toString(actual));
        }
    }
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        eq(CalendarMath.gregorianToJulian(2026, 11, 8), 2026, 10, 26);
        eq(CalendarMath.julianToGregorian(2026, 12, 25), 2027, 1, 7);
        eq(CalendarMath.gregorianToJulian(2024, 3, 13), 2024, 2, 29);
        eq(CalendarMath.gregorianToJulian(2100, 3, 14), 2100, 2, 29); // century offset change (Julian leap)
        eq(CalendarMath.fromJulianDayGregorian(CalendarMath.orthodoxPaschaJdn(2024)), 2024, 5, 5);
        eq(CalendarMath.fromJulianDayGregorian(CalendarMath.orthodoxPaschaJdn(2025)), 2025, 4, 20);
        eq(CalendarMath.fromJulianDayGregorian(CalendarMath.orthodoxPaschaJdn(2026)), 2026, 4, 12);
        eq(CalendarMath.fromJulianDayGregorian(CalendarMath.orthodoxPaschaJdn(2027)), 2027, 5, 2);
        check(LiturgicalCalendar.movableFeast(2026,4,12).contains("ПАСХА"), "Pascha");
        check(LiturgicalCalendar.movableFeast(2026,4,5).contains("Вербна"), "Palm Sunday");
        check(LiturgicalCalendar.movableFeast(2026,5,21).contains("ВОЗНЕСІННЯ"), "Ascension");
        check(LiturgicalCalendar.movableFeast(2026,5,31).contains("П’ЯТИДЕСЯТНИЦЯ"), "Pentecost");
        check(LiturgicalCalendar.fastSummary(2026,3,1).contains("Великий"), "Lent");
        check(LiturgicalCalendar.fastSummary(2026,8,20).contains("Успенський"), "Dormition");
        check(LiturgicalCalendar.fastSummary(2026,12,20).contains("Різдвяний"), "Nativity fast");
        for(int year=1900; year<=2100; year+=5) {
            for(int mon=1; mon<=12; mon++) {
                for(int d=1; d<=28; d++) {
                    int jdn=CalendarMath.gregorianToJdn(year,mon,d);
                    int[] j=CalendarMath.gregorianToJulian(year,mon,d);
                    check(jdn==CalendarMath.julianToJdn(j[0],j[1],j[2]),"JDN round-trip "+year+"/"+mon+"/"+d);
                }
            }
        }
        System.out.println("All Julian/Pascha/fast informational tests passed.");
    }
}
