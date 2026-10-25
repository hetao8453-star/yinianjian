package com.intentpause.app;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class IntentAccessibilityService_26_10_25 extends AccessibilityService {
    private static final long MINUTE = 60_000L;
    private static final long GRACE = MINUTE;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager windows;
    private View overlay;
    private String activePackage;
    private String foregroundPackage;
    private String purpose;
    private String detail;
    private long leftAt;
    private long dueAt;
    private int durationMinutes = 10;
    private boolean waitingForChoice;
    private boolean review;
    private boolean emergency;
    private boolean unsure;
    private final Runnable dueCheck = new Runnable() {
        @Override public void run() {
            if (activePackage == null || dueAt == 0) return;
            long wait = dueAt - android.os.SystemClock.elapsedRealtime();
            if (wait > 0) { handler.postDelayed(this, wait); return; }
            if (activePackage.equals(foregroundPackage) && interactive()) showReview();
        }
    };

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        windows = (WindowManager) getSystemService(WINDOW_SERVICE);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        if (pkg.equals(getPackageName()) || pkg.equals("com.android.systemui") || pkg.startsWith("com.google.android.inputmethod")) return;
        if (pkg.equals(foregroundPackage)) {
            if (pkg.equals(activePackage) && interactive()) {
                if (waitingForChoice && overlay == null) showPrompt(review);
                else if (!waitingForChoice && dueAt > 0 && android.os.SystemClock.elapsedRealtime() >= dueAt) showReview();
            }
            return;
        }
        foregroundPackage = pkg;
        if (activePackage != null && !activePackage.equals(pkg)) {
            if (leftAt == 0) leftAt = android.os.SystemClock.elapsedRealtime();
            removeOverlay();
        }
        if (!selected(pkg) || !interactive()) return;
        long now = android.os.SystemClock.elapsedRealtime();
        if (pkg.equals(activePackage) && leftAt > 0 && now - leftAt <= GRACE) {
            leftAt = 0;
            if (waitingForChoice) showPrompt(review);
            else if (dueAt > 0 && now >= dueAt) showReview();
            return;
        }
        if (pkg.equals(activePackage) && leftAt == 0 && !waitingForChoice) {
            if (dueAt > 0 && now >= dueAt) showReview();
            return;
        }
        activePackage = pkg;
        leftAt = 0;
        dueAt = 0;
        purpose = null;
        detail = null;
        emergency = false;
        waitingForChoice = true;
        review = false;
        showPrompt(false);
    }

    @Override public void onInterrupt() { removeOverlay(); }
    @Override public void onDestroy() { handler.removeCallbacksAndMessages(null); removeOverlay(); super.onDestroy(); }

    private boolean selected(String pkg) {
        SharedPreferences prefs = getSharedPreferences("selected_apps", MODE_PRIVATE);
        return prefs.getBoolean(pkg, false);
    }
    private boolean interactive() {
        PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
        return power != null && power.isInteractive();
    }
    private int dp(float n) { return (int) (n * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable bg(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color); drawable.setCornerRadius(dp(radius)); return drawable;
    }
    private TextView label(String value, int sp, boolean bold) {
        TextView text = new TextView(this); text.setText(value); text.setTextSize(sp);
        text.setTextColor(Color.rgb(37,44,40));
        if (bold) text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return text;
    }
    private void add(LinearLayout box, View view, int top) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.topMargin = dp(top); box.addView(view, lp);
    }
    private Button button(String value, boolean primary, Runnable action) {
        Button button = new Button(this); button.setText(value); button.setAllCaps(false);
        button.setTextSize(16); button.setMinHeight(dp(52));
        button.setTextColor(primary ? Color.WHITE : Color.rgb(37,44,40));
        button.setBackground(bg(primary ? Color.rgb(54,94,75) : Color.rgb(231,239,233), 14));
        button.setOnClickListener(v -> action.run()); return button;
    }
    private LinearLayout container() {
        LinearLayout panel = new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(36), dp(24), dp(28));
        panel.setBackgroundColor(Color.rgb(247,246,242)); return panel;
    }
    private void display(LinearLayout panel) {
        removeOverlay();
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.addView(panel);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(-1,-1,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                android.graphics.PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP; lp.setTitle("意图提醒");
        try { windows.addView(scroll, lp); overlay = scroll; }
        catch (RuntimeException ignored) { overlay = null; }
    }
    private void removeOverlay() {
        if (overlay != null && windows != null) {
            try { windows.removeView(overlay); } catch (RuntimeException ignored) { }
        }
        overlay = null;
    }
    private String appName() {
        try { return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(activePackage, 0)).toString(); }
        catch (Exception ignored) { return "这个应用"; }
    }
    private void showPrompt(boolean fromReview) {
        if (activePackage == null || !activePackage.equals(foregroundPackage) || !interactive()) return;
        waitingForChoice = true; review = fromReview;
        LinearLayout panel = container();
        add(panel, label(appName(),14,false), 4);
        add(panel, label("这次打开，想做什么？",26,true), 24);
        add(panel, label("选一个目的，再决定是否继续。想休息也可以。",15,false), 12);
        EditText input = new EditText(this); input.setSingleLine(true); input.setTextSize(15);
        input.setHint("具体要做什么？可不填"); input.setBackground(bg(Color.WHITE,12));
        input.setPadding(dp(16),dp(12),dp(16),dp(12));
        if (detail != null) input.setText(detail);
        add(panel, input, 22);
        String[] choices = {"查找内容", "回复消息", "放松一下", "还没想清楚"};
        for (String choice : choices) {
            add(panel, button(choice, choice.equals(purpose), () -> {
                detail = input.getText().toString().trim();
                if (choice.equals("还没想清楚")) {
                    unsure = true; purpose = null; showUnsure();
                } else {
                    unsure = false; purpose = choice; showPrompt(fromReview);
                }
            }), 8);
        }
        add(panel, label("本次先用 " + durationMinutes + " 分钟",14,false), 24);
        add(panel, button("5 分钟",false,()->{durationMinutes=5;showPrompt(fromReview);}),8);
        add(panel, button("10 分钟",false,()->{durationMinutes=10;showPrompt(fromReview);}),8);
        add(panel, button("20 分钟",false,()->{durationMinutes=20;showPrompt(fromReview);}),8);
        if (purpose != null) add(panel,button("按这个选择继续",true,()->{
            detail = input.getText().toString().trim(); allow(durationMinutes * MINUTE);
        }),24);
        add(panel,button("先不打开",false,this::leave),12);
        add(panel,button("应急使用 1 分钟",false,()->{
            purpose = "应急使用"; detail = ""; emergency = true;
            getSharedPreferences("local_counts",MODE_PRIVATE).edit().putInt("emergency_count",getSharedPreferences("local_counts",MODE_PRIVATE).getInt("emergency_count",0)+1).apply();
            allow(MINUTE);
        }),12);
        display(panel);
    }
    private void showUnsure() {
        if (activePackage == null || !activePackage.equals(foregroundPackage)) return;
        LinearLayout panel = container();
        add(panel,label(appName(),14,false),4);
        add(panel,label("还没想清楚，也没关系",26,true),28);
        add(panel,label("想休息也可以，主动选一下就好。",16,false),12);
        add(panel,button("主动放松 " + durationMinutes + " 分钟",true,()->{purpose="放松一下";unsure=false;allow(durationMinutes*MINUTE);}),28);
        add(panel,button("先不打开",false,this::leave),12);
        display(panel);
    }
    private void allow(long milliseconds) {
        waitingForChoice = false; review = false;
        dueAt = android.os.SystemClock.elapsedRealtime() + milliseconds;
        handler.removeCallbacks(dueCheck); handler.postDelayed(dueCheck,milliseconds);
        removeOverlay();
    }
    private void showReview() {
        if (activePackage == null || !activePackage.equals(foregroundPackage) || !interactive()) return;
        waitingForChoice = true; review = true;
        LinearLayout panel = container();
        add(panel,label(appName(),14,false),4);
        add(panel,label(emergency ? "1 分钟到了" : "时间到了，现在想怎么选？",26,true),24);
        String remembered = purpose == null ? "尚未选择" : purpose;
        if (detail != null && !detail.isEmpty()) remembered += "：" + detail;
        add(panel,label("刚才的目的：" + remembered,17,false),20);
        add(panel,button("继续一会儿",true,()->allow(durationMinutes*MINUTE)),28);
        add(panel,button("换个目的",false,()->{purpose=null;detail=null;showPrompt(true);}),12);
        add(panel,button("现在离开",false,this::leave),12);
        display(panel);
    }
    private void leave() {
        handler.removeCallbacks(dueCheck); removeOverlay();
        activePackage = null; dueAt = 0; purpose = null; detail = null;
        waitingForChoice = false; review = false; unsure = false; emergency = false;
        performGlobalAction(GLOBAL_ACTION_HOME);
    }
}
