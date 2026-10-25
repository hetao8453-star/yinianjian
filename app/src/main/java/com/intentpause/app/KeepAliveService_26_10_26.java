package com.intentpause.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * 轻量前台服务，降低被系统清理的概率。
 * 通知权限被拒绝时，通知可能不显示，但服务本身仍可运行；到时提醒由无障碍覆盖层完成。
 */
public class KeepAliveService_26_10_26 extends Service {
    private static final String CHANNEL = "keepalive";
    private static final int NOTIFICATION_ID = 9001;
    public static volatile boolean running;

    public static void ensure(Context context) {
        try {
            ContextCompat.startForegroundService(context,
                    new Intent(context, KeepAliveService_26_10_26.class));
        } catch (RuntimeException ignored) {
            // 系统限制后台启动前台服务时忽略；下次打开 App 会再试。
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        createChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        startForeground(NOTIFICATION_ID, buildNotification());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager == null) return;
        NotificationChannel channel = new NotificationChannel(CHANNEL, "后台保护",
                NotificationManager.IMPORTANCE_MIN);
        channel.setDescription("保持提醒服务运行；可在系统设置中关闭。");
        channel.setShowBadge(false);
        manager.createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("一念间正在运行")
                .setContentText("打开受控应用时会先停一下")
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setOngoing(true)
                .setSilent(true)
                .setContentIntent(pending)
                .build();
    }
}
