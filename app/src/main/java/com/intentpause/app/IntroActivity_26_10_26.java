package com.intentpause.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** 首次启动介绍页：只说清楚“是什么、在哪用、数据在哪”。 */
public class IntroActivity_26_10_26 extends AppCompatActivity {
    private static final String PREFS = "intro";
    private static final String DONE = "done";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (prefs.getBoolean(DONE, false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        Ui_26_10_25.applyWindow(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Ui_26_10_25.PAPER);
        scroll.setClipToPadding(false);
        LinearLayout page = Ui_26_10_25.vertical(this);
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setPadding(Ui_26_10_25.dp(this, 28), Ui_26_10_25.dp(this, 36),
                Ui_26_10_25.dp(this, 28), Ui_26_10_25.dp(this, 32));
        scroll.addView(page);
        setContentView(scroll);
        Ui_26_10_25.applyInsets(scroll);

        ImageView mascot = new NuoNuoPoseView_26_10_27(this);
        NuoNuoGesture_26_10_26.attach(mascot);
        page.addView(mascot, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(this, 168), Ui_26_10_25.dp(this, 168)));
        Ui_26_10_25.breathe(mascot);

        TextView title = Ui_26_10_25.text(this, "一念间", 32, Ui_26_10_25.INK, true);
        title.setTypeface(Ui_26_10_25.brandTypeface(this));
        title.setGravity(Gravity.CENTER);
        page.addView(title);
        TextView slogan = Ui_26_10_25.text(this, "这一念，听自己。", 17,
                Ui_26_10_25.MUTED, false);
        slogan.setGravity(Gravity.CENTER);
        slogan.setPadding(0, Ui_26_10_25.dp(this, 10), 0, Ui_26_10_25.dp(this, 24));
        page.addView(slogan);

        LinearLayout choices = Ui_26_10_25.horizontal(this);
        addPoint(choices, "想一想", "打开前，问自己这次想做什么。",
                0xFFE7F0E5, 0);
        addPoint(choices, "给点鼓励", "想多用的应用，也可以留一句话。",
                0xFFF5E4D8, 10);
        page.addView(choices);
        LinearLayout detail = Ui_26_10_25.card(this);
        detail.addView(Ui_26_10_25.text(this, "按自己的节奏", 16,
                Ui_26_10_25.INK, true));
        detail.addView(Ui_26_10_25.text(this,
                "想放松就放松，想学习就学习；到约定时间再看一次。", 14,
                Ui_26_10_25.MUTED, false));
        Ui_26_10_25.add(page, detail, 12);

        TextView mascotLine = Ui_26_10_25.text(this, "糯糯在，按你的节奏来。", 13,
                Ui_26_10_25.MUTED, false);
        mascotLine.setGravity(Gravity.CENTER);
        mascotLine.setPadding(0, Ui_26_10_25.dp(this, 22), 0, Ui_26_10_25.dp(this, 10));
        page.addView(mascotLine);

        Button start = Ui_26_10_25.button(this, "开始使用", true);
        start.setOnClickListener(v -> {
            prefs.edit().putBoolean(DONE, true).apply();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(this, 54));
        lp.topMargin = Ui_26_10_25.dp(this, 6);
        page.addView(start, lp);

        TextView hint = Ui_26_10_25.hint(this,
                "只在本机记录，不联网，不读屏幕内容。下一步会引导开启无障碍服务。");
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, Ui_26_10_25.dp(this, 14), 0, 0);
        page.addView(hint);
    }

    private void addPoint(LinearLayout card, String title, String body,
                          int color, int leftMargin) {
        LinearLayout row = Ui_26_10_25.vertical(this);
        row.setPadding(Ui_26_10_25.dp(this, 14), Ui_26_10_25.dp(this, 15),
                Ui_26_10_25.dp(this, 14), Ui_26_10_25.dp(this, 15));
        row.setBackground(Ui_26_10_25.round(this, color, 20));
        row.addView(Ui_26_10_25.text(this, title, 16, Ui_26_10_25.INK, true));
        TextView description = Ui_26_10_25.text(this, body, 13,
                Ui_26_10_25.MUTED, false);
        description.setPadding(0, Ui_26_10_25.dp(this, 8), 0, 0);
        row.addView(description);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.leftMargin = Ui_26_10_25.dp(this, leftMargin);
        card.addView(row, lp);
    }
}
