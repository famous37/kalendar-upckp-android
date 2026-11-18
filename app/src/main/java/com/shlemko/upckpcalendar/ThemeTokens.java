package com.shlemko.upckpcalendar;

/** Single checked palette for Android 7+ (hex RGB, Android-independent for JVM tests). */
public final class ThemeTokens {
    private ThemeTokens(){}
    public static String background(boolean dark){return dark?"#101722":"#F7F5EF";}
    public static String surface(boolean dark){return dark?"#1D2936":"#FFFEFB";}
    public static String ink(boolean dark){return dark?"#F5F5F2":"#202D3C";}
    public static String subtle(boolean dark){return dark?"#B7C1CA":"#526273";}
    public static String accent(boolean dark){return dark?"#E7C27C":"#805517";}
    public static String border(boolean dark){return dark?"#354454":"#DCD9D1";}
    public static String badge(boolean dark){return dark?"#342A20":"#F4E9D4";}
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
