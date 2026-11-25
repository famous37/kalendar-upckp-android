package com.shlemko.upckpcalendar;

/** Icons in the calendar collection are displayed only on their own feast. */
final class FeastIcon {
    private FeastIcon() {}
    static int forDate(ChurchDate date) {
        int offset = date.jdn - CalendarMath.orthodoxPaschaJdn(date.civil[0]);
        if (offset == 0) return R.drawable.voskresenie;
        if (offset == 49) return R.drawable.pedesetnica;
        switch (date.old[1] * 100 + date.old[2]) {
            case 101: return R.drawable.sv_vasilij;
            case 106: return R.drawable.bogojavlenie;
            case 202: return R.drawable.sretenie;
            case 325: return R.drawable.blagovestenie;
            case 423: return R.drawable.sv_georgij;
            case 624: return R.drawable.predteca;
            case 629: return R.drawable.apostoli;
            case 630: return R.drawable.apostoli12;
            case 806: return R.drawable.preobrazenie;
            case 815: return R.drawable.uspenie;
            case 914: return R.drawable.vozdvizenie;
            case 1121: return R.drawable.vovedenie;
            case 1206: return R.drawable.sv_nikola;
            case 1225: return R.drawable.rozdestvo;
            default: return 0;
        }
    }
}
