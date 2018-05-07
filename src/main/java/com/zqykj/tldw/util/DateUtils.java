package com.zqykj.tldw.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by weifeng on 2018/5/7.
 */
public class DateUtils {

    private static String pattern = "yyyy-MM-dd HH:mm:00";

    public static String getWholeMinute(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static Date getDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }

}
