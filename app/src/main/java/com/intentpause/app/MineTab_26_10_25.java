package com.intentpause.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import java.util.List;

/** 我的：运行状态、全部记录、权限说明与本地数据管理。 */
public class MineTab_26_10_25 extends ScrollView implements Refreshable_26_10_25 {
    private static final int REQUEST_NOTIFICATIONS = 42;

    private final MainActivity activity;
    private final StatusRefs accessibility;
    private final StatusRefs notification;
    private final StatusRefs battery;
    private final StatusRefs autostart;
    private final StatusRefs protection;
    private final TextView accessibilityAction;
    private final TextView notificationAction;
    private final TextView batteryAction;
    private final TextView lastLine;
    private final LinearLayout logList;
    private boolean logsExpanded;

    public MineTab_26_10_25(MainActivity activity) {
        super(activity);
        this.activity = activity;
        setFillViewport(true);
        setBackgroundColor(Ui_26_10_25.PAPER);
        setClipToPadding(false);

        LinearLayout page = Ui_26_10_25.vertical(activity);
        page.setPadding(Ui_26_10_25.dp(activity, 22), Ui_26_10_25.dp(activity, 22),
                Ui_26_10_25.dp(activity, 22), Ui_26_10_25.dp(activity, 32));
        addView(page, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        page.addView(Ui_26_10_25.text(activity, "我的", 28, Ui_26_10_25.INK, true));
        page.addView(Ui_26_10_25.hint(activity, "只在本机工作：不联网，不读屏。"));

        LinearLayout statusCard = Ui_26_10_25.card(activity);
        statusCard.addView(Ui_26_10_25.text(activity, "运行状态", 15, Ui_26_10_25.INK, true));

        LinearLayout row1 = statusRow(statusCard);
        accessibility = statusRefs(row1, "无障碍服务");
        accessibilityAction = rowAction(row1, "去开启", () ->
                Perms_26_10_25.openAccessibilitySettings(activity));

        LinearLayout row2 = statusRow(statusCard);
        notification = statusRefs(row2, "通知");
        notificationAction = rowAction(row2, "去开启", this::askNotifications);

        LinearLayout row3 = statusRow(statusCard);
        battery = statusRefs(row3, "电池优化");
        batteryAction = rowAction(row3, "去设置", () ->
                Perms_26_10_25.requestIgnoreBatteryOptimizations(activity));

        LinearLayout row4 = statusRow(statusCard);
        autostart = statusRefs(row4, "自启动 / 后台高耗电");
        rowAction(row4, "去设置", () -> Perms_26_10_25.openAutostartSettings(activity));

        LinearLayout row5 = statusRow(statusCard);
        protection = statusRefs(row5, "后台保护");
        rowAction(row5, "启动", () -> {
            KeepAliveService_26_10_26.ensure(activity);
            refresh();
        });

        lastLine = Ui_26_10_25.hint(activity, "");
        lastLine.setPadding(0, Ui_26_10_25.dp(activity, 12), 0, 0);
        statusCard.addView(lastLine);
        Ui_26_10_25.add(page, statusCard, 20);

        Ui_26_10_25.add(page, linkRow("全部记录", "每个应用每一次的选择",
                new Intent(activity, AppRecordActivity_26_10_25.class)), 16);
        Ui_26_10_25.add(page, linkRow("敏感应用", "打开时不弹确认页，也不记录",
                new Intent(activity, AppPickerActivity_26_10_25.class).putExtra("mode", "sensitive")), 10);
        Ui_26_10_25.add(page, linkRow("权限说明", "每一项权限与“为什么可能每次都要重开”",
                new Intent(activity, PermissionGuideActivity_26_10_25.class)), 10);

        LinearLayout clearRow = linkRow("清空记录", "只删本机会话与感悟，不影响已选应用", null);
        clearRow.setOnClickListener(v -> confirmClear());
        Ui_26_10_25.add(page, clearRow, 10);

        LinearLayout logCard = Ui_26_10_25.card(activity);
        logList = Ui_26_10_25.vertical(activity);
        logList.setVisibility(View.GONE);
        LinearLayout logHead = Ui_26_10_25.horizontal(activity);
        logHead.addView(Ui_26_10_25.text(activity, "运行记录（排错用）", 15,
                Ui_26_10_25.INK, true), new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView toggle = Ui_26_10_25.action(activity, "展开", () -> {
        });
        toggle.setOnClickListener(v -> {
            logsExpanded = !logsExpanded;
            toggle.setText(logsExpanded ? "收起" : "展开");
            logList.setVisibility(logsExpanded ? View.VISIBLE : View.GONE);
            if (logsExpanded) renderLogs();
        });
        logHead.addView(toggle);
        logCard.addView(logHead);
        logCard.addView(logList);
        Ui_26_10_25.add(page, logCard, 10);

        LinearLayout about = Ui_26_10_25.card(activity);
        about.addView(Ui_26_10_25.text(activity, "关于一念间", 15, Ui_26_10_25.INK, true));
        about.addView(Ui_26_10_25.hint(activity,
                "v4 本地验证版 · 不联网 · 不读屏 · 记录只在本机。\n"
                        + "当前是自用验证包，不是公开发布版本。"));
        Ui_26_10_25.add(page, about, 20);

        refresh();
    }

    private LinearLayout statusRow(LinearLayout parent) {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, Ui_26_10_25.dp(activity, 12), 0, 0);
        parent.addView(row);
        return row;
    }

    private StatusRefs statusRefs(LinearLayout row, String titleText) {
        LinearLayout box = Ui_26_10_25.vertical(activity);
        TextView title = Ui_26_10_25.text(activity, titleText, 14, Ui_26_10_25.INK, true);
        TextView state = Ui_26_10_25.text(activity, "", 12, Ui_26_10_25.MUTED, false);
        box.addView(title);
        box.addView(state);
        row.addView(box, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return new StatusRefs(title, state);
    }

    private TextView rowAction(LinearLayout row, String label, Runnable run) {
        TextView action = Ui_26_10_25.action(activity, label, run);
        action.setPadding(Ui_26_10_25.dp(activity, 8), Ui_26_10_25.dp(activity, 6), 0,
                Ui_26_10_25.dp(activity, 6));
        row.addView(action);
        return action;
    }

    private LinearLayout linkRow(String title, String subtitle, Intent intent) {
        LinearLayout row = Ui_26_10_25.horizontal(activity);
        row.setBackground(Ui_26_10_25.rippleStroke(activity, Color.WHITE, Ui_26_10_25.LINE, 16));
        row.setPadding(Ui_26_10_25.dp(activity, 16), Ui_26_10_25.dp(activity, 14),
                Ui_26_10_25.dp(activity, 12), Ui_26_10_25.dp(activity, 14));
        LinearLayout box = Ui_26_10_25.vertical(activity);
        box.addView(Ui_26_10_25.text(activity, title, 15, Ui_26_10_25.INK, true));
        box.addView(Ui_26_10_25.text(activity, subtitle, 12, Ui_26_10_25.MUTED, false));
        row.addView(box, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        ImageView chevron = new ImageView(activity);
        chevron.setImageResource(R.drawable.ic_chevron_right);
        chevron.setColorFilter(Ui_26_10_25.MUTED);
        row.addView(chevron, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(activity, 18), Ui_26_10_25.dp(activity, 18)));
        if (intent != null) row.setOnClickListener(v -> activity.startActivity(intent));
        return row;
    }

    private void askNotifications() {
        if (Build.VERSION.SDK_INT >= 33
                && activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATIONS);
        } else {
            Perms_26_10_25.openNotificationSettings(activity);
        }
    }

    private void confirmClear() {
        new AlertDialog.Builder(activity)
                .setTitle("清空记录？")
                .setMessage("会删除本机的会话、事件和感悟；已选择的应用不会变。清空后无法恢复。")
                .setNegativeButton("取消", null)
                .setPositiveButton("清空", (dialog, which) -> {
                    IntentDb_26_10_25.get(activity).clearAllRecords();
                    Ui_26_10_25.toast(activity, "记录已清空");
                    refresh();
                })
                .show();
    }

    @Override
    public void refresh() {
        if (activity == null) return;
        boolean accessibilityOn = Perms_26_10_25.accessibilityEnabled(activity);
        update(accessibility, accessibilityOn,
                accessibilityOn ? "已开启" : "未开启，提醒不会工作");
        accessibilityAction.setText(accessibilityOn ? "设置" : "去开启");

        boolean notificationsOn = Perms_26_10_25.notificationsGranted(activity);
        update(notification, notificationsOn,
                notificationsOn ? "已允许（仅兜底与开机提示）" : "未允许；不影响确认页");
        notificationAction.setText(notificationsOn ? "设置" : "去开启");

        boolean batteryOn = Perms_26_10_25.ignoringBatteryOptimizations(activity);
        update(battery, batteryOn,
                batteryOn ? "已允许，减少被清理" : "未设置，可能更早被清理");
        batteryAction.setText(batteryOn ? "设置" : "去设置");

        update(autostart, false,
                "需在系统设置手动允许");
        update(protection, KeepAliveService_26_10_26.running,
                KeepAliveService_26_10_26.running ? "前台服务运行中，降低被清理概率" : "未运行；打开 App 会尝试启动");

        IntentDb_26_10_25 db = IntentDb_26_10_25.get(activity);
        long connected = db.lastServiceWall("connected");
        long disconnected = db.lastServiceWall("disconnected");
        if (connected == 0) {
            lastLine.setText("还没有连接记录。开启无障碍后，这里会显示最近一次连接时间。");
        } else {
            String when = StatsMath_26_10_25.monthDay(connected) + " "
                    + StatsMath_26_10_25.clock(connected);
            lastLine.setText(disconnected > connected
                    ? "最近连接 " + when + "；之后断开过，可能需要重新开启。"
                    : "最近连接 " + when + "。");
        }
        if (logsExpanded) renderLogs();
    }

    private void update(StatusRefs refs, boolean ok, String state) {
        refs.state.setText(state);
        refs.title.setTextColor(ok ? Ui_26_10_25.INK : Ui_26_10_25.WARN);
    }

    private void renderLogs() {
        logList.removeAllViews();
        List<IntentDb_26_10_25.ServiceEntry> logs =
                IntentDb_26_10_25.get(activity).serviceLog(10);
        if (logs.isEmpty()) {
            logList.addView(Ui_26_10_25.hint(activity, "暂无记录"));
            return;
        }
        for (IntentDb_26_10_25.ServiceEntry entry : logs) {
            String label = entry.event;
            if ("connected".equals(entry.event)) label = "服务已连接";
            else if ("disconnected".equals(entry.event)) label = "服务已断开";
            else if ("boot".equals(entry.event)) label = "开机";
            else if ("restore".equals(entry.event)) label = "恢复会话";
            else if ("overlay_error".equals(entry.event)) label = "提醒页显示失败";
            logList.addView(Ui_26_10_25.text(activity,
                    StatsMath_26_10_25.monthDay(entry.wall) + " "
                            + StatsMath_26_10_25.clock(entry.wall) + " · " + label,
                    12, Ui_26_10_25.MUTED, false));
        }
    }

    private static class StatusRefs {
        final TextView title;
        final TextView state;

        StatusRefs(TextView title, TextView state) {
            this.title = title;
            this.state = state;
        }
    }
}
