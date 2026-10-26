package com.intentpause.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** 全部记录：按应用汇总，再查看每一次会话的选择。 */
public class AppRecordActivity_26_10_25 extends AppCompatActivity {
    private LinearLayout content;
    private TextView title;
    private boolean inSessions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui_26_10_25.applyWindow(this);

        LinearLayout root = Ui_26_10_25.vertical(this);
        root.setBackgroundColor(Ui_26_10_25.PAPER);

        LinearLayout header = Ui_26_10_25.horizontal(this);
        header.setPadding(Ui_26_10_25.dp(this, 12), Ui_26_10_25.dp(this, 8),
                Ui_26_10_25.dp(this, 16), Ui_26_10_25.dp(this, 6));
        header.addView(Ui_26_10_25.iconButton(this, R.drawable.ic_back, 44, this::goBack));
        title = Ui_26_10_25.text(this, "全部记录", 19, Ui_26_10_25.INK, true);
        header.addView(title, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        content = Ui_26_10_25.vertical(this);
        content.setPadding(Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 4),
                Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 32));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
        Ui_26_10_25.applyInsets(root);

        showApps();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (inSessions) {
                    showApps();
                } else {
                    finish();
                }
            }
        });
    }

    private void goBack() {
        if (inSessions) showApps();
        else finish();
    }

    private void showApps() {
        inSessions = false;
        title.setText("全部记录");
        content.removeAllViews();
        IntentDb_26_10_25 db = IntentDb_26_10_25.get(this);
        List<IntentDb_26_10_25.AppSummary> apps = db.appSummaries();
        if (apps.isEmpty()) {
            LinearLayout empty = Ui_26_10_25.card(this);
            empty.addView(Ui_26_10_25.text(this, "还没有会话记录", 16, Ui_26_10_25.INK, true));
            empty.addView(Ui_26_10_25.hint(this,
                    "打开一个受控应用并完成选择后，这里会按应用列出每一次会话。"));
            Ui_26_10_25.add(content, empty, 10);
            return;
        }
        for (IntentDb_26_10_25.AppSummary app : apps) {
            content.addView(appRow(app), rowParams());
        }
    }

    private LinearLayout.LayoutParams rowParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui_26_10_25.dp(this, 10);
        return lp;
    }

    private View appRow(IntentDb_26_10_25.AppSummary app) {
        LinearLayout row = Ui_26_10_25.horizontal(this);
        row.setBackground(Ui_26_10_25.rippleStroke(this, Color.WHITE, Ui_26_10_25.LINE, 16));
        row.setPadding(Ui_26_10_25.dp(this, 14), Ui_26_10_25.dp(this, 14),
                Ui_26_10_25.dp(this, 12), Ui_26_10_25.dp(this, 14));
        ImageView icon = new ImageView(this);
        android.graphics.drawable.Drawable drawable = Apps_26_10_25.icon(this, app.pkg);
        if (drawable != null) icon.setImageDrawable(drawable);
        else icon.setImageResource(R.drawable.ic_launcher_foreground);
        row.addView(icon, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(this, 34), Ui_26_10_25.dp(this, 34)));
        LinearLayout box = Ui_26_10_25.vertical(this);
        box.setPadding(Ui_26_10_25.dp(this, 12), 0, 0, 0);
        String label = app.label == null || app.label.isEmpty()
                ? Apps_26_10_25.label(this, app.pkg) : app.label;
        box.addView(Ui_26_10_25.text(this, label, 16, Ui_26_10_25.INK, true));
        box.addView(Ui_26_10_25.text(this,
                "打开 " + app.opens + " 次 · 在应用内 " + StatsMath_26_10_25.duration(app.foregroundMs)
                        + (app.reminders > 0 ? " · 提醒 " + app.reminders + " 次" : ""),
                12, Ui_26_10_25.MUTED, false));
        row.addView(box, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        ImageView chevron = new ImageView(this);
        chevron.setImageResource(R.drawable.ic_chevron_right);
        chevron.setColorFilter(Ui_26_10_25.MUTED);
        row.addView(chevron, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(this, 18), Ui_26_10_25.dp(this, 18)));
        row.setOnClickListener(v -> showSessions(app.pkg, label));
        return row;
    }

    private void showSessions(String pkg, String label) {
        inSessions = true;
        title.setText(label);
        content.removeAllViews();
        IntentDb_26_10_25 db = IntentDb_26_10_25.get(this);
        List<IntentDb_26_10_25.Session> sessions = db.sessionsForApp(pkg, 100);
        if (sessions.isEmpty()) {
            content.addView(Ui_26_10_25.hint(this, "这个应用还没有会话记录。"));
            return;
        }
        for (IntentDb_26_10_25.Session session : sessions) {
            content.addView(sessionCard(session), rowParams());
        }
    }

    private View sessionCard(IntentDb_26_10_25.Session session) {
        LinearLayout card = Ui_26_10_25.card(this);
        LinearLayout top = Ui_26_10_25.horizontal(this);
        TextView when = Ui_26_10_25.text(this,
                StatsMath_26_10_25.monthDay(session.startedWall) + " "
                        + StatsMath_26_10_25.clock(session.startedWall),
                13, Ui_26_10_25.INK, true);
        top.addView(when, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(Ui_26_10_25.text(this, statusLabel(session), 12,
                "left".equals(session.status) ? Ui_26_10_25.GREEN : Ui_26_10_25.MUTED, false));
        card.addView(top);

        String purpose = session.purpose == null || session.purpose.isEmpty()
                ? "未记录目的" : session.purpose;
        if (session.detail != null && !session.detail.isEmpty()) purpose += "：" + session.detail;
        if (session.emergency) purpose = "应急使用 · " + purpose;
        card.addView(Ui_26_10_25.text(this, purpose, 15, Ui_26_10_25.INK, true));

        StringBuilder line = new StringBuilder();
        line.append("计划 ").append(StatsMath_26_10_25.minutes(session.plannedMin));
        if (session.foregroundMs > 0) {
            line.append(" · 实际在应用内 ").append(StatsMath_26_10_25.duration(session.foregroundMs));
        }
        if (session.reviewCount > 0) line.append(" · 提醒 ").append(session.reviewCount).append(" 次");
        if (session.continueCount > 0) line.append(" · 继续 ").append(session.continueCount).append(" 次");
        if (session.savedEstMs > 0) {
            line.append(" · 估算少用 ").append(StatsMath_26_10_25.duration(session.savedEstMs));
        }
        card.addView(Ui_26_10_25.hint(this, line.toString()));
        return card;
    }

    private String statusLabel(IntentDb_26_10_25.Session session) {
        String status = session.status == null ? "" : session.status;
        if ("allowed".equals(status)) return "进行中";
        if ("left".equals(status)) return "复查页离开";
        if ("declined".equals(status)) return "先不打开";
        if ("abandoned".equals(status)) return "未选择";
        if ("interrupted".equals(status)) return "服务中断";
        if ("switched".equals(status)) return "切换到其他应用";
        if ("left_natural".equals(status)) return "已离开";
        if ("switched_to_encourage".equals(status)) return "本次改为鼓励";
        if ("pending".equals(status)) return "未完成";
        return status;
    }
}
