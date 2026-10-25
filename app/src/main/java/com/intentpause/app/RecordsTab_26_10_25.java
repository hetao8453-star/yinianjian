package com.intentpause.app;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 记录页：月历热力图、当日明细、每日感悟与趋势。 */
public class RecordsTab_26_10_25 extends ScrollView implements Refreshable_26_10_25 {
    private final MainActivity activity;
    private final TextView monthLabel;
    private final HeatmapView_26_10_25 heatmap;
    private final TextView dayLabel;
    private final TextView opensValue;
    private final TextView remindersValue;
    private final TextView leavesValue;
    private final TextView timeValue;
    private final TextView savedLine;
    private final LinearLayout dayAppsList;
    private final EditText noteInput;
    private final TextView noteDate;
    private final WeekBarsView_26_10_25 weekBars;
    private final RingChart_26_10_25 ringChart;
    private final LinearLayout ringLegend;
    private final Handler noteHandler = new Handler(Looper.getMainLooper());
    private boolean suppressNote;
    private String noteKey;

    private int shownYear;
    private int shownMonth;
    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;

    private final Runnable saveNote = new Runnable() {
        @Override
        public void run() {
            if (suppressNote || noteKey == null) return;
            IntentDb_26_10_25.get(activity).setNote(noteKey, noteInput.getText().toString());
        }
    };

