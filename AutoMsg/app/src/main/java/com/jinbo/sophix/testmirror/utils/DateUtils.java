package com.jinbo.sophix.testmirror.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by houjinbo on 2017/10/12.
 * DateUtils
 */

public class DateUtils {


    /**
     * 获取当前时间
     *
     * @return
     */
    public static String getTimeNow() {
        Date date = new Date();
        return format(date);
    }

    /**
     * 获取指定时间的日期描述
     *
     * @param millons
     * @return
     */
    public static String getTime(long millons) {
        Date date = new Date(millons);
        return format(date);

    }

    /**
     * 格式化描述日期
     *
     * @param date
     * @return
     */
    private static String format(Date date) {
        String sendDate = "";
        if (date != null) {
            SimpleDateFormat format = new SimpleDateFormat(" yyyy-MM-dd HH:mm:ss ");
            sendDate = format.format(date);
        }
        return sendDate;

    }
}
