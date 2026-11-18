package com.shlemko.upckpcalendar;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

/** Year-scoped 2018 bibliographic source; never reinterpret as current-year lectionary. */
public final class SourceYear2018 {
    private final JSONArray days;
    public SourceYear2018(Context context) throws Exception {
        try(InputStream in=context.getAssets().open("source_calendar_2018.json");
            ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[8192];int n;
            while((n=in.read(buf))!=-1)out.write(buf,0,n);
            JSONObject root=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
            days=root.getJSONArray("days");
            if(days.length()!=365)throw new IllegalStateException("2018 source incomplete");
        }
    }
    public JSONObject get(Calendar date){
        if(date.get(Calendar.YEAR)!=2018)return null;
        int i=date.get(Calendar.DAY_OF_YEAR)-1;
        return i>=0&&i<days.length()?days.optJSONObject(i):null;
    }
}
