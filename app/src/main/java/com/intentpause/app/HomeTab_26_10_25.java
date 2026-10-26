package com.intentpause.app;

import android.content.Intent;
import android.text.InputType;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
    private final TextView[] todayMetrics = new TextView[3];
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
        mascot.setImageResource(R.drawable.mascot_nuonuo_cutout);
        titleRow.addView(mascot, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(activity, 54), Ui_26_10_25.dp(activity, 54)));
        NuoNuoGesture_26_10_26.attach(mascot);
        Ui_26_10_25.breathe(mascot);
        LinearLayout titleBox = Ui_26_10_25.vertical(activity);
        titleBox.setPadding(Ui_26_10_25.dp(activity, 10), 0, 0, 0);
        TextView brand = Ui_26_10_25.text(activity, "一念间", 28, Ui_26_10_25.INK, true);
        brand.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        titleBox.addView(brand);
        titleBox.addView(Ui_26_10_25.text(activity, "这一念，听自己。", 14, Ui_26_10_25.MUTED, false));
        titleRow.addView(titleBox);
        Ui_26_10_25.add(page, titleRow, 0);

        LinearLayout statusCard = Ui_26_10_25.card(activity);
        GradientDrawable statusBackground = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{0xFFE4EEE3, 0xFFF8F3E8});
        statusBackground.setCornerRadius(Ui_26_10_25.dp(activity, 24));
        statusCard.setBackground(statusBackground);
        TextView statusEyebrow = Ui_26_10_25.text(activity,
                "TODAY  /  今天", 11, Ui_26_10_25.GREEN, true);
        statusCard.addView(statusEyebrow);
        LinearLayout statusTop = Ui_26_10_25.horizontal(activity);
        statusTop.setPadding(0, Ui_26_10_25.dp(activity, 13), 0, 0);
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

        LinearLayout metrics = Ui_26_10_25.horizontal(activity);
        String[] metricLabels = {"打开", "提醒", "前台分钟"};
        for (int i = 0; i < metricLabels.length; i++) {
            LinearLayout item = Ui_26_10_25.vertical(activity);
            item.setPadding(Ui_26_10_25.dp(activity, 11), Ui_26_10_25.dp(activity, 9),
                    Ui_26_10_25.dp(activity, 4), Ui_26_10_25.dp(activity, 9));
            item.setBackground(Ui_26_10_25.round(activity, 0xAEFFFFFF, 14));
            todayMetrics[i] = Ui_26_10_25.text(activity, "0", 22,
                    Ui_26_10_25.INK, true);
            item.addView(todayMetrics[i]);
            item.addView(Ui_26_10_25.text(activity, metricLabels[i], 11,
                    Ui_26_10_25.MUTED, false));
            LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(0,
                    Ui_26_10_25.dp(activity, 67), 1f);
            if (i > 0) itemLp.leftMargin = Ui_26_10_25.dp(activity, 8);
            metrics.addView(item, itemLp);
        }
        Ui_26_10_25.add(statusCard, metrics, 15);

        statusButton = Ui_26_10_25.button(activity, "去开启", true);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(activity, 50));
        bp.topMargin = Ui_26_10_25.dp(activity, 14);
        statusCard.addView(statusButton, bp);
        statusCard.setOnClickListener(v -> statusButton.performClick());
        Ui_26_10_25.add(page, statusCard, 22);

        LinearLayout sectionRow = Ui_26_10_25.horizontal(activity);
        TextView section = Ui_26_10_25.section(activity, "提醒我想一想");
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
        TextView encourageSection = Ui_26_10_25.section(activity, "给我一点鼓励");
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
                "提醒应用：切到别的应用不超过 1 分钟，原截止时间不变；"
                        + "回桌面会结束本次。"
                        + "鼓励应用按亮屏前台时间累计。"));
        Ui_26_10_25.add(page, tip, 26);

        LinearLayout notes = Ui_26_10_25.card(activity);
        notes.setBackground(Ui_26_10_25.roundStroke(activity,
                0xFFFFFAF3, Ui_26_10_25.LINE, 24));
        LinearLayout notesHeader = Ui_26_10_25.horizontal(activity);
        notesHeader.setGravity(Gravity.CENTER_VERTICAL);
        notesHeader.addView(Ui_26_10_25.text(activity, "糯糯的小纸条", 19,
                Ui_26_10_25.INK, true), new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView noteMark = Ui_26_10_25.text(activity, "想看时再打开", 11,
                Ui_26_10_25.GREEN, true);
        noteMark.setPadding(Ui_26_10_25.dp(activity, 9), Ui_26_10_25.dp(activity, 6),
                Ui_26_10_25.dp(activity, 9), Ui_26_10_25.dp(activity, 6));
        noteMark.setBackground(Ui_26_10_25.round(activity, 0xFFE7EEE4, 99));
        notesHeader.addView(noteMark);
        notes.addView(notesHeader);
        Ui_26_10_25.add(notes, Ui_26_10_25.hint(activity,
                "三张短文，挑一张此刻想读的。"), 8);
        LinearLayout notePair = Ui_26_10_25.horizontal(activity);
        notePair.addView(noteCard("01  今天读到这里", 0xFFEAF0E4, 0),
                new LinearLayout.LayoutParams(0, Ui_26_10_25.dp(activity, 74), 1f));
        LinearLayout.LayoutParams secondNote = new LinearLayout.LayoutParams(0,
                Ui_26_10_25.dp(activity, 74), 1f);
        secondNote.leftMargin = Ui_26_10_25.dp(activity, 10);
        notePair.addView(noteCard("02  先从一点开始", 0xFFF5E8DD, 1), secondNote);
        Ui_26_10_25.add(notes, notePair, 16);
        Ui_26_10_25.add(notes,
                noteCard("03  就是想放松一下", 0xFFF0EAE8, 2), 10);
        TextView ownNotes = Ui_26_10_25.action(activity, "我的纸条 · 查看或写一张",
                () -> NuoNuoNotes_26_10_26.showMine(activity));
        ownNotes.setGravity(Gravity.CENTER_VERTICAL);
        ownNotes.setMinHeight(Ui_26_10_25.dp(activity, 52));
        ownNotes.setBackground(Ui_26_10_25.ripple(activity,
                Ui_26_10_25.GREEN_SOFT, 14));
        Ui_26_10_25.add(notes, ownNotes, 12);
        Ui_26_10_25.add(page, notes, 12);

        refresh();
    }

    private TextView noteCard(String label, int background, int index) {
        TextView view = Ui_26_10_25.text(activity, label, 14, Ui_26_10_25.INK, true);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setPadding(Ui_26_10_25.dp(activity, 12), 0,
                Ui_26_10_25.dp(activity, 8), 0);
        view.setMinHeight(Ui_26_10_25.dp(activity, 72));
        view.setBackground(Ui_26_10_25.ripple(activity, background, 16));
        view.setOnClickListener(v -> NuoNuoNotes_26_10_26.show(activity, index));
        return view;
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
        todayMetrics[0].setText(String.valueOf(stats.opens));
        todayMetrics[1].setText(String.valueOf(stats.reminders));
        todayMetrics[2].setText(String.valueOf(stats.foregroundMs / 60000));

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
        String note = "累计使用 " + Apps_26_10_25.encourageInterval(activity, pkg) + " 分钟轻鼓励";
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
        LinearLayout box = Ui_26_10_25.vertical(activity);
        box.addView(Ui_26_10_25.hint(activity,
                "只给一句轻提示，不挡住正在做的事。每个应用可单独设置。"));
        android.widget.Switch onOpen = encourageSwitch("打开时给一句鼓励",
                Apps_26_10_25.encourageOnOpen(activity, pkg));
        Ui_26_10_25.add(box, onOpen, 16);
        android.widget.Switch during = encourageSwitch("持续使用时轻提醒",
                Apps_26_10_25.encourageDuring(activity, pkg));
        Ui_26_10_25.add(box, during, 8);
        box.addView(GentleSheet_26_10_26.label(activity,
                "累计使用多久提示一次 · 10–120 分钟"));
        EditText interval = GentleSheet_26_10_26.input(activity,
                String.valueOf(Apps_26_10_25.encourageInterval(activity, pkg)),
                "例如 20", false);
        interval.setInputType(InputType.TYPE_CLASS_NUMBER);
        box.addView(interval);
        Ui_26_10_25.add(box, Ui_26_10_25.hint(activity,
                "只累计亮屏且应用在前台的时间，切走或锁屏时暂停。"), 8);

        box.addView(GentleSheet_26_10_26.label(activity, "提示内容"));
        android.widget.Switch preset = encourageSwitch("使用中轮换糯糯的话",
                Apps_26_10_25.presetEnabled(activity, pkg));
        box.addView(preset);
        Ui_26_10_25.add(box, Ui_26_10_25.hint(activity,
                "关闭后，使用中改为轮换你写的句子。"), 4);
        box.addView(GentleSheet_26_10_26.label(activity, "内置文案的顺序"));
        RadioGroup group = new RadioGroup(activity);
        group.setOrientation(RadioGroup.HORIZONTAL);
        int randomId = View.generateViewId();
        int sequentialId = View.generateViewId();
        RadioButton random = new RadioButton(activity);
        random.setText("随机");
        random.setTextColor(Ui_26_10_25.INK);
        random.setTypeface(android.graphics.Typeface.SANS_SERIF);
        random.setId(randomId);
        RadioButton sequential = new RadioButton(activity);
        sequential.setText("按顺序");
        sequential.setTextColor(Ui_26_10_25.INK);
        sequential.setTypeface(android.graphics.Typeface.SANS_SERIF);
        sequential.setId(sequentialId);
        group.addView(random);
        group.addView(sequential);
        group.check("sequential".equals(Apps_26_10_25.rotation(activity, pkg)) ? sequentialId : randomId);
        box.addView(group);

        box.addView(GentleSheet_26_10_26.label(activity, "写给自己的话 · 每行一句"));
        EditText custom = GentleSheet_26_10_26.input(activity,
                String.join("\n", Apps_26_10_25.customEncourageMessages(activity, pkg)),
                "例如：想读的话，就从这一页开始", true);
        box.addView(custom);
        Ui_26_10_25.add(box, Ui_26_10_25.hint(activity,
                "打开时优先显示自写句；使用中按上方设置轮换。"), 8);

        GentleSheet_26_10_26.show(activity,
                Apps_26_10_25.label(activity, pkg) + " · 鼓励方式", box,
                "保存", () -> {
                    int minutes;
                    try { minutes = Integer.parseInt(interval.getText().toString().trim()); }
                    catch (Exception ignored) { minutes = 0; }
                    if (minutes < 10 || minutes > 120) {
                        interval.setError("请输入 10–120 分钟");
                        interval.requestFocus();
                        return false;
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
                    return true;
                });
    }

    private android.widget.Switch encourageSwitch(String title, boolean checked) {
        android.widget.Switch view = new android.widget.Switch(activity);
        view.setText(title);
        view.setTextSize(15);
        view.setTextColor(Ui_26_10_25.INK);
        view.setTypeface(android.graphics.Typeface.SANS_SERIF);
        view.setChecked(checked);
        view.setMinHeight(Ui_26_10_25.dp(activity, 52));
        view.setPadding(Ui_26_10_25.dp(activity, 12), 0,
                Ui_26_10_25.dp(activity, 12), 0);
        view.setBackground(Ui_26_10_25.roundStroke(activity, Color.WHITE,
                Ui_26_10_25.LINE, 14));
        return view;
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
        LinearLayout body = Ui_26_10_25.vertical(activity);
        body.addView(Ui_26_10_25.hint(activity,
                "下次打开时默认选中；每一次仍可临时改。"));
        body.addView(GentleSheet_26_10_26.label(activity, "常用时长"));
        LinearLayout choices = Ui_26_10_25.horizontal(activity);
        int[] values = {3, 10, 30};
        int[] selected = {current == 3 || current == 10 || current == 30 ? current : 0};
        android.widget.Button[] buttons = new android.widget.Button[3];
        for (int i = 0; i < values.length; i++) {
            final int value = values[i];
            buttons[i] = Ui_26_10_25.pill(activity, value + " 分钟", selected[0] == value);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    Ui_26_10_25.dp(activity, 48), 1f);
            if (i > 0) lp.leftMargin = Ui_26_10_25.dp(activity, 8);
            choices.addView(buttons[i], lp);
        }
        body.addView(choices);
        body.addView(GentleSheet_26_10_26.label(activity, "自定义 · 1–180 分钟"));
        EditText input = GentleSheet_26_10_26.input(activity,
                selected[0] == 0 ? String.valueOf(current) : "", "例如 15", false);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        body.addView(input);
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) selected[0] = 0;
            for (int j = 0; j < buttons.length; j++) {
                buttons[j].setBackground(Ui_26_10_25.ripple(activity,
                        selected[0] == values[j] ? Ui_26_10_25.GREEN
                                : Ui_26_10_25.GREEN_SOFT, 999));
                buttons[j].setTextColor(selected[0] == values[j]
                        ? Color.WHITE : Ui_26_10_25.GREEN);
            }
        });
        for (int i = 0; i < buttons.length; i++) {
            final int value = values[i];
            buttons[i].setOnClickListener(v -> {
                selected[0] = value;
                input.clearFocus();
                for (int j = 0; j < buttons.length; j++) {
                    buttons[j].setBackground(Ui_26_10_25.ripple(activity,
                            value == values[j] ? Ui_26_10_25.GREEN
                                    : Ui_26_10_25.GREEN_SOFT, 999));
                    buttons[j].setTextColor(value == values[j]
                            ? Color.WHITE : Ui_26_10_25.GREEN);
                }
            });
        }
        GentleSheet_26_10_26.show(activity, name + " · 默认使用多久", body,
                "保存", () -> {
                    int minutes = selected[0];
                    if (minutes == 0) {
                        try { minutes = Integer.parseInt(input.getText().toString().trim()); }
                        catch (Exception ignored) { minutes = 0; }
                    }
                    if (minutes < 1 || minutes > 180) {
                        input.setError("请输入 1–180 分钟");
                        input.requestFocus();
                        return false;
                    }
                    Apps_26_10_25.setDefaultMinutes(activity, pkg, minutes);
                    refresh();
                    return true;
                });
    }
}
