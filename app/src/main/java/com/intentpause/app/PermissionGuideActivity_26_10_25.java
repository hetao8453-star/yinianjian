package com.intentpause.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/** 权限说明：逐项解释，并说明“为什么可能每次都要重新开”。 */
public class PermissionGuideActivity_26_10_25 extends AppCompatActivity {
    private static final int REQUEST_NOTIFICATIONS = 43;

    private LinearLayout list;
    private TextView accessibilityState;
    private TextView notificationState;
    private TextView batteryState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui_26_10_25.applyWindow(this);

        LinearLayout root = Ui_26_10_25.vertical(this);
        root.setBackgroundColor(Ui_26_10_25.PAPER);

        LinearLayout header = Ui_26_10_25.horizontal(this);
        header.setPadding(Ui_26_10_25.dp(this, 12), Ui_26_10_25.dp(this, 8),
                Ui_26_10_25.dp(this, 12), Ui_26_10_25.dp(this, 6));
        header.addView(Ui_26_10_25.iconButton(this, R.drawable.ic_back, 44, this::finish));
        header.addView(Ui_26_10_25.text(this, "权限说明", 19, Ui_26_10_25.INK, true));
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        list = Ui_26_10_25.vertical(this);
        list.setPadding(Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 6),
                Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 32));
        scroll.addView(list);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
        Ui_26_10_25.applyInsets(root);

        buildSections();
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void buildSections() {
        LinearLayout accessibility = Ui_26_10_25.card(this);
        accessibility.addView(sectionTitle("无障碍服务（唯一必需）"));
        accessibilityState = Ui_26_10_25.hint(this, "");
        accessibility.addView(accessibilityState);
        accessibility.addView(body("一念间只通过它知道：你选的那几个应用什么时候来到前台。"
                + "只取应用包名和窗口切换，不读屏幕文字、不读聊天内容、不联网。"));
        accessibility.addView(buttonRow("去系统设置开启", () -> Perms_26_10_25.openAccessibilitySettings(this)));
        accessibility.addView(body("系统不允许任何 App 自己打开无障碍，也不能在你关闭后再替你打开——"
                + "这是 Android 的安全设计。安装新包后，系统也可能要求你重新确认一次。"));
        Ui_26_10_25.add(list, accessibility, 10);

        LinearLayout notification = Ui_26_10_25.card(this);
        notification.addView(sectionTitle("通知（可选）"));
        notificationState = Ui_26_10_25.hint(this, "");
        notification.addView(notificationState);
        notification.addView(body("只用于两种低频场景：确认页没能显示时的兜底提醒；"
                + "开机后发现提醒服务没开启时提示一次。拒绝也不影响确认页和记录。"));
        notification.addView(buttonRow("去开启通知", this::askNotifications));
        Ui_26_10_25.add(list, notification, 10);

        LinearLayout battery = Ui_26_10_25.card(this);
        battery.addView(sectionTitle("电池优化（建议）"));
        batteryState = Ui_26_10_25.hint(this, "");
        battery.addView(batteryState);
        battery.addView(body("部分 Android 系统的省电策略会清理后台，导致提醒突然不工作。"
                + "允许忽略电池优化能减少这种情况，但系统仍可能按自己的策略清理。"));
        battery.addView(buttonRow("去设置后台限制", () -> {
            if (!Perms_26_10_25.requestIgnoreBatteryOptimizations(this)) {
                Ui_26_10_25.toast(this, "没能打开电池设置，请到系统设置里手动找一念间");
            }
        }));
        Ui_26_10_25.add(list, battery, 10);

        LinearLayout autostart = Ui_26_10_25.card(this);
        autostart.addView(sectionTitle("自启动 / 后台高耗电（系统设置，不是权限）"));
        autostart.addView(body("Android 没有可以直接申请的“后台运行”权限。部分手机建议手动完成三件事：\n"
                + "1. 系统设置 → 应用管理 → 一念间 → 允许自启动/后台活动；\n"
                + "2. 电池设置 → 一念间 → 允许后台高耗电；\n"
                + "3. 最近任务里锁定一念间的卡片，避免被一键清理。\n"
                + "不同品牌的菜单名称和位置不同。开机后无障碍是否自动恢复，取决于系统是否保留授权。"));
        autostart.addView(buttonRow("打开自启动设置", () ->
                Perms_26_10_25.openAutostartSettings(this)));
        Ui_26_10_25.add(list, autostart, 10);

        LinearLayout safety = Ui_26_10_25.card(this);
        safety.addView(sectionTitle("安全边界"));
        safety.addView(body("一念间没有联网权限，不读取屏幕内容、聊天记录、通知或剪贴板；"
                + "只记录你选择的应用的打开、目的与时长。锁屏/息屏时不显示确认页。"
                + "它用到的无障碍能力只取前台应用包名与窗口切换，不用来读取别的应用内容。"));
        Ui_26_10_25.add(list, safety, 10);

        LinearLayout killed = Ui_26_10_25.card(this);
        killed.addView(sectionTitle("彻底关掉后台后为什么要重新开"));
        killed.addView(body("从最近任务里强制停止应用会同时关掉无障碍服务；部分系统或省电策略也会把已开启的无障碍判为无效。"
                + "这是 Android 的系统行为，App 不能自己重新打开无障碍。"
                + "下次启动一念间时，如果检测到无障碍未开启，会直接跳到系统设置，方便你重开一次。"));
        Ui_26_10_25.add(list, killed, 10);

        LinearLayout why = Ui_26_10_25.card(this);
        why.addView(sectionTitle("为什么可能“每次都要重新开”"));
        why.addView(body("常见原因有三种，运行记录里能看到最近一次连接/断开时间：\n"
                + "1. 安装新包或系统更新后，侧载应用的无障碍授权会被系统要求重新确认；\n"
                + "2. 厂商省电或清理后台把服务停掉；\n"
                + "3. 每次打开受控应用都会重新出现确认页——这是一念间的设计，不是权限丢失。"));
        Ui_26_10_25.add(list, why, 10);

        LinearLayout notUsed = Ui_26_10_25.card(this);
        notUsed.addView(sectionTitle("只申请这些权限"));
        notUsed.addView(body("必需：无障碍服务。只用来识别前台应用包名与窗口切换。\n"
                + "可选：通知（兜底与开机提示）、前台服务、开机检查、电池优化白名单。\n\n"
                + "不申请：联网、使用情况访问、悬浮窗、存储、读取屏幕内容、读取通知或剪贴板。"));
        Ui_26_10_25.add(list, notUsed, 10);
    }

    private TextView sectionTitle(String text) {
        return Ui_26_10_25.text(this, text, 16, Ui_26_10_25.INK, true);
    }

    private TextView body(String text) {
        TextView v = Ui_26_10_25.text(this, text, 13, Ui_26_10_25.MUTED, false);
        v.setPadding(0, Ui_26_10_25.dp(this, 8), 0, 0);
        v.setLineSpacing(Ui_26_10_25.dp(this, 4), 1f);
        return v;
    }

    private View buttonRow(String text, Runnable run) {
        Button button = Ui_26_10_25.button(this, text, false);
        button.setOnClickListener(v -> run.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(this, 48));
        lp.topMargin = Ui_26_10_25.dp(this, 12);
        button.setLayoutParams(lp);
        return button;
    }

    private void refresh() {
        if (accessibilityState == null) return;
        boolean acc = Perms_26_10_25.accessibilityEnabled(this);
        accessibilityState.setText(acc ? "当前：已开启" : "当前：未开启");
        accessibilityState.setTextColor(acc ? Ui_26_10_25.GREEN : Ui_26_10_25.WARN);
        boolean notify = Perms_26_10_25.notificationsGranted(this);
        notificationState.setText(notify ? "当前：已允许" : "当前：未允许（不影响主流程）");
        notificationState.setTextColor(notify ? Ui_26_10_25.GREEN : Ui_26_10_25.MUTED);
        boolean battery = Perms_26_10_25.ignoringBatteryOptimizations(this);
        batteryState.setText(battery ? "当前：已忽略电池优化" : "当前：仍受系统电池策略管理");
        batteryState.setTextColor(battery ? Ui_26_10_25.GREEN : Ui_26_10_25.WARN);
    }

    private void askNotifications() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
        } else {
            Perms_26_10_25.openNotificationSettings(this);
        }
    }
}
