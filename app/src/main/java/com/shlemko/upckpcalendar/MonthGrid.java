package com.shlemko.upckpcalendar;

import java.util.GregorianCalendar;

/** Gregorian month grid with Monday as the first weekday; 0 denotes empty cells. */
public final class MonthGrid {
    private MonthGrid(){}
    public static int daysInMonth(int year,int month){
        if(month<1||month>12)throw new IllegalArgumentException("Invalid month");
        GregorianCalendar date=new GregorianCalendar(year,month-1,1);
        return date.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
    }
    public static int firstMondayOffset(int year,int month){
        GregorianCalendar date=new GregorianCalendar(year,month-1,1);
        return (date.get(GregorianCalendar.DAY_OF_WEEK)+5)%7;
    }
    public static int[] days(int year,int month){
        int[] cells=new int[42];
        int start=firstMondayOffset(year,month),count=daysInMonth(year,month);
        for(int d=1;d<=count;d++)cells[start+d-1]=d;
        return cells;
    }
}
