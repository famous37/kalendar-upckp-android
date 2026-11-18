package com.shlemko.upckpcalendar;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

/** Full original 366-day Julian menologion exported from the owner's APK. */
public final class CalendarData {
    private final JSONArray months;
    public static final String[] MONTHS={"січня","лютого","березня","квітня","травня","червня","липня","серпня","вересня","жовтня","листопада","грудня"};
    public CalendarData(Context context) throws Exception {
        try(InputStream in=context.getAssets().open("calendar.json");ByteArrayOutputStream out=new ByteArrayOutputStream()) {
            byte[] buf=new byte[8192];int len;
            while((len=in.read(buf))!=-1)out.write(buf,0,len);
            months=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8)).getJSONArray("months");
        }
    }
    public Day getOld(int month,int day) {
        try {
            JSONObject d=months.getJSONArray(month-1).getJSONObject(day-1);
            return new Day(d.optString("feast",""),TextCorrections.saints(month,day,d.optString("saints","")),d.optString("note",""));
        } catch(Exception e) { return new Day("","Дані для цієї дати недоступні",""); }
    }
    public static String tidy(String raw) {
        String[] parts=raw.split(";");StringBuilder sb=new StringBuilder();
        for(String x:parts) {
            String p=x.trim().replaceFirst("^(r\\+|[+_%&-])+\\s*","").trim();
            if(p.isEmpty())continue;
            if(sb.length()>0)sb.append("\n\n");
            sb.append("• ").append(p);
        }
        return sb.toString();
    }
    public ArrayList<SearchHit> search(String query) {
        String q=query.toLowerCase(new Locale("uk")).trim();ArrayList<SearchHit> hits=new ArrayList<>();
        if(q.isEmpty())return hits;
        for(int m=1;m<=12;m++) for(int d=1;d<=months.optJSONArray(m-1).length();d++){
            Day item=getOld(m,d);
            String all=(item.feast+" "+item.saints+" "+item.note).toLowerCase(new Locale("uk"));
            if(all.contains(q))hits.add(new SearchHit(m,d,item));
        }
        return hits;
    }
    public static final class Day {
        public final String feast,saints,note;
        Day(String f,String s,String n){feast=f;saints=s;note=n;}
        public String headline(){String a=feast.isEmpty()?saints:feast;return a.replaceFirst("^(r\\+|[+_%&-])+\\s*","").split(";")[0];}
    }
    public static final class SearchHit {
        public final int month,day;public final Day value;
        SearchHit(int m,int d,Day v){month=m;day=d;value=v;}
    }
}
