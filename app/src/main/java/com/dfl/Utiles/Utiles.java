package com.dfl.Utiles;

import android.app.ActivityManager;
import android.content.Context;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Utiles {

    public static String objToJson(Object obj){
        Gson gson=new Gson();
        return gson.toJson(obj);
    }

    public static long getTimestempByDay() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = new Date();
        String dateStr = sdf.format(d);
        try {
            Date date = sdf.parse(dateStr);
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0L;
    }

    public static String getFormatVal(double val)
    {
        return getFormatVal(val,"0.00");
    }

    public static String getFormatVal(double val,String formatStr)
    {
        DecimalFormat df=new DecimalFormat(formatStr);
        return df.format(val);
    }

    /**
     * 判断服务器是否在运行
     * @param ctx
     * @param serviceName
     * @return
     */
    public static boolean isServiceRunning(Context ctx, String serviceName)
    {
        boolean isRunning = false;
        if(ctx==null||serviceName==null){
            return isRunning;
        }
        ActivityManager activityManager = (ActivityManager)ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List servicesList = activityManager.getRunningServices(Integer.MAX_VALUE);
        Iterator iterator = servicesList.iterator();
        while(iterator.hasNext()) {
            ActivityManager.RunningServiceInfo runningServiceInfo = (ActivityManager.RunningServiceInfo)iterator.next();
            if(serviceName.trim().equals(runningServiceInfo.service.getClassName())) {
                isRunning = true;
                return isRunning;
            }
        }
        return isRunning;
    }


}
