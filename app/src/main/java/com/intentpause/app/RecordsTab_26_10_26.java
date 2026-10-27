package com.intentpause.app;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 记录页：可触摸的事件花园、日/周/月、柱图、分应用与感悟。 */
public class RecordsTab_26_10_26 extends ScrollView implements Refreshable_26_10_25 {
    private final MainActivity activity;
    private GardenView_26_10_26 garden;
    private final TextView gardenCount;
    private TextView careLine;
    private final TextView rangeLabel;
    private final TextView barTitle;
    private final TextView barTotal;
    private final PeriodBarsView_26_10_26 bars;
    private final TextView[] stats = new TextView[6];
    private final LinearLayout detailsList;
    private final LinearLayout noteCard;
    private EditText noteInput;
    private final TextView noteLabel;
    private TextView gardenToggle;
    private View gardenLegend;
    private TextView gardenRecent;
    private List<IntentDb_26_10_25.GardenMoment> recentMoments;
    private final TextView[] tabs = new TextView[3];
    private final Handler noteHandler = new Handler(Looper.getMainLooper());

    private Calendar anchor = Calendar.getInstance(Locale.CHINA);
    private int granularity = 1; // 0 日, 1 周, 2 月
    private boolean gardenVisible = true;
    private boolean suppressNote;
    private String noteKey;

    private Runnable saveNote;

