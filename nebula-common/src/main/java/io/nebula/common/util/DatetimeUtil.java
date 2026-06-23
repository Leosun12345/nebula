package io.nebula.common.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DatetimeUtil {
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static String format(Date date, String pattern) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    public static Date parse(String dateStr, String pattern) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    public static Date now() {
        return new Date();
    }

    public static long currentTimestamp() {
        return System.currentTimeMillis();
    }
}
