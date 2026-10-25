package com.intentpause.app;

import android.content.Intent;
import android.graphics.Color;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

/** 首页：运行状态 + 已控制的应用。默认只展开前 3 个，避免一进来就是长列表。 */
public class HomeTab_26_10_25 extends ScrollView implements Refreshable_26_10_25 {
    private static final int COLLAPSED_COUNT = 3;

    private final MainActivity activity;
    private final LinearLayout appList;
    private final View statusDot;
    private final TextView statusTitle;
    private final TextView statusLine;
    private final TextView todayLine;
    private final Button statusButton;
    private boolean expanded;

    public HomeTab_26_10_25(MainActivity activity) {
        super(activity);
        this.activity = activity;
        setFillViewport(true);
        setBackgroundColor(Ui_26_10_25.PAPER);
        setClipToPadding(false);

        LinearLayout page = Ui_26_10_25.vertical(activity);
        page.setPadding(Ui_26_10_25.dp(activity, 22), Ui_26_10_25.dp(activity, 22),
                Ui_26_10_25.dp(activity, 22), Ui_26_10_25.dp(activity, 32));
        addView(page, new ScrollView.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        LinearLayout titleRow = Ui_26_10_25.horizontal(activity);
        ImageView mascot = new ImageView(activity);
        mascot.setImageResource(R.drawable.ic_launcher_foreground);
        titleRow.addView(mascot, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(activity, 44), Ui_26_10_25.dp(activity, 44)));
        LinearLayout titleBox = Ui_26_10_25.vertical(activity);
        titleBox.setPadding(Ui_26_10_25.dp(activity, 10), 0, 0, 0);
        titleBox.addView(Ui_26_10_25.text(activity, "慢一拍", 28, Ui_26_10_25.INK, true));
        titleBox.addView(Ui_26_10_25.text(activity, "打开之前，慢一拍。", 14, Ui_26_10_25.MUTED, false));
        titleRow.addView(titleBox);
        Ui_26_10_25.add(page, titleRow, 0);

        LinearLayout statusCard = Ui_26_10_25.card(activity);
        LinearLayout statusTop = Ui_26_10_25.horizontal(activity);
        statusDot = new View(activity);
        statusDot.setBackground(Ui_26_10_25.round(activity, Ui_26_10_25.WARN, 999));
        statusTop.addView(statusDot, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(activity, 10), Ui_26_10_25.dp(activity, 10)));
        statusTitle = Ui_26_10_25.text(activity, "正在检查…", 17, Ui_26_10_25.INK, true);
        statusTitle.setPadding(Ui_26_10_25.dp(activity, 10), 0, 0, 0);
        statusTop.addView(statusTitle);
        statusCard.addView(statusTop);

        statusLine = Ui_26_10_25.hint(activity, "");
        statusLine.setPadding(0, Ui_26_10_25.dp(activity, 8), 0, 0);
        statusCard.addView(statusLine);

        todayLine = Ui_26_10_25.text(activity, "", 13, Ui_26_10_25.MUTED, false);
        todayLine.setPadding(0, Ui_26_10_25.dp(activity, 10), 0, 0);
        statusCard.addView(todayLine);

