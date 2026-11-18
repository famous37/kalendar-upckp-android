package com.shlemko.upckpcalendar;

import org.junit.Test;
import java.util.Calendar;
import java.util.GregorianCalendar;
import static org.junit.Assert.*;

/** In-process regression suite, independent of Android device. */
public class CalendarRegressionTest {
    @Test public void verifiedMemorialCorrectionAndNoOtherChanges(){
        assertEquals("Перенесення мощей архідиякона Стефана",
            TextCorrections.saints(8,2,"Перенесення мощей архидияк;Стефана"));
        assertEquals("Пам'ять святого",TextCorrections.saints(8,3,"Пам'ять святого"));
    }
    @Test public void displayNormalizesMixedLatinIInMemorials(){
        assertEquals("Свт. Лікійський і Прп.",TextCorrections.display("Свт. Лiкiйський i Прп."));
    }
    @Test public void normalTextAndBadgesMeetWcagContrast(){
        for(boolean dark:new boolean[]{false,true}){
            assertTrue("Body text",ThemeTokens.contrastRatio(
                ThemeTokens.ink(dark),ThemeTokens.surface(dark))>=7.0);
            assertTrue("Secondary text",ThemeTokens.contrastRatio(
                ThemeTokens.subtle(dark),ThemeTokens.surface(dark))>=4.5);
            assertTrue("Fasting badge",ThemeTokens.contrastRatio(
                ThemeTokens.accent(dark),ThemeTokens.badge(dark))>=4.5);
        }
    }
    @Test public void leapAndCenturyBoundary() {
        assertArrayEquals(new int[]{2024,2,29},CalendarMath.gregorianToJulian(2024,3,13));
        assertArrayEquals(new int[]{2100,2,29},CalendarMath.gregorianToJulian(2100,3,14));
        assertArrayEquals(new int[]{2027,1,7},CalendarMath.julianToGregorian(2026,12,25));
    }
    @Test public void roundTripsAllMonthsAcrossFourCenturies(){
        for(int year=1900;year<=2200;year+=5){
            for(int month=1;month<=12;month++){
                int last=MonthGrid.daysInMonth(year,month);
                for(int day=1;day<=last;day++){
                    int[] old=CalendarMath.gregorianToJulian(year,month,day);
                    assertArrayEquals(new int[]{year,month,day},
                        CalendarMath.julianToGregorian(old[0],old[1],old[2]));
                }
            }
        }
    }
    @Test public void fixedOrthodoxPaschas(){
        assertArrayEquals(new int[]{2024,5,5},CalendarMath.fromJulianDayGregorian(CalendarMath.orthodoxPaschaJdn(2024)));
        assertArrayEquals(new int[]{2025,4,20},CalendarMath.fromJulianDayGregorian(CalendarMath.orthodoxPaschaJdn(2025)));
        assertArrayEquals(new int[]{2026,4,12},CalendarMath.fromJulianDayGregorian(CalendarMath.orthodoxPaschaJdn(2026)));
        assertArrayEquals(new int[]{2027,5,2},CalendarMath.fromJulianDayGregorian(CalendarMath.orthodoxPaschaJdn(2027)));
    }
    @Test public void monthGridStartsMondayAndHasEveryDayExactlyOnce(){
        for(int year=2023;year<=2102;year++){
            for(int month=1;month<=12;month++){
                int[] cells=MonthGrid.days(year,month);
                assertEquals(42,cells.length);
                int count=0;
                for(int day:cells){if(day>0){count++;assertTrue(day<=MonthGrid.daysInMonth(year,month));}}
                assertEquals(MonthGrid.daysInMonth(year,month),count);
                Calendar cal=new GregorianCalendar(year,month-1,1);
                assertEquals((cal.get(Calendar.DAY_OF_WEEK)+5)%7,MonthGrid.firstMondayOffset(year,month));
            }
        }
    }
    @Test public void movableFeastsAndFastingOverview(){
        assertTrue(LiturgicalCalendar.movableFeast(2026,4,12).contains("ПАСХА"));
        assertTrue(LiturgicalCalendar.movableFeast(2026,4,5).contains("Вербна"));
        assertTrue(LiturgicalCalendar.movableFeast(2026,5,21).contains("ВОЗНЕСІННЯ"));
        assertTrue(LiturgicalCalendar.movableFeast(2026,5,31).contains("П’ЯТИДЕСЯТНИЦЯ"));
        assertTrue(LiturgicalCalendar.fastSummary(2026,3,1).contains("Великий"));
        assertTrue(LiturgicalCalendar.fastSummary(2026,8,20).contains("Успенський"));
        assertTrue(LiturgicalCalendar.fastSummary(2026,12,20).contains("Різдвяний"));
    }
}