    public RecordsTab_26_10_26(MainActivity activity) {
        super(activity);
        this.activity = activity;
        setFillViewport(true);
        setBackgroundColor(Ui_26_10_25.PAPER);
        setClipToPadding(false);

        LinearLayout page = Ui_26_10_25.vertical(activity);
        page.setPadding(Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 20),
                Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 32));
        addView(page, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView recordsTitle = Ui_26_10_25.text(activity, "记录", 28,
                Ui_26_10_25.INK, true);
        recordsTitle.setTypeface(Ui_26_10_25.brandTypeface(activity));
        page.addView(recordsTitle);
        page.addView(Ui_26_10_25.hint(activity,
                "只记录本机看到的前台切换，不读内容，不上传。"));

        // 花园
        LinearLayout gardenCard = Ui_26_10_25.card(activity);
        gardenCard.setBackground(Ui_26_10_25.roundStroke(activity,
                0xFFFCFAF3, Ui_26_10_25.LINE, 24));
        LinearLayout gardenHead = Ui_26_10_25.horizontal(activity);
        gardenHead.setGravity(Gravity.CENTER_VERTICAL);
        TextView gardenTitle = Ui_26_10_25.text(activity, "糯糯的花园", 19,
                Ui_26_10_25.INK, true);
        gardenTitle.setTypeface(Ui_26_10_25.brandTypeface(activity));
        gardenHead.addView(gardenTitle, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        gardenCount = Ui_26_10_25.text(activity, "", 12, Ui_26_10_25.MUTED, false);
        gardenHead.addView(gardenCount);
        gardenToggle = Ui_26_10_25.action(activity, "收起", () -> {
            gardenVisible = !gardenVisible;
            garden.setVisibility(gardenVisible ? View.VISIBLE : View.GONE);
            gardenCount.setVisibility(gardenVisible ? View.VISIBLE : View.GONE);
            careLine.setVisibility(gardenVisible ? View.VISIBLE : View.GONE);
            gardenLegend.setVisibility(gardenVisible ? View.VISIBLE : View.GONE);
            gardenRecent.setVisibility(gardenVisible ? View.VISIBLE : View.GONE);
            gardenToggle.setText(gardenVisible ? "收起" : "展开");
        });
        gardenToggle.setPadding(Ui_26_10_25.dp(activity, 12), 0, 0, 0);
        gardenHead.addView(gardenToggle);
        gardenCard.addView(gardenHead);
        Ui_26_10_25.add(gardenCard, Ui_26_10_25.text(activity,
                "每次选择，都在这里留下一点痕迹。", 13,
                Ui_26_10_25.MUTED, false), 5);
        garden = new GardenView_26_10_26(activity);
        garden.setListener(this::showGardenMoment);
        LinearLayout.LayoutParams gp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(activity, 210));
        gp.topMargin = Ui_26_10_25.dp(activity, 14);
        gardenCard.addView(garden, gp);
        LinearLayout legendRow = Ui_26_10_25.horizontal(activity);
        legendRow.addView(legendPill("四瓣花", "主动选择", false, 0xFFE9EEE3),
                new LinearLayout.LayoutParams(0, Ui_26_10_25.dp(activity, 64), 1f));
        LinearLayout.LayoutParams budLp = new LinearLayout.LayoutParams(0,
                Ui_26_10_25.dp(activity, 64), 1f);
        budLp.leftMargin = Ui_26_10_25.dp(activity, 8);
        legendRow.addView(legendPill("五瓣花", "鼓励打开", true, 0xFFF4E9E0), budLp);
        gardenLegend = legendRow;
        Ui_26_10_25.add(gardenCard, gardenLegend, 12);
        gardenRecent = Ui_26_10_25.action(activity, "查看花园里的记录",
                this::showGardenList);
        gardenRecent.setMinHeight(Ui_26_10_25.dp(activity, 48));
        Ui_26_10_25.add(gardenCard, gardenRecent, 4);
        careLine = Ui_26_10_25.hint(activity, "");
        careLine.setPadding(0, Ui_26_10_25.dp(activity, 8), 0, 0);
        gardenCard.addView(careLine);
        Ui_26_10_25.add(page, gardenCard, 16);

        // 周期
        LinearLayout tabRow = Ui_26_10_25.horizontal(activity);
        tabRow.setPadding(0, 0, 0, Ui_26_10_25.dp(activity, 8));
        for (int i = 0; i < 3; i++) {
            final int index = i;
            TextView tab = Ui_26_10_25.text(activity, i == 0 ? "日" : i == 1 ? "周" : "月",
                    13, Ui_26_10_25.MUTED, false);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(0, Ui_26_10_25.dp(activity, 9), 0, Ui_26_10_25.dp(activity, 9));
            tab.setOnClickListener(v -> {
                granularity = index;
                refresh();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(Ui_26_10_25.dp(activity, 3), 0,
                    Ui_26_10_25.dp(activity, 3), 0);
            tabRow.addView(tab, lp);
            tabs[i] = tab;
        }
        page.addView(tabRow);

        LinearLayout rangeRow = Ui_26_10_25.horizontal(activity);
        rangeRow.setGravity(Gravity.CENTER_VERTICAL);
        rangeRow.addView(Ui_26_10_25.iconButton(activity, R.drawable.ic_chevron_left, 36,
                () -> shift(-1)));
        rangeLabel = Ui_26_10_25.text(activity, "", 14, Ui_26_10_25.INK, true);
        rangeLabel.setGravity(Gravity.CENTER);
        rangeRow.addView(rangeLabel, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        rangeRow.addView(Ui_26_10_25.action(activity, "今天", () -> {
            anchor = Calendar.getInstance(Locale.CHINA);
            refresh();
        }));
        rangeRow.addView(Ui_26_10_25.iconButton(activity, R.drawable.ic_chevron_right, 36,
                () -> shift(1)));
        Ui_26_10_25.add(page, rangeRow, 8);

        // 柱图
        LinearLayout barCard = Ui_26_10_25.card(activity);
        LinearLayout barHead = Ui_26_10_25.horizontal(activity);
        barHead.addView(Ui_26_10_25.text(activity, "主动停", 15,
                Ui_26_10_25.INK, true), new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        barTotal = Ui_26_10_25.text(activity, "", 12, Ui_26_10_25.MUTED, false);
        barHead.addView(barTotal);
        barCard.addView(barHead);
        barTitle = Ui_26_10_25.hint(activity, "");
        barCard.addView(barTitle);
        bars = new PeriodBarsView_26_10_26(activity);
        bars.setListener(this::onBar);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(activity, 150));
        bp.topMargin = Ui_26_10_25.dp(activity, 6);
        barCard.addView(bars, bp);
        Ui_26_10_25.add(page, barCard, 10);

        // 6 个数字
        LinearLayout statsCard = Ui_26_10_25.card(activity);
        statsCard.addView(groupLabel("受控应用"));
        LinearLayout controlledRow = statRow();
        stats[0] = statCell(controlledRow, "打开次数");
        stats[1] = statCell(controlledRow, "提醒次数");
        stats[2] = statCell(controlledRow, "使用时长");
        statsCard.addView(controlledRow);
        statsCard.addView(groupLabel("鼓励应用"));
        LinearLayout encourageRow = statRow();
        encourageRow.setPadding(0, Ui_26_10_25.dp(activity, 10), 0, 0);
        stats[3] = statCell(encourageRow, "打开次数");
        stats[4] = statCell(encourageRow, "鼓励提示");
        stats[5] = statCell(encourageRow, "使用时长");
        statsCard.addView(encourageRow);
        Ui_26_10_25.add(page, statsCard, 10);

        // 明细
        page.addView(Ui_26_10_25.section(activity, "用在了哪里"));
        detailsList = Ui_26_10_25.vertical(activity);
        Ui_26_10_25.add(page, detailsList, 6);

        // 感悟
        noteCard = Ui_26_10_25.card(activity);
        LinearLayout noteHead = Ui_26_10_25.horizontal(activity);
        noteHead.addView(Ui_26_10_25.text(activity, "一点感受", 15,
                Ui_26_10_25.INK, true), new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        noteLabel = Ui_26_10_25.text(activity, "", 12, Ui_26_10_25.MUTED, false);
        noteHead.addView(noteLabel);
        noteCard.addView(noteHead);
        noteInput = new EditText(activity);
        noteInput.setHint("今天想留下什么？可不填");
        noteInput.setHintTextColor(Ui_26_10_25.MUTED);
        noteInput.setTextColor(Ui_26_10_25.INK);
        noteInput.setTextSize(14);
        noteInput.setBackgroundColor(Color.TRANSPARENT);
        noteInput.setMinLines(2);
        noteInput.setGravity(Gravity.TOP);
        saveNote = () -> {
            if (suppressNote || noteKey == null) return;
            IntentDb_26_10_25.get(activity).setNote(noteKey, noteInput.getText().toString());
        };
        noteInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                noteHandler.removeCallbacks(saveNote);
                noteHandler.postDelayed(saveNote, 500);
            }
        });
        noteCard.addView(noteInput);
        Ui_26_10_25.add(page, noteCard, 14);

        refresh();
    }

    private LinearLayout legendPill(String name, String meaning, boolean encouraged,
                                    int background) {
        LinearLayout pill = Ui_26_10_25.vertical(activity);
        pill.setGravity(Gravity.CENTER);
        pill.setBackground(Ui_26_10_25.round(activity, background, 14));
        LinearLayout top = Ui_26_10_25.horizontal(activity);
        top.setGravity(Gravity.CENTER);
        top.addView(new FlowerMark_26_10_27(activity, encouraged),
                new LinearLayout.LayoutParams(Ui_26_10_25.dp(activity, 22),
                        Ui_26_10_25.dp(activity, 22)));
        TextView title = Ui_26_10_25.text(activity, name, 13, Ui_26_10_25.INK, true);
        title.setPadding(Ui_26_10_25.dp(activity, 5), 0, 0, 0);
        top.addView(title);
        pill.addView(top);
        TextView detail = Ui_26_10_25.text(activity, meaning, 12, Ui_26_10_25.MUTED, false);
        detail.setGravity(Gravity.CENTER);
        pill.addView(detail);
        return pill;
    }

    private void shift(int direction) {
        Calendar next = (Calendar) anchor.clone();
        if (granularity == 0) next.add(Calendar.DAY_OF_MONTH, direction);
        else if (granularity == 1) next.add(Calendar.WEEK_OF_YEAR, direction);
        else next.add(Calendar.MONTH, direction);
        anchor = next;
        refresh();
    }

    private long[] range() {
        Calendar start = (Calendar) anchor.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        if (granularity == 1) {
            int day = start.get(Calendar.DAY_OF_WEEK);
            int back = (day + 5) % 7; // 周一为 1
            start.add(Calendar.DAY_OF_MONTH, -back);
        } else if (granularity == 2) {
            start.set(Calendar.DAY_OF_MONTH, 1);
        }
        Calendar end = (Calendar) start.clone();
        if (granularity == 0) end.add(Calendar.DAY_OF_MONTH, 1);
        else if (granularity == 1) end.add(Calendar.DAY_OF_MONTH, 7);
        else end.add(Calendar.MONTH, 1);
        return new long[]{start.getTimeInMillis(), end.getTimeInMillis()};
    }

    @Override
    public void refresh() {
        if (activity == null) return;
        flushNote();
        for (int i = 0; i < tabs.length; i++) {
            boolean on = i == granularity;
            tabs[i].setTextColor(on ? Color.WHITE : Ui_26_10_25.MUTED);
            tabs[i].setTypeface(Typeface.create("sans-serif", on ? Typeface.BOLD : Typeface.NORMAL));
            tabs[i].setBackground(Ui_26_10_25.round(activity,
                    on ? Ui_26_10_25.GREEN : Ui_26_10_25.GREEN_SOFT, 10));
        }
        long[] range = range();
        long begin = range[0];
        long end = range[1];
        rangeLabel.setText(rangeLabel(begin, end));
        IntentDb_26_10_25 db = IntentDb_26_10_25.get(activity);
        IntentDb_26_10_25.DayStats controlled = db.dayStats(begin, end);
        IntentDb_26_10_25.EncourageStats encourage = db.encourageStats(begin, end);

        stats[0].setText(String.valueOf(controlled.opens));
        stats[1].setText(String.valueOf(controlled.reminders));
        stats[2].setText(StatsMath_26_10_25.duration(controlled.foregroundMs));
        stats[3].setText(String.valueOf(encourage.opens));
        stats[4].setText(String.valueOf(encourage.prompts));
        stats[5].setText(StatsMath_26_10_25.duration(encourage.ms));

        int stops = db.controlledActiveStops(begin, end);
        int totalLeaves = stops + encourage.opens;
        recentMoments = db.gardenMoments(begin, end, GardenView_26_10_26.MAX_VISIBLE);
        garden.setMoments(recentMoments, true);
        gardenCount.setText(periodWord() + " · 共 " + totalLeaves + " 次");
        gardenRecent.setText("查看最近 " + recentMoments.size() + " 次选择与打开");
        int careDays = db.careDayCount(begin, end);
        careLine.setText(periodWord() + "有 " + careDays + " 天主动停过/用过鼓励应用");
        barTotal.setText("共 " + stops + " 次");
        barTitle.setText(granularity == 0 ? "今天每小时" : granularity == 1 ? "本周每天" : "本月每天");
        fillBars(db, begin, end, stops);
        renderDetails(db, begin, end);
        renderNote(begin);
    }

    private String gardenMomentAction(IntentDb_26_10_25.GardenMoment moment) {
        if (moment.encouraged) return moment.temporary
                ? "这次改为鼓励使用" : "打开了一次鼓励应用";
        if ("declined".equals(moment.status)) return "这次选择先不打开";
        String purpose = moment.purpose == null ? "" : moment.purpose;
        return purpose.isEmpty() ? "这次选择打开应用" : "这次选择：" + purpose;
    }

    private void showGardenMoment(IntentDb_26_10_25.GardenMoment moment) {
        LinearLayout body = Ui_26_10_25.vertical(activity);
        String label = moment.label == null || moment.label.isEmpty()
                ? Apps_26_10_25.label(activity, moment.pkg) : moment.label;
        body.addView(Ui_26_10_25.hint(activity,
                StatsMath_26_10_25.monthDay(moment.wall) + " "
                        + StatsMath_26_10_25.clock(moment.wall) + " · " + label));
        Ui_26_10_25.add(body, Ui_26_10_25.text(activity,
                gardenMomentAction(moment), 18, Ui_26_10_25.INK, true), 16);
        if (moment.detail != null && !moment.detail.trim().isEmpty()) {
            Ui_26_10_25.add(body, Ui_26_10_25.text(activity,
                    "当时写下：" + moment.detail.trim(), 15,
                    Ui_26_10_25.MUTED, false), 10);
        }
        if (moment.foregroundMs > 0) {
            Ui_26_10_25.add(body, Ui_26_10_25.hint(activity,
                    "已记录前台时间 " + StatsMath_26_10_25.duration(moment.foregroundMs)), 14);
        }
        GentleSheet_26_10_26.show(activity,
                moment.encouraged ? "一朵五瓣花的来处" : "一朵四瓣花的来处",
                body, null, null);
    }

    private void showGardenList() {
        LinearLayout body = Ui_26_10_25.vertical(activity);
        if (recentMoments == null || recentMoments.isEmpty()) {
            body.addView(Ui_26_10_25.hint(activity, "这个时段还没有花园记录。"));
        } else {
            body.addView(Ui_26_10_25.hint(activity,
                    "展示最近 " + recentMoments.size() + " 次；点开可看对应的选择。"));
            android.app.Dialog[] sheet = new android.app.Dialog[1];
            for (IntentDb_26_10_25.GardenMoment moment : recentMoments) {
                LinearLayout row = Ui_26_10_25.card(activity);
                row.addView(Ui_26_10_25.text(activity,
                        (moment.encouraged ? "五瓣花 · " : "四瓣花 · ")
                                + gardenMomentAction(moment), 15, Ui_26_10_25.INK, true));
                Ui_26_10_25.add(row, Ui_26_10_25.hint(activity,
                        StatsMath_26_10_25.monthDay(moment.wall) + " "
                                + StatsMath_26_10_25.clock(moment.wall)), 6);
                row.setOnClickListener(v -> {
                    sheet[0].dismiss();
                    showGardenMoment(moment);
                });
                Ui_26_10_25.add(body, row, 8);
            }
            sheet[0] = GentleSheet_26_10_26.show(activity, "花园里的记录", body,
                    null, null);
            return;
        }
        GentleSheet_26_10_26.show(activity, "花园里的记录", body, null, null);
    }

    private void fillBars(IntentDb_26_10_25 db, long begin, long end, int stops) {
        if (granularity == 0) {
            int[] values = db.controlledStopsPerHour(begin, end);
            String[] labels = new String[24];
            for (int i = 0; i < 24; i++) labels[i] = (i % 6 == 0) ? String.valueOf(i) : "";
            bars.setData(values, labels, -1, true);
            return;
        }
        Map<String, Integer> map = new HashMap<>();
        for (IntentDb_26_10_25.DayCount day : db.controlledStopsPerDay(begin, end)) {
            map.put(day.dateKey, day.count);
        }
        Calendar cursor = Calendar.getInstance(Locale.CHINA);
        cursor.setTimeInMillis(begin);
        int days = (int) Math.round((end - begin) / 86400000.0);
        int[] values = new int[days];
        String[] labels = new String[days];
        int highlight = -1;
        String today = StatsMath_26_10_25.dateKey(System.currentTimeMillis());
        for (int i = 0; i < days; i++) {
            String key = StatsMath_26_10_25.dateKey(cursor.getTimeInMillis());
            Integer value = map.get(key);
            values[i] = value == null ? 0 : value;
            if (granularity == 1) {
                labels[i] = new String[]{"一", "二", "三", "四", "五", "六", "日"}
                        [cursor.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6
                                : cursor.get(Calendar.DAY_OF_WEEK) - 2];
            } else {
                int dom = cursor.get(Calendar.DAY_OF_MONTH);
                labels[i] = dom % 5 == 0 || dom == 1 ? String.valueOf(dom) : "";
            }
            if (key.equals(today)) highlight = i;
            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
        bars.setData(values, labels, highlight, true);
    }

    private void renderDetails(IntentDb_26_10_25 db, long begin, long end) {
        detailsList.removeAllViews();
        List<IntentDb_26_10_25.DayApp> apps = db.dayApps(begin, end);
        List<IntentDb_26_10_25.EncourageAppSummary> encouraged = db.encourageAppSummaries(begin, end);
        if (apps.isEmpty() && encouraged.isEmpty()) {
            detailsList.addView(Ui_26_10_25.hint(activity, "这段时间还没有记录。"));
            return;
        }
        if (!apps.isEmpty()) {
            detailsList.addView(Ui_26_10_25.text(activity, "受控应用", 13,
                    Ui_26_10_25.MUTED, true));
            int limit = Math.min(5, apps.size());
            for (int i = 0; i < limit; i++) {
                IntentDb_26_10_25.DayApp app = apps.get(i);
                detailRow(app.pkg, app.label,
                        "打开 " + app.opens + " 次 · 提醒 " + app.reminders + " 次 · "
                                + StatsMath_26_10_25.duration(app.foregroundMs));
            }
        }
        if (!encouraged.isEmpty()) {
            TextView title = Ui_26_10_25.text(activity, "鼓励应用", 13,
                    Ui_26_10_25.MUTED, true);
            title.setPadding(0, Ui_26_10_25.dp(activity, 12), 0, 0);
            detailsList.addView(title);
            int limit = Math.min(5, encouraged.size());
            for (int i = 0; i < limit; i++) {
                IntentDb_26_10_25.EncourageAppSummary app = encouraged.get(i);
                detailRow(app.pkg, app.label,
                        "打开 " + app.opens + " 次 · 鼓励 " + app.prompts + " 次 · "
                                + StatsMath_26_10_25.duration(app.foregroundMs)
                                + (app.temporaryOpens > 0
                                ? " · 本次切换 " + app.temporaryOpens + " 次" : ""));
            }
        }
    }

    private View detailRow(String pkg, String label, String subtitle) {
        LinearLayout row = Ui_26_10_25.horizontal(activity);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, Ui_26_10_25.dp(activity, 8), 0, Ui_26_10_25.dp(activity, 8));
        ImageView icon = new ImageView(activity);
        android.graphics.drawable.Drawable drawable = Apps_26_10_25.icon(activity, pkg);
        if (drawable != null) icon.setImageDrawable(drawable);
        else icon.setImageResource(R.drawable.ic_launcher_foreground);
        row.addView(icon, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(activity, 30), Ui_26_10_25.dp(activity, 30)));
        LinearLayout box = Ui_26_10_25.vertical(activity);
        box.setPadding(Ui_26_10_25.dp(activity, 10), 0, 0, 0);
        box.addView(Ui_26_10_25.text(activity, label == null || label.isEmpty() ? pkg : label,
                14, Ui_26_10_25.INK, true));
        box.addView(Ui_26_10_25.text(activity, subtitle, 12, Ui_26_10_25.MUTED, false));
        row.addView(box, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        Ui_26_10_25.add(detailsList, row, 0);
        return row;
    }

    private void renderNote(long begin) {
        if (granularity != 0) {
            noteCard.setVisibility(View.GONE);
            return;
        }
        noteCard.setVisibility(View.VISIBLE);
        noteKey = StatsMath_26_10_25.dateKey(begin);
        noteLabel.setText(new SimpleDateFormat("M月d日", Locale.CHINA).format(begin));
        suppressNote = true;
        noteInput.setText(IntentDb_26_10_25.get(activity).note(noteKey));
        suppressNote = false;
    }

    private void flushNote() {
        noteHandler.removeCallbacks(saveNote);
        if (!suppressNote && noteKey != null) saveNote.run();
    }

    private View groupLabel(String text) {
        TextView label = Ui_26_10_25.text(activity, text, 12, Ui_26_10_25.MUTED, true);
        label.setPadding(0, Ui_26_10_25.dp(activity, 4), 0, Ui_26_10_25.dp(activity, 4));
        return label;
    }

    private LinearLayout statRow() {
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private TextView statCell(LinearLayout row, String label) {
        LinearLayout box = Ui_26_10_25.vertical(activity);
        box.setPadding(0, Ui_26_10_25.dp(activity, 4), 0, Ui_26_10_25.dp(activity, 4));
        TextView value = Ui_26_10_25.text(activity, "0", 22, Ui_26_10_25.INK, true);
        box.addView(value);
        box.addView(Ui_26_10_25.text(activity, label, 11, Ui_26_10_25.MUTED, false));
        row.addView(box, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return value;
    }

    private String periodWord() {
        return granularity == 0 ? "今天" : granularity == 1 ? "本周" : "本月";
    }

    private String rangeLabel(long begin, long end) {
        SimpleDateFormat day = new SimpleDateFormat("M月d日", Locale.CHINA);
        SimpleDateFormat full = new SimpleDateFormat("yyyy年M月", Locale.CHINA);
        if (granularity == 0) {
            return new SimpleDateFormat("M月d日 · EEEE", Locale.CHINA).format(begin);
        }
        if (granularity == 1) {
            Calendar e = Calendar.getInstance(Locale.CHINA);
            e.setTimeInMillis(end - 1);
            return day.format(begin) + " – " + day.format(e.getTimeInMillis());
        }
        return full.format(begin);
    }

    private void onBar(int index) {
        if (granularity == 0) {
            int startHour = index;
            Ui_26_10_25.toast(activity, startHour + ":00–" + (startHour + 1) + ":00");
            return;
        }
        Calendar selected = Calendar.getInstance(Locale.CHINA);
        selected.setTimeInMillis(range()[0]);
        selected.add(Calendar.DAY_OF_MONTH, index);
        anchor = selected;
        granularity = 0;
        refresh();
    }
}
