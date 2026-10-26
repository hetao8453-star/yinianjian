package com.intentpause.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** 鼓励应用顶部轻提示：不触摸、不抢焦点，2.5 秒自动消失。 */
public class EncourageBanner_26_10_26 {
    private final Context context;
    private final WindowManager wm;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private View view;
    private WindowManager.LayoutParams params;
    private final Runnable hideTask = this::hide;

    public EncourageBanner_26_10_26(Context context) {
        this.context = context;
        this.wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void show(String message) {
        hide();
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(16), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Ui_26_10_25.CARD);
        bg.setCornerRadius(dp(20));
        bg.setStroke(dp(1), Ui_26_10_25.LINE);
        card.setBackground(bg);
        card.setElevation(dp(6));

        ImageView mascot = new ImageView(context);
        mascot.setImageResource(R.drawable.mascot_xiaohuan);
        card.addView(mascot, new LinearLayout.LayoutParams(dp(38), dp(38)));

        TextView text = new TextView(context);
        text.setText(message);
        text.setTextSize(14);
        text.setTextColor(Ui_26_10_25.INK);
        text.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        text.setLineSpacing(dp(2), 1f);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        tp.leftMargin = dp(10);
        card.addView(text, tp);

        int width = context.getResources().getDisplayMetrics().widthPixels - dp(32);
        params = new WindowManager.LayoutParams(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = dp(16);
        params.y = dp(54);
        try {
            wm.addView(card, params);
            view = card;
            handler.postDelayed(hideTask, 2500);
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