    public RecordsTab_26_10_25(MainActivity activity) {
        super(activity);
        this.activity = activity;
        setFillViewport(true);
        setBackgroundColor(Ui_26_10_25.PAPER);
        setClipToPadding(false);

        Calendar now = Calendar.getInstance(Locale.CHINA);
        shownYear = selectedYear = now.get(Calendar.YEAR);
        shownMonth = selectedMonth = now.get(Calendar.MONTH);
        selectedDay = now.get(Calendar.DAY_OF_MONTH);

        LinearLayout page = Ui_26_10_25.vertical(activity);
        page.setPadding(Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 20),
                Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 32));
        addView(page, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        page.addView(Ui_26_10_25.text(activity, "记录", 28, Ui_26_10_25.INK, true));
        page.addView(Ui_26_10_25.hint(activity,
                "只记录本机看到的前台切换；不读聊天内容，不上传。"));

        // 月历
        LinearLayout calendarCard = Ui_26_10_25.card(activity);
        LinearLayout monthRow = Ui_26_10_25.horizontal(activity);
        ImageButton prev = Ui_26_10_25.iconButton(activity, R.drawable.ic_chevron_left, 36,
                () -> shiftMonth(-1));
        monthRow.addView(prev);
        monthLabel = Ui_26_10_25.text(activity, "", 16, Ui_26_10_25.INK, true);
        monthLabel.setGravity(Gravity.CENTER);
        monthRow.addView(monthLabel, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView todayAction = Ui_26_10_25.action(activity, "今天", this::jumpToToday);
        monthRow.addView(todayAction);
        monthRow.addView(Ui_26_10_25.iconButton(activity, R.drawable.ic_chevron_right, 36,
                () -> shiftMonth(1)));
        calendarCard.addView(monthRow);

        heatmap = new HeatmapView_26_10_25(activity);
        heatmap.setListener((year, month, day) -> {
            flushNote();
            selectedYear = year;
            selectedMonth = month;
            selectedDay = day;
            refresh();
        });
        calendarCard.addView(heatmap);

        LinearLayout legend = Ui_26_10_25.horizontal(activity);
        legend.setGravity(Gravity.END);
        legend.addView(Ui_26_10_25.text(activity, "少", 11, Ui_26_10_25.MUTED, false));
        int[] legendColors = {0xFFEFEEE8, 0xFFDDE9E0, 0xFFC0D6C8, 0xFF93B6A1, 0xFF628E76};
        for (int color : legendColors) {
            View square = new View(activity);
            square.setBackground(Ui_26_10_25.round(activity, color, 4));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    Ui_26_10_25.dp(activity, 12), Ui_26_10_25.dp(activity, 12));
            lp.setMargins(Ui_26_10_25.dp(activity, 3), 0, Ui_26_10_25.dp(activity, 3), 0);
            legend.addView(square, lp);
        }
        legend.addView(Ui_26_10_25.text(activity, "多", 11, Ui_26_10_25.MUTED, false));
        calendarCard.addView(legend);
        Ui_26_10_25.add(page, calendarCard, 16);

        // 选中日统计
        dayLabel = Ui_26_10_25.text(activity, "", 15, Ui_26_10_25.INK, true);
        Ui_26_10_25.add(page, dayLabel, 22);

        LinearLayout statsCard = Ui_26_10_25.card(activity);
        LinearLayout rowA = new LinearLayout(activity);
        rowA.setOrientation(LinearLayout.HORIZONTAL);
        opensValue = statCell(rowA, "打开次数");
        remindersValue = statCell(rowA, "提醒次数");
        statsCard.addView(rowA);
        LinearLayout rowB = new LinearLayout(activity);
        rowB.setOrientation(LinearLayout.HORIZONTAL);
        rowB.setPadding(0, Ui_26_10_25.dp(activity, 14), 0, 0);
        leavesValue = statCell(rowB, "主动离开");
        timeValue = statCell(rowB, "在应用内");
        statsCard.addView(rowB);
        savedLine = Ui_26_10_25.hint(activity, "");
        savedLine.setPadding(0, Ui_26_10_25.dp(activity, 12), 0, 0);
        statsCard.addView(savedLine);
        TextView explain = Ui_26_10_25.hint(activity,
                "打开=进入次数；提醒=复查出现；在应用内=实际前台时长（不含确认页）。");
        explain.setPadding(0, Ui_26_10_25.dp(activity, 8), 0, 0);
        statsCard.addView(explain);
        Ui_26_10_25.add(page, statsCard, 10);

        // 当日分应用
        page.addView(Ui_26_10_25.section(activity, "这一天用在了哪里"));
        dayAppsList = Ui_26_10_25.vertical(activity);
        Ui_26_10_25.add(page, dayAppsList, 6);

        // 每日感悟
        LinearLayout noteCard = Ui_26_10_25.card(activity);
        LinearLayout noteHead = Ui_26_10_25.horizontal(activity);
        noteHead.addView(Ui_26_10_25.text(activity, "一点感受", 15, Ui_26_10_25.INK, true),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        noteDate = Ui_26_10_25.text(activity, "", 12, Ui_26_10_25.MUTED, false);
        noteHead.addView(noteDate);
        noteCard.addView(noteHead);
        noteInput = new EditText(activity);
        noteInput.setTextSize(15);
        noteInput.setTextColor(Ui_26_10_25.INK);
        noteInput.setHintTextColor(Ui_26_10_25.MUTED);
        noteInput.setHint("写点什么，只存在本机。");
        noteInput.setBackgroundColor(Color.TRANSPARENT);
        noteInput.setPadding(0, Ui_26_10_25.dp(activity, 8), 0, 0);
        noteInput.setMinLines(2);
        noteInput.setGravity(Gravity.TOP | Gravity.START);
        noteInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (suppressNote) return;
                noteHandler.removeCallbacks(saveNote);
                noteHandler.postDelayed(saveNote, 600);
            }
        });
        noteCard.addView(noteInput);
        Ui_26_10_25.add(page, noteCard, 14);

        // 一周节奏
        LinearLayout weekCard = Ui_26_10_25.card(activity);
        weekCard.addView(Ui_26_10_25.text(activity, "一周节奏", 15, Ui_26_10_25.INK, true));
        weekCard.addView(Ui_26_10_25.hint(activity, "本周每天打开受控应用的次数。"));
        weekBars = new WeekBarsView_26_10_25(activity);
        weekCard.addView(weekBars);

        // 时间花在哪
        LinearLayout ringCard = Ui_26_10_25.card(activity);
        ringCard.addView(Ui_26_10_25.text(activity, "时间花在哪", 15, Ui_26_10_25.INK, true));
        ringCard.addView(Ui_26_10_25.hint(activity, "近 30 天，受控应用内的观察时长。"));
        ringChart = new RingChart_26_10_25(activity);
        ringCard.addView(ringChart);
        ringLegend = Ui_26_10_25.vertical(activity);
        ringCard.addView(ringLegend);

        LinearLayout trendsBox = Ui_26_10_25.vertical(activity);
        Ui_26_10_25.add(trendsBox, weekCard, 0);
        Ui_26_10_25.add(trendsBox, ringCard, 12);
        trendsBox.setVisibility(android.view.View.GONE);
        LinearLayout trendsHead = Ui_26_10_25.horizontal(activity);
        trendsHead.setPadding(0, Ui_26_10_25.dp(activity, 22), 0, 0);
        trendsHead.addView(Ui_26_10_25.section(activity, "趋势"),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        TextView trendsToggle = Ui_26_10_25.action(activity, "展开", () -> {
        });
        trendsToggle.setOnClickListener(v -> {
            boolean show = trendsBox.getVisibility() != LinearLayout.VISIBLE;
            trendsBox.setVisibility(show ? LinearLayout.VISIBLE : LinearLayout.GONE);
            trendsToggle.setText(show ? "收起" : "展开");
        });
        trendsHead.addView(trendsToggle);
        page.addView(trendsHead);
        page.addView(trendsBox, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView more = Ui_26_10_25.action(activity, "完整记录在「我的 → 全部记录」",
                () -> Ui_26_10_25.toast(activity, "打开底部「我的」，点「全部记录」查看每一次会话"));
        more.setGravity(Gravity.CENTER);
        Ui_26_10_25.add(page, more, 22);

        refresh();
    }

    private TextView statCell(LinearLayout row, String label) {
        LinearLayout cell = Ui_26_10_25.vertical(activity);
        cell.setPadding(0, Ui_26_10_25.dp(activity, 6), Ui_26_10_25.dp(activity, 12), 0);
        TextView value = Ui_26_10_25.text(activity, "0", 22, Ui_26_10_25.INK, true);
        cell.addView(value);
        cell.addView(Ui_26_10_25.text(activity, label, 12, Ui_26_10_25.MUTED, false));
        row.addView(cell, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        return value;
    }

    private void shiftMonth(int delta) {
        flushNote();
        Calendar cal = Calendar.getInstance(Locale.CHINA);
        cal.set(shownYear, shownMonth, 1);
        cal.add(Calendar.MONTH, delta);
        shownYear = cal.get(Calendar.YEAR);
        shownMonth = cal.get(Calendar.MONTH);
        Calendar today = Calendar.getInstance(Locale.CHINA);
        if (shownYear == today.get(Calendar.YEAR) && shownMonth == today.get(Calendar.MONTH)) {
            selectedYear = shownYear;
            selectedMonth = shownMonth;
            selectedDay = today.get(Calendar.DAY_OF_MONTH);
        } else {
            selectedYear = shownYear;
            selectedMonth = shownMonth;
            selectedDay = 1;
        }
        refresh();
    }

    private void jumpToToday() {
        flushNote();
        Calendar now = Calendar.getInstance(Locale.CHINA);
        shownYear = selectedYear = now.get(Calendar.YEAR);
        shownMonth = selectedMonth = now.get(Calendar.MONTH);
        selectedDay = now.get(Calendar.DAY_OF_MONTH);
        refresh();
    }

    private void flushNote() {
        noteHandler.removeCallbacks(saveNote);
        if (!suppressNote && noteKey != null) {
            IntentDb_26_10_25.get(activity).setNote(noteKey, noteInput.getText().toString());
        }
        noteInput.clearFocus();
    }

    @Override
    public void refresh() {
        if (activity == null) return;
        IntentDb_26_10_25 db = IntentDb_26_10_25.get(activity);
        Calendar monthStart = Calendar.getInstance(Locale.CHINA);
        monthStart.set(shownYear, shownMonth, 1, 0, 0, 0);
        monthStart.set(Calendar.MILLISECOND, 0);
        Calendar nextMonth = (Calendar) monthStart.clone();
        nextMonth.add(Calendar.MONTH, 1);

        Map<String, Integer> counts = new HashMap<>();
        for (IntentDb_26_10_25.DayCount dayCount : db.opensPerDay(
                monthStart.getTimeInMillis(), nextMonth.getTimeInMillis())) {
            counts.put(dayCount.dateKey, dayCount.count);
        }
        monthLabel.setText(String.format(Locale.CHINA, "%d年%d月",
                shownYear, shownMonth + 1));
        heatmap.setData(shownYear, shownMonth, counts, selectedDay);

        Calendar dayStart = Calendar.getInstance(Locale.CHINA);
        dayStart.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
        dayStart.set(Calendar.MILLISECOND, 0);
        Calendar dayEnd = (Calendar) dayStart.clone();
        dayEnd.add(Calendar.DAY_OF_YEAR, 1);
        long begin = dayStart.getTimeInMillis();
        long end = dayEnd.getTimeInMillis();

        Calendar today = Calendar.getInstance(Locale.CHINA);
        boolean selectedToday = today.get(Calendar.YEAR) == selectedYear
                && today.get(Calendar.MONTH) == selectedMonth
                && today.get(Calendar.DAY_OF_MONTH) == selectedDay;
        dayLabel.setText(StatsMath_26_10_25.monthDay(begin) + " · "
                + StatsMath_26_10_25.weekday(begin) + (selectedToday ? "（今天）" : ""));

        IntentDb_26_10_25.DayStats stats = db.dayStats(begin, end);
        opensValue.setText(String.valueOf(stats.opens));
        remindersValue.setText(String.valueOf(stats.reminders));
        leavesValue.setText(String.valueOf(stats.leaves));
        timeValue.setText(StatsMath_26_10_25.duration(stats.foregroundMs));
        if (stats.savedEstMs > 0) {
            savedLine.setText("复查页离开的估算：少用约 "
                    + StatsMath_26_10_25.duration(stats.savedEstMs));
        } else {
            savedLine.setText("只在复查页选“现在离开”时估算少用时间。");
        }

        renderDayApps(db.dayApps(begin, end));

        noteKey = StatsMath_26_10_25.dateKey(begin);
        noteDate.setText(StatsMath_26_10_25.monthDay(begin));
        String note = db.note(noteKey);
        if (!noteInput.hasFocus() && !note.contentEquals(noteInput.getText())) {
            suppressNote = true;
            noteInput.setText(note);
            noteInput.setSelection(noteInput.getText().length());
            suppressNote = false;
        }

        renderWeek(db);
        renderRing(db);
    }

    private void renderDayApps(List<IntentDb_26_10_25.DayApp> apps) {
        dayAppsList.removeAllViews();
        if (apps.isEmpty()) {
            dayAppsList.addView(Ui_26_10_25.hint(activity, "这一天没有打开受控应用的记录。"));
            return;
        }
        int maxOpens = 1;
        for (IntentDb_26_10_25.DayApp app : apps) maxOpens = Math.max(maxOpens, app.opens);
        for (IntentDb_26_10_25.DayApp app : apps) {
            LinearLayout row = Ui_26_10_25.horizontal(activity);
            row.setBackground(Ui_26_10_25.roundStroke(activity, Color.WHITE, Ui_26_10_25.LINE, 14));
            row.setPadding(Ui_26_10_25.dp(activity, 12), Ui_26_10_25.dp(activity, 10),
                    Ui_26_10_25.dp(activity, 12), Ui_26_10_25.dp(activity, 10));
            ImageView icon = new ImageView(activity);
            android.graphics.drawable.Drawable drawable = Apps_26_10_25.icon(activity, app.pkg);
            if (drawable != null) icon.setImageDrawable(drawable);
            else icon.setImageResource(R.drawable.ic_launcher_foreground);
            row.addView(icon, new LinearLayout.LayoutParams(
                    Ui_26_10_25.dp(activity, 30), Ui_26_10_25.dp(activity, 30)));
            LinearLayout box = Ui_26_10_25.vertical(activity);
            box.setPadding(Ui_26_10_25.dp(activity, 10), 0, Ui_26_10_25.dp(activity, 10), 0);
            String label = app.label == null || app.label.isEmpty()
                    ? Apps_26_10_25.label(activity, app.pkg) : app.label;
            box.addView(Ui_26_10_25.text(activity, label, 14, Ui_26_10_25.INK, true));
            box.addView(Ui_26_10_25.text(activity,
                    "打开 " + app.opens + " 次" + (app.reminders > 0 ? " · 提醒 " + app.reminders + " 次" : "")
                            + " · " + StatsMath_26_10_25.duration(app.foregroundMs),
                    12, Ui_26_10_25.MUTED, false));
            row.addView(box, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.setBackground(Ui_26_10_25.rippleStroke(activity, Color.WHITE, Ui_26_10_25.LINE, 14));
            Ui_26_10_25.add(dayAppsList, row, 8);
        }
    }

    private void renderWeek(IntentDb_26_10_25 db) {
        Calendar now = Calendar.getInstance(Locale.CHINA);
        int dow = (now.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        Calendar monday = Calendar.getInstance(Locale.CHINA);
        monday.setTimeInMillis(StatsMath_26_10_25.addDays(now.getTimeInMillis(), -dow));
        monday = startOfDay(monday);
        Calendar nextMonday = (Calendar) monday.clone();
        nextMonday.add(Calendar.DAY_OF_YEAR, 7);
        Map<String, Integer> counts = new HashMap<>();
        for (IntentDb_26_10_25.DayCount dayCount : db.opensPerDay(
                monday.getTimeInMillis(), nextMonday.getTimeInMillis())) {
            counts.put(dayCount.dateKey, dayCount.count);
        }
        int[] values = new int[7];
        for (int i = 0; i < 7; i++) {
            String key = StatsMath_26_10_25.dateKey(
                    StatsMath_26_10_25.addDays(monday.getTimeInMillis(), i));
            values[i] = counts.getOrDefault(key, 0);
        }
        weekBars.setData(values, dow);
    }

    private void renderRing(IntentDb_26_10_25 db) {
        long now = System.currentTimeMillis();
        List<IntentDb_26_10_25.AppTime> apps = db.appTimes(
                StatsMath_26_10_25.addDays(now, -30), now + 60_000L);
        long total = 0;
        for (IntentDb_26_10_25.AppTime app : apps) total += app.ms;
        List<RingChart_26_10_25.Slice> slices = new ArrayList<>();
        ringLegend.removeAllViews();
        long others = 0;
        for (int i = 0; i < apps.size(); i++) {
            IntentDb_26_10_25.AppTime app = apps.get(i);
            if (i < 4) {
                slices.add(new RingChart_26_10_25.Slice(app.label, app.ms,
                        RingChart_26_10_25.paletteAt(i)));
            } else {
                others += app.ms;
            }
        }
        if (others > 0) {
            slices.add(new RingChart_26_10_25.Slice("其他", others,
                    RingChart_26_10_25.paletteAt(4)));
        }
        ringChart.setData(slices);
        if (total <= 0) {
            ringLegend.addView(Ui_26_10_25.hint(activity,
                    "近 30 天还没有观察到受控应用内的使用时长。"));
            return;
        }
        for (RingChart_26_10_25.Slice slice : slices) {
            LinearLayout row = Ui_26_10_25.horizontal(activity);
            row.setPadding(0, Ui_26_10_25.dp(activity, 6), 0, 0);
            View dot = new View(activity);
            dot.setBackground(Ui_26_10_25.round(activity, slice.color, 999));
            row.addView(dot, new LinearLayout.LayoutParams(
                    Ui_26_10_25.dp(activity, 9), Ui_26_10_25.dp(activity, 9)));
            TextView name = Ui_26_10_25.text(activity, slice.label, 13, Ui_26_10_25.INK, false);
            name.setPadding(Ui_26_10_25.dp(activity, 8), 0, 0, 0);
            row.addView(name, new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(Ui_26_10_25.text(activity,
                    StatsMath_26_10_25.duration(slice.ms) + " · "
                            + StatsMath_26_10_25.percent(slice.ms, total) + "%",
                    12, Ui_26_10_25.MUTED, false));
            ringLegend.addView(row);
        }
    }

    private Calendar startOfDay(Calendar cal) {
        Calendar copy = (Calendar) cal.clone();
        copy.set(Calendar.HOUR_OF_DAY, 0);
        copy.set(Calendar.MINUTE, 0);
        copy.set(Calendar.SECOND, 0);
        copy.set(Calendar.MILLISECOND, 0);
        return copy;
    }

}
