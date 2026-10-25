package com.intentpause.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 本地会话与事件记录。只存本应用观察到的事实，不联网、不上传。 */
public final class IntentDb_26_10_25 extends SQLiteOpenHelper {
    private static final String DB_NAME = "intent.db";
    private static final int DB_VERSION = 1;
    private static volatile IntentDb_26_10_25 instance;

    public static IntentDb_26_10_25 get(Context context) {
        if (instance == null) {
            synchronized (IntentDb_26_10_25.class) {
                if (instance == null) {
                    instance = new IntentDb_26_10_25(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private IntentDb_26_10_25(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE sessions("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "pkg TEXT NOT NULL,"
                + "label TEXT,"
                + "purpose TEXT,"
                + "detail TEXT,"
                + "planned_min INTEGER DEFAULT 0,"
                + "emergency INTEGER DEFAULT 0,"
                + "started_wall INTEGER NOT NULL,"
                + "allowed_wall INTEGER DEFAULT 0,"
                + "due_wall INTEGER DEFAULT 0,"
                + "ended_wall INTEGER DEFAULT 0,"
                + "foreground_ms INTEGER DEFAULT 0,"
                + "review_count INTEGER DEFAULT 0,"
                + "continue_count INTEGER DEFAULT 0,"
                + "status TEXT DEFAULT 'pending',"
                + "saved_est_ms INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE events("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "session_id INTEGER,"
                + "pkg TEXT,"
                + "type TEXT,"
                + "wall INTEGER,"
                + "purpose TEXT,"
                + "detail TEXT,"
                + "meta TEXT)");
        db.execSQL("CREATE TABLE notes(date TEXT PRIMARY KEY, text TEXT, updated_wall INTEGER)");
        db.execSQL("CREATE TABLE service_log("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, wall INTEGER, event TEXT, detail TEXT)");
        db.execSQL("CREATE INDEX idx_sessions_started ON sessions(started_wall)");
        db.execSQL("CREATE INDEX idx_sessions_pkg ON sessions(pkg)");
        db.execSQL("CREATE INDEX idx_events_session ON events(session_id)");
        db.execSQL("CREATE INDEX idx_events_wall ON events(wall)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 首个版本；后续升级在这里做迁移，不清空用户记录。
    }

    // ---------------- 写入 ----------------

    public long startSession(String pkg, String label, long wall) {
        ContentValues v = new ContentValues();
        v.put("pkg", pkg);
        v.put("label", label);
        v.put("started_wall", wall);
        v.put("status", "pending");
        return getWritableDatabase().insert("sessions", null, v);
    }

    public void updatePurpose(long id, String purpose, String detail) {
        ContentValues v = new ContentValues();
        v.put("purpose", purpose);
        v.put("detail", detail);
        getWritableDatabase().update("sessions", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void markAllowed(long id, int plannedMin, boolean emergency, long dueWall, long wall) {
        ContentValues v = new ContentValues();
        v.put("planned_min", plannedMin);
        v.put("emergency", emergency ? 1 : 0);
        v.put("due_wall", dueWall);
        v.put("allowed_wall", wall);
        v.put("status", "allowed");
        getWritableDatabase().update("sessions", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void markDeclined(long id, long wall) {
        ContentValues v = new ContentValues();
        v.put("status", "declined");
        v.put("ended_wall", wall);
        getWritableDatabase().update("sessions", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void extendDue(long id, long dueWall) {
        ContentValues v = new ContentValues();
        v.put("due_wall", dueWall);
        getWritableDatabase().update("sessions", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void markEnded(long id, String status, long savedEstMs, long wall) {
        ContentValues v = new ContentValues();
        v.put("status", status);
        v.put("saved_est_ms", savedEstMs);
        v.put("ended_wall", wall);
        getWritableDatabase().update("sessions", v, "id=?", new String[]{String.valueOf(id)});
    }

    public void addForeground(long id, long ms) {
        if (id < 0 || ms <= 0) return;
        getWritableDatabase().execSQL(
                "UPDATE sessions SET foreground_ms=foreground_ms+? WHERE id=?", new Object[]{ms, id});
    }

    public void bumpReview(long id) {
        getWritableDatabase().execSQL(
                "UPDATE sessions SET review_count=review_count+1 WHERE id=?", new Object[]{id});
    }

    public void bumpContinue(long id) {
        getWritableDatabase().execSQL(
                "UPDATE sessions SET continue_count=continue_count+1 WHERE id=?", new Object[]{id});
    }

    public void event(long sessionId, String pkg, String type, String purpose, String detail, String meta, long wall) {
        ContentValues v = new ContentValues();
        v.put("session_id", sessionId);
        v.put("pkg", pkg);
        v.put("type", type);
        v.put("wall", wall);
        v.put("purpose", purpose);
        v.put("detail", detail);
        v.put("meta", meta);
        getWritableDatabase().insert("events", null, v);
    }

    public void serviceEvent(String event, String detail, long wall) {
        ContentValues v = new ContentValues();
        v.put("wall", wall);
        v.put("event", event);
        v.put("detail", detail);
        getWritableDatabase().insert("service_log", null, v);
    }

    // ---------------- 查询 ----------------

    public List<Session> recentSessions(int limit) {
        return querySessions("SELECT * FROM sessions ORDER BY started_wall DESC LIMIT " + limit);
    }

    public List<Session> sessionsForApp(String pkg, int limit) {
        return querySessions("SELECT * FROM sessions WHERE pkg=? ORDER BY started_wall DESC LIMIT " + limit,
                pkg);
    }

    public List<Session> sessionsForDay(long begin, long end) {
        return querySessions("SELECT * FROM sessions WHERE started_wall>=? AND started_wall<? ORDER BY started_wall DESC",
                begin, end);
    }

    private List<Session> querySessions(String sql, Object... args) {
        List<Session> result = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(sql, toStrings(args))) {
            while (c.moveToNext()) result.add(readSession(c));
        }
        return result;
    }

    private static Session readSession(Cursor c) {
        Session s = new Session();
        s.id = c.getLong(c.getColumnIndexOrThrow("id"));
        s.pkg = c.getString(c.getColumnIndexOrThrow("pkg"));
        s.label = c.getString(c.getColumnIndexOrThrow("label"));
        s.purpose = c.getString(c.getColumnIndexOrThrow("purpose"));
        s.detail = c.getString(c.getColumnIndexOrThrow("detail"));
        s.plannedMin = c.getInt(c.getColumnIndexOrThrow("planned_min"));
        s.emergency = c.getInt(c.getColumnIndexOrThrow("emergency")) == 1;
        s.startedWall = c.getLong(c.getColumnIndexOrThrow("started_wall"));
        s.allowedWall = c.getLong(c.getColumnIndexOrThrow("allowed_wall"));
        s.endedWall = c.getLong(c.getColumnIndexOrThrow("ended_wall"));
        s.foregroundMs = c.getLong(c.getColumnIndexOrThrow("foreground_ms"));
        s.reviewCount = c.getInt(c.getColumnIndexOrThrow("review_count"));
        s.continueCount = c.getInt(c.getColumnIndexOrThrow("continue_count"));
        s.status = c.getString(c.getColumnIndexOrThrow("status"));
        s.savedEstMs = c.getLong(c.getColumnIndexOrThrow("saved_est_ms"));
        return s;
    }

    public DayStats dayStats(long begin, long end) {
        DayStats d = new DayStats();
        d.opens = count("SELECT COUNT(*) FROM sessions WHERE allowed_wall>0 AND started_wall>=? AND started_wall<?", begin, end);
        d.declined = count("SELECT COUNT(*) FROM sessions WHERE status='declined' AND started_wall>=? AND started_wall<?", begin, end);
        d.reminders = count("SELECT COUNT(*) FROM events WHERE type='review_shown' AND wall>=? AND wall<?", begin, end);
        d.leaves = count("SELECT COUNT(*) FROM events WHERE type='review_leave' AND wall>=? AND wall<?", begin, end);
        d.savedEstMs = sum("SELECT IFNULL(SUM(saved_est_ms),0) FROM sessions WHERE saved_est_ms>0 AND ended_wall>=? AND ended_wall<?", begin, end);
        d.foregroundMs = sum("SELECT IFNULL(SUM(foreground_ms),0) FROM sessions WHERE started_wall>=? AND started_wall<?", begin, end);
        return d;
    }

    public List<DayApp> dayApps(long begin, long end) {
        Map<String, DayApp> map = new HashMap<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT pkg, IFNULL(label,'') AS label, COUNT(*) AS opens, IFNULL(SUM(foreground_ms),0) AS ms "
                        + "FROM sessions WHERE allowed_wall>0 AND started_wall>=? AND started_wall<? "
                        + "GROUP BY pkg ORDER BY opens DESC", toStrings(begin, end))) {
            while (c.moveToNext()) {
                DayApp a = new DayApp();
                a.pkg = c.getString(0);
                a.label = c.getString(1);
                a.opens = c.getInt(2);
                a.foregroundMs = c.getLong(3);
                map.put(a.pkg, a);
            }
        }
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT pkg, COUNT(*) FROM events WHERE type='review_shown' AND wall>=? AND wall<? GROUP BY pkg",
                toStrings(begin, end))) {
            while (c.moveToNext()) {
                DayApp a = map.get(c.getString(0));
                if (a != null) a.reminders = c.getInt(1);
            }
        }
        List<DayApp> result = new ArrayList<>(map.values());
        result.sort((a, b) -> Integer.compare(b.opens, a.opens));
        return result;
    }

    public List<AppSummary> appSummaries() {
        Map<String, AppSummary> map = new HashMap<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT pkg, IFNULL(label,''), COUNT(*) AS opens, IFNULL(SUM(foreground_ms),0), "
                        + "MAX(started_wall), IFNULL(SUM(saved_est_ms),0) "
                        + "FROM sessions WHERE allowed_wall>0 GROUP BY pkg ORDER BY MAX(started_wall) DESC", null)) {
            while (c.moveToNext()) {
                AppSummary a = new AppSummary();
                a.pkg = c.getString(0);
                a.label = c.getString(1);
                a.opens = c.getInt(2);
                a.foregroundMs = c.getLong(3);
                a.lastWall = c.getLong(4);
                a.savedEstMs = c.getLong(5);
                map.put(a.pkg, a);
            }
        }
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT pkg, COUNT(*) FROM events WHERE type='review_shown' GROUP BY pkg", null)) {
            while (c.moveToNext()) {
                AppSummary a = map.get(c.getString(0));
                if (a != null) a.reminders = c.getInt(1);
            }
        }
        return new ArrayList<>(map.values());
    }

    public List<DayCount> opensPerDay(long begin, long end) {
        Map<String, Integer> map = new HashMap<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT started_wall FROM sessions WHERE allowed_wall>0 AND started_wall>=? AND started_wall<?",
                toStrings(begin, end))) {
            while (c.moveToNext()) {
                String key = StatsMath_26_10_25.dateKey(c.getLong(0));
                Integer n = map.get(key);
                map.put(key, n == null ? 1 : n + 1);
            }
        }
        List<DayCount> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            DayCount d = new DayCount();
            d.dateKey = e.getKey();
            d.count = e.getValue();
            result.add(d);
        }
        return result;
    }

    /** 返回周一到周日 7 个桶。 */
    public int[] weekdayOpens(long begin, long end) {
        int[] buckets = new int[7];
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT started_wall FROM sessions WHERE allowed_wall>0 AND started_wall>=? AND started_wall<?",
                toStrings(begin, end))) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            while (c.moveToNext()) {
                cal.setTimeInMillis(c.getLong(0));
                int dow = cal.get(java.util.Calendar.DAY_OF_WEEK); // Sunday=1
                int index = (dow + 5) % 7; // Monday=0
                buckets[index]++;
            }
        }
        return buckets;
    }

