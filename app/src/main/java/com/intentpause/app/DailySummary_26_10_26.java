package com.intentpause.app;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

/** 今日总结：用户选时间，近似调度；通知关闭时下次打开补看。 */
public final class DailySummary_26_10_26 {
    private static final String PREFS = "daily_summary";
    private static final String KEY_MINUTES = "minutes";
    private static final String KEY_PENDING = "pending_date";
    private static final String KEY_ASKED = "asked_time";
    public static final String CHANNEL = "summary";
    public static final String EXTRA_DATE = "summary_date";

    private DailySummary_26_10_26() {
    }

    public static int minutes(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_MINUTES, -1);
    }

    public static void setMinutes(Context context, int minutes) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putInt(KEY_MINUTES, Math.max(0, Math.min(1439, minutes))).apply();
        schedule(context);
    }

    public static String timeText(Context context) {
        int minutes = minutes(context);
        if (minutes < 0) return "还没设置时间";
        return String.format(Locale.CHINA, "%02d:%02d", minutes / 60, minutes % 60);
    }

    public static boolean askedTime(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_ASKED, false);
    }

    public static void markAskedTime(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putBoolean(KEY_ASKED, true).apply();
    }

    public static String todayText(Context context) {
        long now = System.currentTimeMillis();
        long begin = StatsMath_26_10_25.startOfDay(now);
        long end = begin + 86400000L;
        IntentDb_26_10_25 db = IntentDb_26_10_25.get(context);
        IntentDb_26_10_25.DayStats controlled = db.dayStats(begin, end);
        IntentDb_26_10_25.EncourageStats encourage = db.encourageStats(begin, end);
        int stops = db.controlledActiveStops(begin, end);
        int declined = db.declinedCount(begin, end);
        StringBuilder sb = new StringBuilder();
        sb.append("受控应用：打开 ").append(controlled.opens).append(" 次 · 提醒 ")
                .append(controlled.reminders).append(" 次 · ")
                .append(StatsMath_26_10_25.duration(controlled.foregroundMs)).append("\n");
        sb.append("鼓励应用：打开 ").append(encourage.opens).append(" 次 · 鼓励提示 ")
                .append(encourage.prompts).append(" 次 · ")
                .append(StatsMath_26_10_25.duration(encourage.ms)).append("\n");
        sb.append("今天先不打开 ").append(declined).append(" 次。\n");
        sb.append(gentleLine(controlled.reminders, encourage.prompts, declined));
        String compare = compare(context, begin, now, stops);
        if (compare != null) sb.append("\n").append(compare);
        return sb.toString();
    }

    /** 通知和手动入口共用同一日期口径；当天尚未到发送时刻时只统计到现在。 */
    public static Snapshot snapshot(Context context, String dateKey) {
        Snapshot saved = savedSnapshot(context, dateKey);
        if (saved != null) return saved;
        Calendar day = Calendar.getInstance(Locale.CHINA);
        try {
            day.setTime(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).parse(dateKey));
        } catch (ParseException | NullPointerException error) {
            day.setTimeInMillis(System.currentTimeMillis());
        }
        day.set(Calendar.HOUR_OF_DAY, 0);
        day.set(Calendar.MINUTE, 0);
        day.set(Calendar.SECOND, 0);
        day.set(Calendar.MILLISECOND, 0);
        long begin = day.getTimeInMillis();
        int chosen = minutes(context);
        long cutoff = begin + (chosen < 0 ? 1440 : chosen) * 60000L;
        long end = Math.min(cutoff, System.currentTimeMillis());
        if (end < begin) end = begin;
        IntentDb_26_10_25 db = IntentDb_26_10_25.get(context);
        Snapshot result = new Snapshot();
        result.dateKey = StatsMath_26_10_25.dateKey(begin);
        result.controlled = db.dayStats(begin, end);
        result.encouraged = db.encourageStats(begin, end);
        result.stops = db.controlledActiveStops(begin, end);
        result.declined = db.declinedCount(begin, end);
        result.gentle = gentleLine(result.controlled.reminders,
                result.encouraged.prompts, result.declined);
        result.compare = compare(context, begin, end, result.stops);
        return result;
    }

    public static final class Snapshot {
        public String dateKey;
        public IntentDb_26_10_25.DayStats controlled;
        public IntentDb_26_10_25.EncourageStats encouraged;
        public int stops;
        public int declined;
        public String gentle;
        public String compare;
    }

    private static String snapshotKey(String dateKey) {
        return "snapshot_" + dateKey;
    }

    private static Snapshot savedSnapshot(Context context, String dateKey) {
        if (dateKey == null) return null;
        String json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(snapshotKey(dateKey), null);
        if (json == null) return null;
        try {
            JSONObject object = new JSONObject(json);
            Snapshot s = new Snapshot();
            s.dateKey = dateKey;
            s.controlled = new IntentDb_26_10_25.DayStats();
            s.controlled.opens = object.getInt("controlled_opens");
            s.controlled.reminders = object.getInt("reminders");
            s.controlled.foregroundMs = object.getLong("controlled_ms");
            s.encouraged = new IntentDb_26_10_25.EncourageStats();
            s.encouraged.opens = object.getInt("encourage_opens");
            s.encouraged.prompts = object.getInt("prompts");
            s.encouraged.ms = object.getLong("encourage_ms");
            s.stops = object.getInt("stops");
            s.declined = object.getInt("declined");
            s.gentle = object.getString("gentle");
            s.compare = object.optString("compare", null);
            return s;
        } catch (JSONException error) {
            return null;
        }
    }

    private static void saveSnapshot(Context context, Snapshot s) {
        JSONObject object = new JSONObject();
        try {
            object.put("controlled_opens", s.controlled.opens);
            object.put("reminders", s.controlled.reminders);
            object.put("controlled_ms", s.controlled.foregroundMs);
            object.put("encourage_opens", s.encouraged.opens);
            object.put("prompts", s.encouraged.prompts);
            object.put("encourage_ms", s.encouraged.ms);
            object.put("stops", s.stops);
            object.put("declined", s.declined);
            object.put("gentle", s.gentle);
            object.put("compare", s.compare);
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString(snapshotKey(s.dateKey), object.toString()).apply();
        } catch (JSONException ignored) {
        }
    }

    private static String gentleLine(int reminders, int prompts, int declined) {
        if (declined > 0) return "这一次停一下，就已经不一样了。";
        if (prompts > 0 || reminders > 0) return "有意识地用，慢慢来就好。";
        return "今天也按自己的节奏来。";
    }

    private static String compare(Context context, long dayBegin, long cutoffWall, int todayStops) {
        long elapsed = cutoffWall - dayBegin;
        if (elapsed < 60 * 60 * 1000L) return null; // 太早不做比较
        IntentDb_26_10_25 db = IntentDb_26_10_25.get(context);
        long sum = 0;
        int valid = 0;
        for (int i = 1; i <= 7; i++) {
            long begin = StatsMath_26_10_25.addDays(dayBegin, -i);
            long end = begin + elapsed;
            IntentDb_26_10_25.DayStats d = db.dayStats(begin, end);
            IntentDb_26_10_25.EncourageStats e = db.encourageStats(begin, end);
            int stops = db.controlledActiveStops(begin, end);
            if (stops == 0 && d.opens == 0 && e.opens == 0 && d.foregroundMs == 0) continue;
            sum += stops;
            valid++;
        }
        if (valid < 3) return null;
        double avg = sum / (double) valid;
        if (todayStops > avg + 0.5) {
            return "到今天这会儿，比前 7 天同一时刻平均多停了 "
                    + Math.round(todayStops - avg) + " 次。";
        }
        if (todayStops < avg - 0.5) {
            return "到今天这会儿，比前 7 天同一时刻平均少停了 "
                    + Math.round(avg - todayStops) + " 次。";
        }
        return "今天的节奏，和前 7 天差不多。";
    }

    public static void schedule(Context context) {
        int minutes = minutes(context);
        if (minutes < 0) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Calendar next = Calendar.getInstance(Locale.CHINA);
        next.set(Calendar.HOUR_OF_DAY, minutes / 60);
        next.set(Calendar.MINUTE, minutes % 60);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            next.add(Calendar.DAY_OF_MONTH, 1);
        }
        PendingIntent pi = PendingIntent.getBroadcast(context, 7301,
                new Intent(context, DailySummaryReceiver_26_10_26.class),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        try {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
        } catch (RuntimeException ignored) {
        }
    }

    public static void deliver(Context context) {
        String dateKey = StatsMath_26_10_25.dateKey(System.currentTimeMillis());
        saveSnapshot(context, snapshot(context, dateKey));
        if (!Perms_26_10_25.notificationsGranted(context)) {
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString(KEY_PENDING, dateKey)
                    .apply();
            return;
        }
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL, "今日总结",
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("每天一次，把今天的记录轻轻放在这里。");
            nm.createNotificationChannel(channel);
        }
        Intent open = new Intent(context, SummaryActivity_26_10_26.class)
                .putExtra(EXTRA_DATE, dateKey)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(context, 7302, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("今天的记录整理好了")
                .setContentText("想看看吗？")
                .setContentIntent(pi)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_LOW);
        try {
            NotificationManagerCompat.from(context).notify(7301, b.build());
        } catch (SecurityException ignored) {
        }
    }

    public static boolean hasPending(Context context) {
        String pending = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_PENDING, "");
        return pending != null && !pending.isEmpty();
    }

    public static String pendingDate(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_PENDING, StatsMath_26_10_25.dateKey(System.currentTimeMillis()));
    }

    public static void clearPending(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .remove(KEY_PENDING).apply();
    }
}
