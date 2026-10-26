package com.intentpause.app;

import android.app.TimePickerDialog;
import android.content.res.ColorStateList;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/** 每日总结的独立页面。通知携带日期，因此隔天点开仍显示对应那一天。 */
public final class SummaryActivity_26_10_26 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui_26_10_25.applyWindow(this);
        getOnBackPressedDispatcher().addCallback(this,
                new androidx.activity.OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        closeSummary();
                    }
                });
        String date = getIntent().getStringExtra(DailySummary_26_10_26.EXTRA_DATE);
        if (date == null) date = StatsMath_26_10_25.dateKey(System.currentTimeMillis());
        DailySummary_26_10_26.Snapshot data = DailySummary_26_10_26.snapshot(this, date);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Ui_26_10_25.PAPER);
        LinearLayout page = Ui_26_10_25.vertical(this);
        page.setPadding(Ui_26_10_25.dp(this, 22), Ui_26_10_25.dp(this, 18),
                Ui_26_10_25.dp(this, 22), Ui_26_10_25.dp(this, 32));
        scroll.addView(page);
        setContentView(scroll);
        Ui_26_10_25.applyInsets(scroll);

        LinearLayout header = Ui_26_10_25.horizontal(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(Ui_26_10_25.iconButton(this, R.drawable.ic_back, 48,
                this::closeSummary));
        TextView title = Ui_26_10_25.text(this, "今日总结", 23, Ui_26_10_25.INK, true);
        header.addView(title);
        page.addView(header);
        int chosenMinute = DailySummary_26_10_26.minutes(this);
        java.util.Calendar clock = java.util.Calendar.getInstance();
        int nowMinute = clock.get(java.util.Calendar.HOUR_OF_DAY) * 60
                + clock.get(java.util.Calendar.MINUTE);
        boolean beforeCutoff = data.dateKey.equals(
                StatsMath_26_10_25.dateKey(System.currentTimeMillis()))
                && chosenMinute >= 0 && nowMinute < chosenMinute;
        String period = beforeCutoff ? "截至现在" : "到 "
                + DailySummary_26_10_26.timeText(this);
        TextView dateLine = Ui_26_10_25.text(this, data.dateKey + " · "
                + period + " 的记录", 13, Ui_26_10_25.MUTED, false);
        dateLine.setPadding(0, Ui_26_10_25.dp(this, 12), 0, Ui_26_10_25.dp(this, 20));
        page.addView(dateLine);

        LinearLayout greeting = Ui_26_10_25.card(this);
        LinearLayout greetingRow = Ui_26_10_25.horizontal(this);
        greetingRow.setGravity(Gravity.CENTER_VERTICAL);
        ImageView mascot = new ImageView(this);
        mascot.setImageResource(R.drawable.mascot_nuonuo_cutout);
        greetingRow.addView(mascot, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(this, 54), Ui_26_10_25.dp(this, 54)));
        TextView line = Ui_26_10_25.text(this, data.gentle, 17, Ui_26_10_25.INK, true);
        line.setPadding(Ui_26_10_25.dp(this, 12), 0, 0, 0);
        greetingRow.addView(line, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        greeting.addView(greetingRow);
        Ui_26_10_25.add(page, greeting, 0);

        LinearLayout times = Ui_26_10_25.card(this);
        times.addView(Ui_26_10_25.text(this, "时间用在了哪里", 18, Ui_26_10_25.INK, true));
        long max = Math.max(60000L, Math.max(data.controlled.foregroundMs, data.encouraged.ms));
        timeBar(times, "提醒我想一想", data.controlled.foregroundMs, max, Ui_26_10_25.GREEN);
        timeBar(times, "给我一点鼓励", data.encouraged.ms, max, Ui_26_10_25.SAGE);
        TextView foot = Ui_26_10_25.hint(this,
                "仅统计一念间观察到的亮屏前台时间；切走或锁屏时暂停。");
        Ui_26_10_25.add(times, foot, 18);
        Ui_26_10_25.add(page, times, 14);

        LinearLayout counts = Ui_26_10_25.card(this);
        counts.addView(Ui_26_10_25.text(this, "今天的选择", 18, Ui_26_10_25.INK, true));
        countRow(counts, "受控应用", "打开 " + data.controlled.opens + " 次",
                "复查 " + data.controlled.reminders + " 次");
        countRow(counts, "鼓励应用", "打开 " + data.encouraged.opens + " 次",
                "鼓励 " + data.encouraged.prompts + " 次");
        countRow(counts, "主动停下", data.stops + " 次",
                "先不打开 " + data.declined + " 次");
        if (data.compare != null) Ui_26_10_25.add(counts,
                Ui_26_10_25.hint(this, data.compare), 14);
        Ui_26_10_25.add(page, counts, 14);

        TextView changeTime = Ui_26_10_25.action(this, "调整每日总结时间", this::pickTime);
        changeTime.setGravity(Gravity.CENTER);
        changeTime.setMinHeight(Ui_26_10_25.dp(this, 48));
        Ui_26_10_25.add(page, changeTime, 12);
    }

    private void closeSummary() {
        if (isTaskRoot()) startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void timeBar(LinearLayout parent, String title, long ms, long max, int color) {
        LinearLayout row = Ui_26_10_25.horizontal(this);
        TextView name = Ui_26_10_25.text(this, title, 14, Ui_26_10_25.INK, true);
        row.addView(name, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(Ui_26_10_25.text(this, StatsMath_26_10_25.duration(ms), 14,
                Ui_26_10_25.INK, false));
        Ui_26_10_25.add(parent, row, 18);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(1000);
        bar.setProgress((int) Math.round(1000.0 * ms / max));
        bar.setProgressTintList(ColorStateList.valueOf(color));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(Ui_26_10_25.GREEN_SOFT));
        parent.addView(bar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(this, 12)));
    }

    private void countRow(LinearLayout parent, String name, String a, String b) {
        LinearLayout row = Ui_26_10_25.horizontal(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading = Ui_26_10_25.text(this, name, 14, Ui_26_10_25.MUTED, false);
        row.addView(heading, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout values = Ui_26_10_25.vertical(this);
        values.setGravity(Gravity.END);
        values.addView(Ui_26_10_25.text(this, a, 14, Ui_26_10_25.INK, true));
        values.addView(Ui_26_10_25.text(this, b, 12, Ui_26_10_25.MUTED, false));
        row.addView(values);
        Ui_26_10_25.add(parent, row, 14);
    }

    private void pickTime() {
        int minutes = DailySummary_26_10_26.minutes(this);
        int hour = minutes < 0 ? 21 : minutes / 60;
        int minute = minutes < 0 ? 30 : minutes % 60;
        new TimePickerDialog(this, (view, h, m) -> {
            DailySummary_26_10_26.setMinutes(this, h * 60 + m);
            Ui_26_10_25.toast(this, String.format(Locale.CHINA,
                    "已改为每天 %02d:%02d", h, m));
        }, hour, minute, true).show();
    }
}