    public List<AppTime> appTimes(long begin, long end) {
        List<AppTime> result = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT pkg, IFNULL(label,''), IFNULL(SUM(foreground_ms),0) FROM sessions "
                        + "WHERE allowed_wall>0 AND started_wall>=? AND started_wall<? "
                        + "GROUP BY pkg HAVING SUM(foreground_ms)>0 ORDER BY SUM(foreground_ms) DESC",
                toStrings(begin, end))) {
            while (c.moveToNext()) {
                AppTime a = new AppTime();
                a.pkg = c.getString(0);
                a.label = c.getString(1);
                a.ms = c.getLong(2);
                result.add(a);
            }
        }
        return result;
    }

    public String note(String date) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT text FROM notes WHERE date=?", new String[]{date})) {
            if (c.moveToFirst()) return c.getString(0);
        }
        return "";
    }

    public void setNote(String date, String text) {
        ContentValues v = new ContentValues();
        v.put("date", date);
        v.put("text", text);
        v.put("updated_wall", System.currentTimeMillis());
        getWritableDatabase().insertWithOnConflict("notes", null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<ServiceEntry> serviceLog(int limit) {
        List<ServiceEntry> result = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT wall, event, detail FROM service_log ORDER BY wall DESC LIMIT " + limit, null)) {
            while (c.moveToNext()) {
                ServiceEntry s = new ServiceEntry();
                s.wall = c.getLong(0);
                s.event = c.getString(1);
                s.detail = c.getString(2);
                result.add(s);
            }
        }
        return result;
    }

    public long lastServiceWall(String event) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT wall FROM service_log WHERE event=? ORDER BY wall DESC LIMIT 1",
                new String[]{event})) {
            return c.moveToFirst() ? c.getLong(0) : 0L;
        }
    }

    public int totalSessionCount() {
        return count("SELECT COUNT(*) FROM sessions", null);
    }

    public void clearAllRecords() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("sessions", null, null);
        db.delete("events", null, null);
        db.delete("notes", null, null);
        db.delete("service_log", null, null);
    }

    private int count(String sql, Object... args) {
        try (Cursor c = getReadableDatabase().rawQuery(sql, toStrings(args))) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    private long sum(String sql, Object... args) {
        try (Cursor c = getReadableDatabase().rawQuery(sql, toStrings(args))) {
            return c.moveToFirst() ? c.getLong(0) : 0L;
        }
    }

    private static String[] toStrings(Object... args) {
        if (args == null) return null;
        String[] out = new String[args.length];
        for (int i = 0; i < args.length; i++) out[i] = String.valueOf(args[i]);
        return out;
    }

    // ---------------- POJO ----------------

    public static class Session {
        public long id;
        public String pkg;
        public String label;
        public String purpose;
        public String detail;
        public int plannedMin;
        public boolean emergency;
        public long startedWall;
        public long allowedWall;
        public long endedWall;
        public long foregroundMs;
        public int reviewCount;
        public int continueCount;
        public String status;
        public long savedEstMs;
    }

    public static class DayStats {
        public int opens;
        public int declined;
        public int reminders;
        public int leaves;
        public long savedEstMs;
        public long foregroundMs;
    }

    public static class DayApp {
        public String pkg;
        public String label;
        public int opens;
        public int reminders;
        public long foregroundMs;
    }

    public static class AppSummary {
        public String pkg;
        public String label;
        public int opens;
        public int reminders;
        public long foregroundMs;
        public long savedEstMs;
        public long lastWall;
    }

    public static class DayCount {
        public String dateKey;
        public int count;
    }

    public static class AppTime {
        public String pkg;
        public String label;
        public long ms;
    }

    public static class ServiceEntry {
        public long wall;
        public String event;
        public String detail;
    }
}
