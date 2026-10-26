package com.intentpause.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** 可启动应用列表与"需要停一下"的应用选择。选择状态沿用旧版的 SharedPreferences，避免升级丢数据。 */
public final class Apps_26_10_25 {
    private static final String PREFS = "selected_apps";
    private static final String DEFAULTS = "app_defaults";
    private static final String SENSITIVE = "sensitive_apps";
    private static final String ENCOURAGE = "encourage_apps";
    private static final String ENCOURAGE_SETTINGS = "encourage_settings";
    public static final int DEFAULT_MINUTES = 10;
    private static List<ResolveInfo> cache;

    private Apps_26_10_25() {
    }

    public static synchronized List<ResolveInfo> launcherApps(Context context) {
        if (cache != null) return cache;
        PackageManager pm = context.getPackageManager();
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_MAIN);
        intent.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        try {
            for (ResolveInfo info : pm.queryIntentActivities(intent, 0)) {
                if (info.activityInfo == null) continue;
                String pkg = info.activityInfo.packageName;
                if (pkg.equals(context.getPackageName())) continue;
                if (!seen.add(pkg)) continue;
                result.add(info);
            }
        } catch (RuntimeException ignored) {
        }
        Collator collator = Collator.getInstance(Locale.CHINA);
        Collections.sort(result, (a, b) -> collator.compare(
                a.loadLabel(pm).toString(), b.loadLabel(pm).toString()));
        cache = result;
        return cache;
    }

    public static synchronized void invalidate() {
        cache = null;
    }

    public static String label(Context context, String pkg) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (Exception ignored) {
            return pkg;
        }
    }

    public static Drawable icon(Context context, String pkg) {
        try {
            return context.getPackageManager().getApplicationIcon(pkg);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean selected(Context context, String pkg) {
        return prefs(context).getBoolean(pkg, false);
    }

    public static void setSelected(Context context, String pkg, boolean on) {
        prefs(context).edit().putBoolean(pkg, on).apply();
    }

    public static boolean sensitive(Context context, String pkg) {
        return context.getSharedPreferences(SENSITIVE, Context.MODE_PRIVATE)
                .getBoolean(pkg, false);
    }

    public static void setSensitive(Context context, String pkg, boolean on) {
        context.getSharedPreferences(SENSITIVE, Context.MODE_PRIVATE)
                .edit().putBoolean(pkg, on).apply();
    }

    public static java.util.List<String> sensitivePackages(Context context) {
        java.util.List<String> result = new java.util.ArrayList<>();
        android.content.SharedPreferences p =
                context.getSharedPreferences(SENSITIVE, Context.MODE_PRIVATE);
        for (java.util.Map.Entry<String, ?> e : p.getAll().entrySet()) {
            if (e.getValue() instanceof Boolean && (Boolean) e.getValue()) result.add(e.getKey());
        }
        return result;
    }

    public static boolean encouraged(Context context, String pkg) {
        return context.getSharedPreferences(ENCOURAGE, Context.MODE_PRIVATE)
                .getBoolean(pkg, false);
    }

    public static void setEncouraged(Context context, String pkg, boolean on) {
        context.getSharedPreferences(ENCOURAGE, Context.MODE_PRIVATE)
                .edit().putBoolean(pkg, on).apply();
    }

    public static List<String> encouragePackages(Context context) {
        android.content.SharedPreferences p = context.getSharedPreferences(ENCOURAGE, Context.MODE_PRIVATE);
        Set<String> on = new HashSet<>();
        for (java.util.Map.Entry<String, ?> e : p.getAll().entrySet()) {
            if (e.getValue() instanceof Boolean && (Boolean) e.getValue()) on.add(e.getKey());
        }
        List<String> result = new ArrayList<>();
        for (ResolveInfo info : launcherApps(context)) {
            String pkg = info.activityInfo.packageName;
            if (on.contains(pkg)) result.add(pkg);
        }
        return result;
    }

    public static int encourageInterval(Context context, String pkg) {
        return context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .getInt(pkg + "_interval", 20);
    }

    public static void setEncourageInterval(Context context, String pkg, int minutes) {
        context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .edit().putInt(pkg + "_interval", Math.max(10, Math.min(120, minutes))).apply();
    }

    public static boolean encourageOnOpen(Context context, String pkg) {
        return context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(pkg + "_on_open", true);
    }

    public static void setEncourageOnOpen(Context context, String pkg, boolean on) {
        context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .edit().putBoolean(pkg + "_on_open", on).apply();
    }

    public static boolean encourageDuring(Context context, String pkg) {
        return context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(pkg + "_during", true);
    }

    public static void setEncourageDuring(Context context, String pkg, boolean on) {
        context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .edit().putBoolean(pkg + "_during", on).apply();
    }

    public static boolean presetEnabled(Context context, String pkg) {
        return context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .getBoolean(pkg + "_preset", true);
    }

    public static void setPresetEnabled(Context context, String pkg, boolean on) {
        context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .edit().putBoolean(pkg + "_preset", on).apply();
    }

    public static String rotation(Context context, String pkg) {
        return context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .getString(pkg + "_rotation", "random");
    }

    public static void setRotation(Context context, String pkg, String rotation) {
        context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .edit().putString(pkg + "_rotation", rotation).apply();
    }

    public static List<String> customEncourageMessages(Context context, String pkg) {
        String raw = context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .getString(pkg + "_custom", "");
        List<String> result = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return result;
        try {
            org.json.JSONArray array = new org.json.JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                String line = array.optString(i, "").trim();
                if (!line.isEmpty()) result.add(line);
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    public static void setCustomEncourageMessages(Context context, String pkg, List<String> messages) {
        org.json.JSONArray array = new org.json.JSONArray();
        for (String line : messages) {
            if (line != null && !line.trim().isEmpty()) array.put(line.trim());
        }
        context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE)
                .edit().putString(pkg + "_custom", array.toString()).apply();
    }

    public static List<String> presetMessages() {
        List<String> list = new ArrayList<>();
        list.add("给自己想做的事，一点时间。");
        list.add("糯糯悄悄路过，送你一个赞。");
        list.add("按自己的节奏，糯糯不催。");
        list.add("你忙你的，糯糯给你打气。");
        list.add("想继续，就再留一会儿。");
        list.add("读几页也好，先从这一页开始。");
        return list;
    }

    public static String openingEncourageMessage(Context context, String pkg) {
        List<String> custom = customEncourageMessages(context, pkg);
        if (!custom.isEmpty()) return custom.get(0);
        String label = label(context, pkg);
        if (label.contains("读书") || label.contains("阅读")) return "想读的话，就从这一页开始。";
        return "给自己想做的事，一点时间。";
    }

    public static String nextEncourageMessage(Context context, String pkg) {
        List<String> messages = new ArrayList<>();
        if (presetEnabled(context, pkg)) messages.addAll(presetMessages());
        else messages.addAll(customEncourageMessages(context, pkg));
        if (messages.isEmpty()) messages.addAll(presetMessages());
        if ("sequential".equals(rotation(context, pkg))) {
            SharedPreferences p = context.getSharedPreferences(ENCOURAGE_SETTINGS, Context.MODE_PRIVATE);
            int index = p.getInt(pkg + "_seq", 0);
            String message = messages.get(Math.floorMod(index, messages.size()));
            p.edit().putInt(pkg + "_seq", index + 1).apply();
            return message;
        }
        return messages.get(new java.util.Random().nextInt(messages.size()));
    }

    public static int defaultMinutes(Context context, String pkg) {
        return context.getSharedPreferences(DEFAULTS, Context.MODE_PRIVATE)
                .getInt(pkg, DEFAULT_MINUTES);
    }

    public static void setDefaultMinutes(Context context, String pkg, int minutes) {
        context.getSharedPreferences(DEFAULTS, Context.MODE_PRIVATE)
                .edit().putInt(pkg, Math.max(1, Math.min(180, minutes))).apply();
    }

    /** 已选择且仍安装的应用，按名称排序。 */
    public static List<String> selectedPackages(Context context) {
        SharedPreferences p = prefs(context);
        Set<String> on = new LinkedHashSet<>();
        for (String key : p.getAll().keySet()) {
            Object value = p.getAll().get(key);
            if (value instanceof Boolean && (Boolean) value) on.add(key);
        }
        List<String> result = new ArrayList<>();
        for (ResolveInfo info : launcherApps(context)) {
            String pkg = info.activityInfo.packageName;
            if (on.contains(pkg)) result.add(pkg);
        }
        return result;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
