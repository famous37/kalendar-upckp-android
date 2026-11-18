package com.shlemko.upckpcalendar;

/** Verified corrections and presentation normalization; source JSON stays intact. */
public final class TextCorrections {
    private TextCorrections(){}
    public static String display(String text){
        return text==null?"":text.replace('i','і');
    }
    public static String saints(int julianMonth,int julianDay,String original){
        if(original==null)return "";
        String result=original;
        if(julianMonth==8&&julianDay==2){
            result=result.replace("архидияк;Стефана","архідиякона Стефана");
        }
        return display(result);
    }
}
