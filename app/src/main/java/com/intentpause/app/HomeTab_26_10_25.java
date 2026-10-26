package com.intentpause.app;

import android.content.Intent;
import android.text.InputType;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.List;

/** 首页：运行状态 + 已控制的应用。默认只展开前 3 个，避免一进来就是长列表。 */
public class HomeTab_26_10_25 extends ScrollView implements Refreshable_26_10_25 {
    private static final int COLLAPSED_COUNT = 3;

    private final MainActivity activity;
    private final LinearLayout appList;
    private final LinearLayout encourageList;
    private final View statusDot;
    private final TextView statusTitle;
    private final TextView statusLine;
    private final TextView todayLine;
    private final Button statusButton;
    private boolean expanded;
    private boolean encourageExpanded;

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
        mascot.setImageResource(R.drawable.mascot_xiaohuan);
        titleRow.addView(mascot, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(activity, 44), Ui_26_10_25.dp(activity, 44)));
        Ui_26_10_25.breathe(mascot);
        LinearLayout titleBox = Ui_26_10_25.vertical(activity);
        titleBox.setPadding(Ui_26_10_25.dp(activity, 10), 0, 0, 0);
        titleBox.addView(Ui_26_10_25.text(activity, "一念间", 28, Ui_26_10_25.INK, true));
        titleBox.addView(Ui_26_10_25.text(activity, "陪你停一下，也陪你做下去。", 14, Ui_26_10_25.MUTED, false));
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
        TextView section = Ui_26_10_25.section(activity, "受控应用");
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

        LinearLayout encourageHead = Ui_26_10_25.horizontal(activity);
        TextView encourageSection = Ui_26_10_25.section(activity, "鼓励应用");
        encourageHead.addView(encourageSection, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView manageEncourage = Ui_26_10_25.action(activity, "管理", () ->
                activity.startActivity(new Intent(activity, AppPickerActivity_26_10_25.class)
                        .putExtra("mode", "encourage")));
        encourageHead.addView(manageEncourage);
        Ui_26_10_25.add(page, encourageHead, 28);
        encourageList = Ui_26_10_25.vertical(activity);
        Ui_26_10_25.add(page, encourageList, 8);
        Button addEncourage = Ui_26_10_25.button(activity, "添加鼓励应用", false);
        addEncourage.setOnClickListener(v -> activity.startActivity(
                new Intent(activity, AppPickerActivity_26_10_25.class)
                        .putExtra("mode", "encourage")));
        Ui_26_10_25.add(page, addEncourage, 14);

        LinearLayout tip = Ui_26_10_25.card(activity);
        tip.addView(Ui_26_10_25.text(activity, "怎么工作", 15, Ui_26_10_25.INK, true));
        tip.addView(Ui_26_10_25.hint(activity,
                "每个应用可以单独设置默认时长（3/10/30 或自定义）；到时如果还在应用内会复查；"
                        + "短暂离开不超过 1 分钟，原计时不变。"));
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
        statusButton.setText(enabled ? "查看状态" : "开启提醒");
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
            statusLine.setText("开启后，打开受控应用才会先停一下。");
        } else if (disconnected > connected) {
            statusLine.setText("服务断开过，建议重新开启。");
        } else {
            statusLine.setText("已连接 · " + StatsMath_26_10_25.clock(connected));
        }

        long begin = StatsMath_26_10_25.startOfDay(System.currentTimeMillis());
        long end = StatsMath_26_10_25.addDays(begin, 1);
        IntentDb_26_10_25.DayStats stats = db.dayStats(begin, end);
        todayLine.setText("今天：打开 " + stats.opens + " 次 · 提醒 " + stats.reminders
                + " 次 · 在应用内 " + StatsMath_26_10_25.duration(stats.foregroundMs));

        renderApps();
        renderEncourageApps();
    }

    private void renderEncourageApps() {
        encourageList.removeAllViews();
        List<String> encouraged = Apps_26_10_25.encouragePackages(activity);
        if (encouraged.isEmpty()) {
            LinearLayout empty = Ui_26_10_25.card(activity);
            empty.addView(Ui_26_10_25.text(activity, "还没有鼓励应用", 15,
                    Ui_26_10_25.INK, true));
            empty.addView(Ui_26_10_25.hint(activity,
                    "把想多用的应用放进来：打开时给一句鼓励，用久了轻轻提醒一下。"));
            Ui_26_10_25.add(encourageList, empty, 8);
            return;
        }
        int limit = encourageExpanded ? encouraged.size()
                : Math.min(COLLAPSED_COUNT, encouraged.size());
        for (int i = 0; i < limit; i++) {
            Ui_26_10_25.add(encourageList, encourageRow(encouraged.get(i)), 8);
        }
        if (encouraged.size() > COLLAPSED_COUNT) {
            TextView more = Ui_26_10_25.action(activity,
                    encourageExpanded ? "收起" : "展开全部（" + encouraged.size() + "）", () -> {
                        encourageExpanded = !encourageExpanded;
                        renderEncourageApps();
                    });
            more.setGravity(Gravity.CENTER);
            more.setPadding(0, Ui_26_10_25.dp(activity, 10), 0, Ui_26_10_25.dp(activity, 4));
            Ui_26_10_25.add(encourageList, more, 2);
        }
    }

    private View encourageRow(String pkg) {
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
        String note = "每 " + Apps_26_10_25.encourageInterval(activity, pkg) + " 分钟轻鼓励";
        if (!Apps_26_10_25.encourageOnOpen(activity, pkg)) note = "使用中轻鼓励";
        if (!Apps_26_10_25.encourageDuring(activity, pkg)) note = "打开时轻鼓励";
        textBox.addView(Ui_26_10_25.text(activity, note + " · 点按设置",
                12, Ui_26_10_25.MUTED, false));
        row.addView(textBox, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Toggle_26_10_25 toggle = new Toggle_26_10_25(activity);
        toggle.setChecked(true, false);
        toggle.setContentDescription("鼓励" + Apps_26_10_25.label(activity, pkg));
        toggle.setOnToggleRequested(next -> {
            if (!next) {
                Apps_26_10_25.setEncouraged(activity, pkg, false);
                refresh();
            }
        });
        row.addView(toggle, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(activity, 48), Ui_26_10_25.dp(activity, 28)));
        row.setOnClickListener(v -> showEncourageDialog(pkg));
        return row;
    }

    private void showEncourageDialog(String pkg) {
        ScrollView scroll = new ScrollView(activity);
        LinearLayout box = Ui_26_10_25.vertical(activity);
        box.setPadding(Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 8),
                Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 8));
        scroll.addView(box);

        CheckBox onOpen = new CheckBox(activity);
        onOpen.setText("打开时，给一句鼓励");
        onOpen.setTextColor(Ui_26_10_25.INK);
        onOpen.setChecked(Apps_26_10_25.encourageOnOpen(activity, pkg));
        box.addView(onOpen);

        CheckBox during = new CheckBox(activity);
        during.setText("使用中，隔一段时间轻提醒");
        during.setTextColor(Ui_26_10_25.INK);
        during.setChecked(Apps_26_10_25.encourageDuring(activity, pkg));
        box.addView(during);

        EditText interval = new EditText(activity);
        interval.setInputType(InputType.TYPE_CLASS_NUMBER);
        interval.setHint("间隔分钟（10–120）");
        interval.setText(String.valueOf(Apps_26_10_25.encourageInterval(activity, pkg)));
        interval.setTextColor(Ui_26_10_25.INK);
        interval.setHintTextColor(Ui_26_10_25.MUTED);
        box.addView(interval);

        CheckBox preset = new CheckBox(activity);
        preset.setText("使用内置鼓励文案");
        preset.setTextColor(Ui_26_10_25.INK);
        preset.setChecked(Apps_26_10_25.presetEnabled(activity, pkg));
        box.addView(preset);

        TextView rotationLabel = Ui_26_10_25.text(activity, "文案顺序", 13,
                Ui_26_10_25.MUTED, false);
        rotationLabel.setPadding(0, Ui_26_10_25.dp(activity, 8), 0, 0);
        box.addView(rotationLabel);
        RadioGroup group = new RadioGroup(activity);
        group.setOrientation(RadioGroup.HORIZONTAL);
        int randomId = View.generateViewId();
        int sequentialId = View.generateViewId();
        RadioButton random = new RadioButton(activity);
        random.setText("随机");
        random.setTextColor(Ui_26_10_25.INK);
        random.setId(randomId);
        RadioButton sequential = new RadioButton(activity);
        sequential.setText("按顺序");
        sequential.setTextColor(Ui_26_10_25.INK);
        sequential.setId(sequentialId);
        group.addView(random);
        group.addView(sequential);
        group.check("sequential".equals(Apps_26_10_25.rotation(activity, pkg)) ? sequentialId : randomId);
        box.addView(group);

        EditText custom = new EditText(activity);
        custom.setHint("自己写几句，每行一句（可留空）");
        custom.setHintTextColor(Ui_26_10_25.MUTED);
        custom.setTextColor(Ui_26_10_25.INK);
        custom.setTextSize(14);
        custom.setMinLines(3);
        custom.setGravity(Gravity.TOP);
        custom.setText(String.join("\n", Apps_26_10_25.customEncourageMessages(activity, pkg)));
        box.addView(custom);

        new AlertDialog.Builder(activity)
                .setTitle(Apps_26_10_25.label(activity, pkg) + " · 鼓励方式")
                .setView(scroll)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    int minutes = 30;
                    try {
                        minutes = Integer.parseInt(interval.getText().toString().trim());
                    } catch (Exception ignored) {
                    }
                    Apps_26_10_25.setEncourageInterval(activity, pkg, minutes);
                    Apps_26_10_25.setEncourageOnOpen(activity, pkg, onOpen.isChecked());
                    Apps_26_10_25.setEncourageDuring(activity, pkg, during.isChecked());
                    Apps_26_10_25.setPresetEnabled(activity, pkg, preset.isChecked());
                    Apps_26_10_25.setRotation(activity, pkg,
                            group.getCheckedRadioButtonId() == sequentialId ? "sequential" : "random");
                    java.util.List<String> lines = new java.util.ArrayList<>();
                    for (String line : custom.getText().toString().split("\n")) {
                        if (!line.trim().isEmpty()) lines.add(line.trim());
                    }
                    Apps_26_10_25.setCustomEncourageMessages(activity, pkg, lines);
                    refresh();
                })
                .show();
    }

    private void renderApps() {
        appList.removeAllViews();
        List<String> selected = Apps_26_10_25.selectedPackages(activity);
        if (selected.isEmpty()) {
            LinearLayout empty = Ui_26_10_25.card(activity);
            empty.addView(Ui_26_10_25.text(activity, "还没有选择应用", 16, Ui_26_10_25.INK, true));
            empty.addView(Ui_26_10_25.hint(activity,
                    "选一个最常无意识打开的应用。打开它时，会先停一下。"));
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
        textBox.addView(Ui_26_10_25.text(activity,
                "默认 " + Apps_26_10_25.defaultMinutes(activity, pkg) + " 分钟 · 点按修改",
                12, Ui_26_10_25.MUTED, false));
        row.addView(textBox, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        Toggle_26_10_25 toggle = new Toggle_26_10_25(activity);
        toggle.setChecked(Apps_26_10_25.selected(activity, pkg), false);
        toggle.setContentDescription("监测" + Apps_26_10_25.label(activity, pkg));
        toggle.setOnToggleRequested(next -> {
            if (next) {
                Apps_26_10_25.setSelected(activity, pkg, true);
                toggle.post(this::refresh);
            } else {
                confirmRemove(pkg, toggle);
            }
        });
        row.addView(toggle, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(activity, 48), Ui_26_10_25.dp(activity, 28)));
        row.setOnClickListener(v -> showDefaultDialog(pkg));
        return row;
    }

    private void confirmRemove(String pkg, Toggle_26_10_25 toggle) {
        String name = Apps_26_10_25.label(activity, pkg);
        new AlertDialog.Builder(activity)
                .setTitle("不再监测？")
                .setMessage("之后打开“" + name + "”时不会再先停一下；已有记录保留。")
                .setNegativeButton("继续监测", (dialog, which) -> toggle.setChecked(true, true))
                .setPositiveButton("不再监测", (dialog, which) -> {
                    Apps_26_10_25.setSelected(activity, pkg, false);
                    refresh();
                })
                .show();
    }

    private void showDefaultDialog(String pkg) {
        String name = Apps_26_10_25.label(activity, pkg);
        int current = Apps_26_10_25.defaultMinutes(activity, pkg);
        String[] items = {"3 分钟", "10 分钟", "30 分钟", "自定义…"};
        int checked = current == 3 ? 0 : current == 10 ? 1 : current == 30 ? 2 : 3;
        new AlertDialog.Builder(activity)
                .setTitle("“" + name + "”默认使用多久？")
                .setSingleChoiceItems(items, checked, (dialog, which) -> {
                    dialog.dismiss();
                    if (which == 3) askCustomDefault(pkg, current);
                    else {
                        Apps_26_10_25.setDefaultMinutes(activity, pkg, which == 0 ? 3 : which == 1 ? 10 : 30);
                        refresh();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void askCustomDefault(String pkg, int current) {
        EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("1–180 分钟");
        input.setText(String.valueOf(current));
        input.setPadding(Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 8),
                Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 8));
        new AlertDialog.Builder(activity)
                .setTitle("自定义默认时长")
                .setView(input)
                .setNegativeButton("取消", null)
                .setPositiveButton("保存", (dialog, which) -> {
                    int minutes;
                    try {
                        minutes = Integer.parseInt(input.getText().toString().trim());
                    } catch (Exception e) {
                        minutes = 0;
                    }
                    if (minutes < 1 || minutes > 180) {
                        Ui_26_10_25.toast(activity, "请输入 1–180 分钟");
                        return;
                    }
                    Apps_26_10_25.setDefaultMinutes(activity, pkg, minutes);
                    refresh();
                })
                .show();
    }
}
