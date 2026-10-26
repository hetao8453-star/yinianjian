package com.intentpause.app;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** 退出受控应用后的可选小入口：去一个鼓励应用，或做一个小动作；不强制。 */
public class AlternativeOffer_26_10_26 {
    public interface Listener {
        void onOpenApp(String pkg);
        void onSmallAction();
        void onHome();
    }

    private final Context context;
    private final WindowManager wm;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private View view;
    private final Runnable hideTask = this::hide;

    public AlternativeOffer_26_10_26(Context context) {
        this.context = context;
        this.wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void show(Listener listener) {
        hide();
        List<String> apps = Apps_26_10_25.encouragePackages(context);
        String pkg = apps.isEmpty() ? null : apps.get(0);
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(14), dp(18), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Ui_26_10_25.CARD);
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(1), Ui_26_10_25.LINE);
        card.setBackground(bg);
        card.setElevation(dp(8));

        TextView title = new TextView(context);
        title.setText("要不要换个方式？");
        title.setTextSize(14);
        title.setTextColor(Ui_26_10_25.INK);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(title);

        if (pkg != null) {
            TextView app = new TextView(context);
            app.setText("去 " + Apps_26_10_25.label(context, pkg) + " 一会儿");
            app.setTextSize(13);
            app.setTextColor(Ui_26_10_25.GREEN);
            app.setPadding(0, dp(8), 0, 0);
            app.setOnClickListener(v -> {
                hide();
                listener.onOpenApp(pkg);
            });
            card.addView(app);
        }

        TextView action = new TextView(context);
        action.setText("喝口水，走两步");
        action.setTextSize(13);
        action.setTextColor(Ui_26_10_25.MUTED);
        action.setPadding(0, dp(8), 0, 0);
        action.setOnClickListener(v -> {
            hide();
            listener.onSmallAction();
        });
        card.addView(action);

        TextView home = new TextView(context);
        home.setText("回桌面");
        home.setTextSize(12);
        home.setTextColor(Ui_26_10_25.MUTED);
        home.setGravity(Gravity.END);
        home.setPadding(0, dp(6), 0, 0);
        home.setOnClickListener(v -> {
            hide();
            listener.onHome();
        });
        card.addView(home);

        int width = context.getResources().getDisplayMetrics().widthPixels - dp(32);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                width, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.BOTTOM | Gravity.START;
        lp.x = dp(16);
        lp.y = dp(80);
        try {
            wm.addView(card, lp);
            view = card;
            handler.postDelayed(hideTask, 6000);
        } catch (RuntimeException ignored) {
        }
    }

    public void hide() {
        handler.removeCallbacks(hideTask);
        if (view != null) {
            try {
                wm.removeView(view);
            } catch (RuntimeException ignored) {
            }
            view = null;
        }
    }

    private int dp(int v) {
        return Math.round(v * context.getResources().getDisplayMetrics().density);
    }
}
