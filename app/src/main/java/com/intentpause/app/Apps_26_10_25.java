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
