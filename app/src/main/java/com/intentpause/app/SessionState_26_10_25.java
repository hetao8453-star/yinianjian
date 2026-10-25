package com.intentpause.app;

import android.content.Context;
import android.content.SharedPreferences;

/** 活动会话的磁盘快照，用于服务进程被回收后恢复。 */
public final class SessionState_26_10_25 {
    private static final String PREFS = "active_session";

    private SessionState_26_10_25() {
    }

    public static class Snapshot {
        public long sessionId = -1;
        public String pkg;
        public String label;
        public String purpose;
        public String detail;
        public int durationMinutes = 10;
        public boolean emergency;
        public boolean waitingForChoice;
        public boolean review;
        public long dueWall;
        public long leftWall;
        public long allowedWall;
        public long startedWall;
        public long foregroundMs;
        public String foreground;
        public int page = 1;

        public boolean active() {
            return sessionId > 0 && pkg != null;
        }
    }

    public static Snapshot load(Context context) {
        SharedPreferences p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        Snapshot s = new Snapshot();
        s.sessionId = p.getLong("sessionId", -1);
        s.pkg = p.getString("pkg", null);
        s.label = p.getString("label", null);
        s.purpose = p.getString("purpose", null);
        s.detail = p.getString("detail", null);
        s.durationMinutes = p.getInt("durationMinutes", 10);
        s.emergency = p.getBoolean("emergency", false);
        s.waitingForChoice = p.getBoolean("waitingForChoice", false);
        s.review = p.getBoolean("review", false);
        s.dueWall = p.getLong("dueWall", 0);
        s.leftWall = p.getLong("leftWall", 0);
        s.allowedWall = p.getLong("allowedWall", 0);
        s.startedWall = p.getLong("startedWall", 0);
        s.foregroundMs = p.getLong("foregroundMs", 0);
        s.foreground = p.getString("foreground", null);
        s.page = p.getInt("page", 1);
        return s;
    }

    public static void save(Context context, Snapshot s) {
        SharedPreferences.Editor e = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        e.putLong("sessionId", s.sessionId);
        e.putString("pkg", s.pkg);
        e.putString("label", s.label);
        e.putString("purpose", s.purpose);
        e.putString("detail", s.detail);
        e.putInt("durationMinutes", s.durationMinutes);
        e.putBoolean("emergency", s.emergency);
        e.putBoolean("waitingForChoice", s.waitingForChoice);
        e.putBoolean("review", s.review);
        e.putLong("dueWall", s.dueWall);
        e.putLong("leftWall", s.leftWall);
        e.putLong("allowedWall", s.allowedWall);
        e.putLong("startedWall", s.startedWall);
        e.putLong("foregroundMs", s.foregroundMs);
        e.putString("foreground", s.foreground);
        e.putInt("page", s.page);
        e.apply();
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