        statusButton = Ui_26_10_25.button(activity, "去开启", true);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(activity, 50));
        bp.topMargin = Ui_26_10_25.dp(activity, 14);
        statusCard.addView(statusButton, bp);
        statusCard.setOnClickListener(v -> statusButton.performClick());
        Ui_26_10_25.add(page, statusCard, 22);

        LinearLayout sectionRow = Ui_26_10_25.horizontal(activity);
        TextView section = Ui_26_10_25.section(activity, "已控制的应用");
        sectionRow.addView(section, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView manage = Ui_26_10_25.action(activity, "管理", () ->
                activity.startActivity(new Intent(activity, AppPickerActivity_26_10_25.class)));
        sectionRow.addView(manage);
        Ui_26_10_25.add(page, sectionRow, 28);

        appList = Ui_26_10_25.vertical(activity);
        Ui_26_10_25.add(page, appList, 8);

        Button add = Ui_26_10_25.button(activity, "添加应用", true);
        add.setOnClickListener(v -> activity.startActivity(
                new Intent(activity, AppPickerActivity_26_10_25.class)));
        Ui_26_10_25.add(page, add, 14);

        LinearLayout tip = Ui_26_10_25.card(activity);
        tip.addView(Ui_26_10_25.text(activity, "怎么工作", 15, Ui_26_10_25.INK, true));
        tip.addView(Ui_26_10_25.hint(activity,
                "默认 10 分钟后复查；短暂离开不超过 1 分钟，原计时不变。想休息也是正当选择。"));
        Ui_26_10_25.add(page, tip, 26);

        refresh();
    }

    @Override
    public void refresh() {
        if (activity == null) return;
        boolean enabled = Perms_26_10_25.accessibilityEnabled(activity);
        statusDot.setBackground(Ui_26_10_25.round(activity,
                enabled ? Ui_26_10_25.GREEN : Ui_26_10_25.WARN, 999));
        statusTitle.setText(enabled ? "提醒已开启" : "提醒待开启");
        statusButton.setText(enabled ? "查看运行状态" : "去开启无障碍服务");
        statusButton.setBackground(Ui_26_10_25.ripple(activity,
                enabled ? Ui_26_10_25.GREEN_SOFT : Ui_26_10_25.GREEN, 14));
        statusButton.setTextColor(enabled ? Ui_26_10_25.GREEN : Color.WHITE);
        statusButton.setOnClickListener(v -> {
            if (Perms_26_10_25.accessibilityEnabled(activity)) {
                activity.startActivity(new Intent(activity, PermissionGuideActivity_26_10_25.class));
            } else {
                Perms_26_10_25.openAccessibilitySettings(activity);
            }
        });

        IntentDb_26_10_25 db = IntentDb_26_10_25.get(activity);
        long connected = db.lastServiceWall("connected");
        long disconnected = db.lastServiceWall("disconnected");
        if (connected == 0) {
            statusLine.setText("还没有成功连接过无障碍服务。开启后，打开受控应用才会停一下。");
        } else if (disconnected > connected) {
            statusLine.setText("上次连接 " + StatsMath_26_10_25.monthDay(connected) + " "
                    + StatsMath_26_10_25.clock(connected) + "；服务随后断开，可能需要重新开启。");
        } else {
            statusLine.setText("本次连接于 " + StatsMath_26_10_25.monthDay(connected) + " "
                    + StatsMath_26_10_25.clock(connected) + "。");
        }

        long begin = StatsMath_26_10_25.startOfDay(System.currentTimeMillis());
        long end = StatsMath_26_10_25.addDays(begin, 1);
        IntentDb_26_10_25.DayStats stats = db.dayStats(begin, end);
        todayLine.setText("今天：打开 " + stats.opens + " 次 · 提醒 " + stats.reminders
                + " 次 · 在应用内 " + StatsMath_26_10_25.duration(stats.foregroundMs));

        renderApps();
    }

    private void renderApps() {
        appList.removeAllViews();
        List<String> selected = Apps_26_10_25.selectedPackages(activity);
        if (selected.isEmpty()) {
            LinearLayout empty = Ui_26_10_25.card(activity);
            empty.addView(Ui_26_10_25.text(activity, "还没有选择应用", 16, Ui_26_10_25.INK, true));
            empty.addView(Ui_26_10_25.hint(activity,
                    "选一个最常无意识打开的应用试试。打开它时，会先停一下。"));
            Ui_26_10_25.add(appList, empty, 8);
            return;
        }
        int limit = expanded ? selected.size() : Math.min(COLLAPSED_COUNT, selected.size());
        for (int i = 0; i < limit; i++) {
            Ui_26_10_25.add(appList, appRow(selected.get(i)), 8);
        }
        if (selected.size() > COLLAPSED_COUNT) {
            TextView more = Ui_26_10_25.action(activity,
                    expanded ? "收起" : "展开全部（" + selected.size() + "）", () -> {
                        expanded = !expanded;
                        renderApps();
                    });
            more.setGravity(Gravity.CENTER);
            more.setPadding(0, Ui_26_10_25.dp(activity, 10), 0, Ui_26_10_25.dp(activity, 4));
            Ui_26_10_25.add(appList, more, 2);
        }
    }

    private View appRow(String pkg) {
        LinearLayout row = Ui_26_10_25.horizontal(activity);
        row.setBackground(Ui_26_10_25.rippleStroke(activity, Color.WHITE, Ui_26_10_25.LINE, 16));
        row.setPadding(Ui_26_10_25.dp(activity, 14), Ui_26_10_25.dp(activity, 10),
                Ui_26_10_25.dp(activity, 10), Ui_26_10_25.dp(activity, 10));
        ImageView icon = new ImageView(activity);
        android.graphics.drawable.Drawable drawable = Apps_26_10_25.icon(activity, pkg);
        if (drawable != null) icon.setImageDrawable(drawable);
        else icon.setImageResource(R.drawable.ic_launcher_foreground);
        row.addView(icon, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(activity, 34), Ui_26_10_25.dp(activity, 34)));

        LinearLayout textBox = Ui_26_10_25.vertical(activity);
        textBox.setPadding(Ui_26_10_25.dp(activity, 12), 0, 0, 0);
        textBox.addView(Ui_26_10_25.text(activity, Apps_26_10_25.label(activity, pkg),
                16, Ui_26_10_25.INK, true));
        textBox.addView(Ui_26_10_25.text(activity, "每次打开先确认 · 默认 10 分钟",
                12, Ui_26_10_25.MUTED, false));
        row.addView(textBox, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Switch toggle = new Switch(activity);
        toggle.setChecked(Apps_26_10_25.selected(activity, pkg));
        ColorStateList track = new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, new int[]{}},
                new int[]{Ui_26_10_25.GREEN, Color.rgb(217, 222, 217)});
        toggle.setTrackTintList(track);
        ColorStateList thumb = new ColorStateList(
                new int[][]{{android.R.attr.state_checked}, new int[]{}},
                new int[]{Color.WHITE, Color.WHITE});
        toggle.setThumbTintList(thumb);
        toggle.setContentDescription("监测" + Apps_26_10_25.label(activity, pkg));
        toggle.setOnCheckedChangeListener((buttonView, checked) -> {
            Apps_26_10_25.setSelected(activity, pkg, checked);
            toggle.post(this::refresh);
        });
        row.addView(toggle, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, Ui_26_10_25.dp(activity, 48)));
        row.setOnClickListener(v -> toggle.setChecked(!toggle.isChecked()));
        return row;
    }
}
