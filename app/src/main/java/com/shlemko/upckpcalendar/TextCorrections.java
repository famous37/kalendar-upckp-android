package com.shlemko.upckpcalendar;

/** Only manually verified source-text corrections, leaving the 366-day original JSON intact. */
public final class TextCorrections {
    private TextCorrections(){}
    public static String saints(int julianMonth,int julianDay,String original){
        if(original==null)return "";
        if(julianMonth==8&&julianDay==2){
            return original.replace("архидияк;Стефана","архідиякона Стефана");
        }
        return original;
    }
}
