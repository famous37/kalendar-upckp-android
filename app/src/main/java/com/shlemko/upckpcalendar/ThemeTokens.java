package com.shlemko.upckpcalendar;

/** Single checked palette for Android 7+ (hex RGB, Android-independent for JVM tests). */
public final class ThemeTokens {
    private ThemeTokens(){}
    public static String background(boolean dark){return dark?"#1A1719":"#F8F5EF";}
    public static String surface(boolean dark){return dark?"#242023":"#FFFCF7";}
    public static String ink(boolean dark){return dark?"#F4EBDE":"#292321";}
    public static String subtle(boolean dark){return dark?"#C4B6AF":"#675B58";}
    public static String accent(boolean dark){return dark?"#EDB8B7":"#80394B";}
    public static String border(boolean dark){return dark?"#463B3D":"#DDD3C8";}
    public static String badge(boolean dark){return dark?"#392A30":"#F1E1E1";}
    /** WCAG 2.2 relative contrast. */
    public static double contrastRatio(String a,String b){
        double x=relativeLuminance(a),y=relativeLuminance(b);
        return (Math.max(x,y)+0.05)/(Math.min(x,y)+0.05);
    }
    private static double relativeLuminance(String hex){
        if(hex==null||!hex.matches("#[0-9a-fA-F]{6}"))
            throw new IllegalArgumentException("6-digit RGB color required");
        double[] c=new double[3];
        for(int i=0;i<3;i++){
            double x=Integer.parseInt(hex.substring(1+i*2,3+i*2),16)/255.0;
            c[i]=x<=0.04045?x/12.92:Math.pow((x+0.055)/1.055,2.4);
        }
        return c[0]*0.2126+c[1]*0.7152+c[2]*0.0722;
    }
}
