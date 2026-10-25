package com.intentpause.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** 共享的颜色、字号、间距与常用控件，避免每个页面重复写样式。 */
public final class Ui_26_10_25 {
    public static final int PAPER = Color.rgb(247, 246, 242);
    public static final int CARD = Color.WHITE;
    public static final int INK = Color.rgb(37, 44, 40);
    public static final int MUTED = Color.rgb(110, 117, 111);
    public static final int GREEN = Color.rgb(54, 94, 75);
    public static final int GREEN_SOFT = Color.rgb(231, 239, 233);
    public static final int GREEN_TINT = Color.rgb(241, 246, 242);
    public static final int LINE = Color.rgb(236, 234, 227);
    public static final int WARN = Color.rgb(150, 103, 72);
    public static final int CHART_GREEN = Color.rgb(111, 156, 132);
    public static final int CHART_SAGE = Color.rgb(169, 195, 180);
    public static final int CHART_SAND = Color.rgb(217, 199, 167);
    public static final int CHART_CLAY = Color.rgb(216, 180, 166);
    public static final int CHART_MIST = Color.rgb(183, 200, 214);

    private Ui_26_10_25() {
    }

    public static int blend(int from, int to, float fraction) {
        float f = Math.max(0f, Math.min(1f, fraction));
        int a = (int) (Color.alpha(from) + (Color.alpha(to) - Color.alpha(from)) * f);
        int r = (int) (Color.red(from) + (Color.red(to) - Color.red(from)) * f);
        int g = (int) (Color.green(from) + (Color.green(to) - Color.green(from)) * f);
        int b = (int) (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * f);
        return Color.argb(a, r, g, b);
    }

    public static int dp(Context c, float value) {
        return (int) (value * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static GradientDrawable round(Context c, int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    public static GradientDrawable roundStroke(Context c, int color, int strokeColor, int radiusDp) {
        GradientDrawable d = round(c, color, radiusDp);
        d.setStroke(dp(c, 1), strokeColor);
        return d;
    }

    public static Drawable ripple(Context c, int color, int radiusDp) {
        return new RippleDrawable(ColorStateList.valueOf(0x1F365E4B), round(c, color, radiusDp), null);
    }

    public static Drawable rippleStroke(Context c, int fill, int stroke, int radiusDp) {
        return new RippleDrawable(ColorStateList.valueOf(0x1F365E4B),
                roundStroke(c, fill, stroke, radiusDp), null);
    }

    public static Button pill(Context c, CharSequence s, boolean selected) {
        Button b = new Button(c);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(selected ? Color.WHITE : GREEN);
        b.setBackground(ripple(c, selected ? GREEN : GREEN_SOFT, 999));
        b.setMinHeight(dp(c, 46));
        b.setMinimumHeight(dp(c, 46));
        b.setPadding(dp(c, 8), dp(c, 8), dp(c, 8), dp(c, 8));
        b.setStateListAnimator(null);
        b.setElevation(0);
        return b;
    }

    public static TextView text(Context c, CharSequence s, float sp, int color, boolean bold) {
        TextView v = new TextView(c);
        v.setText(s);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setIncludeFontPadding(false);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    public static TextView title(Context c, CharSequence s) {
        return text(c, s, 26, INK, true);
    }

    public static TextView subtitle(Context c, CharSequence s) {
        return text(c, s, 15, MUTED, false);
    }

    public static TextView section(Context c, CharSequence s) {
        TextView v = text(c, s, 18, INK, true);
        v.setPadding(0, dp(c, 6), 0, 0);
        return v;
    }

    public static TextView hint(Context c, CharSequence s) {
        TextView v = text(c, s, 13, MUTED, false);
        v.setLineSpacing(dp(c, 3), 1f);
        return v;
    }

    public static Button button(Context c, CharSequence s, boolean primary) {
        Button b = new Button(c);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(primary ? Color.WHITE : GREEN);
        b.setBackground(ripple(c, primary ? GREEN : GREEN_SOFT, 14));
        b.setMinHeight(dp(c, 52));
        b.setMinimumHeight(dp(c, 52));
        b.setPadding(dp(c, 16), dp(c, 12), dp(c, 16), dp(c, 12));
        b.setStateListAnimator(null);
        b.setElevation(0);
        return b;
    }

    public static Button ghost(Context c, CharSequence s) {
        Button b = new Button(c);
        b.setText(s);
        b.setAllCaps(false);
        b.setTextSize(15);
        b.setTextColor(GREEN);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setMinHeight(dp(c, 48));
        b.setMinimumHeight(dp(c, 48));
        b.setStateListAnimator(null);
        b.setElevation(0);
        return b;
    }

    public static LinearLayout card(Context c) {
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundStroke(c, CARD, LINE, 20));
        box.setPadding(dp(c, 18), dp(c, 18), dp(c, 18), dp(c, 18));
        return box;
    }

    public static View divider(Context c) {
        View v = new View(c);
        v.setBackgroundColor(LINE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(c, 1)));
        v.setLayoutParams(lp);
        return v;
    }

    public static void add(LinearLayout parent, View child, int topDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(parent.getContext(), topDp);
        parent.addView(child, lp);
    }

    public static void addRow(LinearLayout parent, View child, int topDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(parent.getContext(), 56));
        lp.topMargin = dp(parent.getContext(), topDp);
        parent.addView(child, lp);
    }

    public static ImageButton iconButton(Context c, int drawable, int sizeDp, Runnable onClick) {
        ImageButton b = new ImageButton(c);
        b.setImageResource(drawable);
        b.setColorFilter(INK);
        b.setBackground(ripple(c, Color.TRANSPARENT, 999));
        b.setPadding(dp(c, 12), dp(c, 12), dp(c, 12), dp(c, 12));
        b.setOnClickListener(v -> onClick.run());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp));
        b.setLayoutParams(lp);
        return b;
    }

    public static void toast(Context c, CharSequence s) {
        Toast.makeText(c, s, Toast.LENGTH_SHORT).show();
    }

    /** Android 15+ 强制 edge-to-edge；这里统一处理系统栏外观与内容避让。 */
    public static void applyWindow(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView());
        controller.setAppearanceLightStatusBars(true);
        controller.setAppearanceLightNavigationBars(true);
    }

    public static void applyInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, Math.max(bars.bottom, ime.bottom));
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    public static LinearLayout vertical(Context c) {
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.VERTICAL);
        return box;
    }

    public static LinearLayout horizontal(Context c) {
        LinearLayout box = new LinearLayout(c);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        return box;
    }

    public static TextView action(Context c, CharSequence s, Runnable run) {
        TextView v = text(c, s, 14, GREEN, true);
        v.setPadding(dp(c, 8), dp(c, 8), 0, dp(c, 8));
        v.setOnClickListener(x -> run.run());
        return v;
    }
}
