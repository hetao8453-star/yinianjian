package com.intentpause.app;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** 日期、时长与统计口径的纯计算，便于单元测试。 */
public final class StatsMath_26_10_25 {
    private StatsMath_26_10_25() {
    }

    public static long startOfDay(long wallMs) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(wallMs);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long addDays(long wallMs, int days) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(wallMs);
        c.add(Calendar.DAY_OF_YEAR, days);
        return c.getTimeInMillis();
    }

    public static String dateKey(long wallMs) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date(wallMs));
    }

    public static String monthDay(long wallMs) {
        return new SimpleDateFormat("M月d日", Locale.CHINA).format(new Date(wallMs));
    }

    public static String weekday(long wallMs) {
        String[] names = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(wallMs);
        return names[c.get(Calendar.DAY_OF_WEEK) - 1];
    }

    public static String clock(long wallMs) {
        return new SimpleDateFormat("HH:mm", Locale.CHINA).format(new Date(wallMs));
    }

    public static String duration(long ms) {
        if (ms <= 0) return "0 分钟";
        long minutes = Math.max(1, Math.round(ms / 60000.0));
        if (minutes < 60) return minutes + " 分钟";
        long hours = minutes / 60;
        long rest = minutes % 60;
        if (rest == 0) return hours + " 小时";
        return hours + " 小时 " + rest + " 分钟";
    }

    public static String minutes(int minutes) {
        return Math.max(0, minutes) + " 分钟";
    }

    /** 省时估算：复查页选择离开时，本可继续的整段时间。仅在界面明确标注“估算”。 */
    public static long savedEstimateMs(int plannedMinutes) {
        return Math.max(0, plannedMinutes) * 60000L;
    }

    public static int percent(long part, long total) {
        if (total <= 0) return 0;
        return (int) Math.round(part * 100.0 / total);
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
