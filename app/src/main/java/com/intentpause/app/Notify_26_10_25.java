package com.intentpause.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/** 低频通知：只用于兜底提醒与开机后的服务状态提示，可被用户拒绝。 */
public final class Notify_26_10_25 {
    private static final String CHANNEL = "reminders";

    private Notify_26_10_25() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL, "提醒与运行状态",
                NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("确认页无法显示时的兜底提醒，以及开机后提醒服务状态。");
        channel.setShowBadge(false);
        manager.createNotificationChannel(channel);
    }

    public static boolean canNotify(Context context) {
        return Perms_26_10_25.notificationsGranted(context);
    }

    public static void bootCheck(Context context) {
        ensureChannel(context);
        if (!canNotify(context)) return;
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("慢一拍：提醒服务没有开启")
                .setContentText("点此到无障碍设置重新开启。开机或安装新包后，系统可能需要你重新确认。")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(pending);
        try {
            NotificationManagerCompat.from(context).notify(1001, builder.build());
        } catch (SecurityException ignored) {
        }
    }

    public static void overlayFallback(Context context, String appLabel) {
        ensureChannel(context);
        if (!canNotify(context)) return;
        Intent intent = new Intent(context, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("提醒页暂时没有显示出来")
                .setContentText("你刚打开了 " + (appLabel == null ? "一个应用" : appLabel)
                        + "。点此查看慢一拍的状态，或稍后重新打开。")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setContentIntent(pending);
        try {
            NotificationManagerCompat.from(context).notify(1002, builder.build());
        } catch (SecurityException ignored) {
        }
    }
}
