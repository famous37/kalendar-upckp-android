package com.shlemko.upckpcalendar;

/** Independent Julian/Gregorian conversion; no fixed 13-day assumption. */
public final class CalendarMath {
    private CalendarMath() {}
    public static int[] gregorianToJulian(int year, int month, int day) {
        return fromJulianDayJulian(gregorianToJdn(year,month,day));
    }
    public static int[] julianToGregorian(int year, int month, int day) {
        return fromJulianDayGregorian(julianToJdn(year,month,day));
    }
    public static int gregorianToJdn(int y,int m,int d) {
        int a=(14-m)/12, yy=y+4800-a, mm=m+12*a-3;
        return d+(153*mm+2)/5+365*yy+yy/4-yy/100+yy/400-32045;
    }
    public static int julianToJdn(int y,int m,int d) {
        int a=(14-m)/12, yy=y+4800-a, mm=m+12*a-3;
        return d+(153*mm+2)/5+365*yy+yy/4-32083;
    }
    private static int[] fromJulianDayJulian(int jdn) {
        int c=jdn+32082,d=(4*c+3)/1461,e=c-(1461*d)/4,m=(5*e+2)/153;
        return new int[]{d-4800+m/10,m+3-12*(m/10),e-(153*m+2)/5+1};
    }
    public static int[] fromJulianDayGregorian(int jdn) {
        int a=jdn+32044,b=(4*a+3)/146097,c=a-(146097*b)/4;
        int d=(4*c+3)/1461,e=c-(1461*d)/4,m=(5*e+2)/153;
        return new int[]{100*b+d-4800+m/10,m+3-12*(m/10),e-(153*m+2)/5+1};
    }
    public static int orthodoxPaschaJdn(int year) {
        int a=year%4,b=year%7,c=year%19;
        int d=(19*c+15)%30,e=(2*a+4*b-d+34)%7;
        int m=(d+e+114)/31, day=(d+e+114)%31+1;
        return julianToJdn(year,m,day);
    }
}
