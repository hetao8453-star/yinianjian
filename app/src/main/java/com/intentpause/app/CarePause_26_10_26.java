package com.intentpause.app;

import android.content.Context;

/** “今天先暂停照看”：按本地日期生效，跨天自动恢复。 */
public final class CarePause_26_10_26 {
    private static final String PREFS = "care_pause";
    private static final String KEY_DATE = "date";

    private CarePause_26_10_26() {
    }

    public static boolean paused(Context context) {
        String date = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString(KEY_DATE, "");
        return StatsMath_26_10_25.dateKey(System.currentTimeMillis()).equals(date);
    }

    public static void pauseToday(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_DATE, StatsMath_26_10_25.dateKey(System.currentTimeMillis()))
                .apply();
    }

    public static void resume(Context context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .remove(KEY_DATE).apply();
    }
}
