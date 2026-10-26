package com.intentpause.app;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.app.KeyguardManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

/**
 * 监听所选应用的前台切换，显示两页确认与复查。
 * 只取包名与窗口状态变化，不读取窗口内容；记录只存本机。
 */
public class IntentAccessibilityService_26_10_25 extends AccessibilityService
        implements PromptOverlay_26_10_25.Listener {
    private static final long MINUTE = 60_000L;
    private static final long GRACE = MINUTE;
    private static final long ENCOURAGE_GRACE = 3 * MINUTE;
    private static final int EMERGENCY_MINUTES = 3;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Set<String> imePackages = new HashSet<>();

    private IntentDb_26_10_25 db;
    private PromptOverlay_26_10_25 overlay;

    private String activePackage;
    private String label;
    private String purpose;
    private String detail;
    private int durationMinutes = 10;
    private boolean emergency;
    private boolean waitingForChoice;
    private boolean review;
    private boolean reviewLogged;
    private int page = 1;
    private long sessionId = -1;
    private long startedWall;
    private long allowedWall;
    private long dueWall;
    private long leftWall;
    private long foregroundMs;
    private long fgEnterWall;
    private long lastTargetSeenWall;
    private String foregroundPackage;
    private boolean screenOn = true;
    private boolean receiverRegistered;
    private EncourageBanner_26_10_26 encourageBanner;
    private AlternativeOffer_26_10_26 alternativeOffer;
    private String encouragePkg;
    private long encourageId = -1;
    private long encourageStartWall;
    private long encourageLeftWall;
    private long encourageFgEnterWall;
    private long encourageMs;
    private long encourageNextPromptWall;
    private int todayOpenCount;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            long now = System.currentTimeMillis();
            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_OFF:
                    screenOn = false;
                    pauseForeground(now);
                    encourageLeft(now);
                    if (overlay != null) overlay.hide();
                    if (encourageBanner != null) encourageBanner.hide();
                    persist();
                    break;
                case Intent.ACTION_SCREEN_ON:
                case Intent.ACTION_USER_PRESENT:
                    screenOn = true;
                    onScreenAvailable(now);
                    break;
                default:
                    break;
            }
        }
    };

    private final Runnable dueCheck = new Runnable() {
        @Override
        public void run() {
            if (dueWall <= 0 || activePackage == null || waitingForChoice) return;
            long wait = dueWall - System.currentTimeMillis();
            if (wait > 0) {
                handler.postDelayed(this, wait);
                return;
            }
            if (!interactive()) return;
            long now = System.currentTimeMillis();
            boolean inTarget = activePackage.equals(foregroundPackage)
                    || (lastTargetSeenWall > 0 && now - lastTargetSeenWall <= 3000);
            if (inTarget) showReview();
        }
    };

    private final Runnable graceCheck = new Runnable() {
        @Override
        public void run() {
            if (activePackage == null || leftWall <= 0) return;
            long now = System.currentTimeMillis();
            if (now - leftWall <= GRACE) return;
            if (!activePackage.equals(foregroundPackage)) {
                finishSession(waitingForChoice ? "abandoned" : "left_natural", 0, false);
            }
        }
    };

    private final Runnable leaveHide = new Runnable() {
        @Override
        public void run() {
            if (activePackage != null && !activePackage.equals(foregroundPackage)
                    && overlay != null) {
                overlay.hide();
            }
        }
    };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        db = IntentDb_26_10_25.get(this);
        overlay = new PromptOverlay_26_10_25(this, this);
        encourageBanner = new EncourageBanner_26_10_26(this);
        alternativeOffer = new AlternativeOffer_26_10_26(this);
        refreshInputMethods();
        registerScreenReceiver();
        db.serviceEvent("connected", "无障碍服务已连接", System.currentTimeMillis());
        KeepAliveService_26_10_26.ensure(this);
        restoreState();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;
        CharSequence name = event.getPackageName();
        if (name == null) return;
        String pkg = name.toString();
        if (pkg.equals(getPackageName())) return;
        if (isIgnoredWindow(pkg)) return;
        long now = System.currentTimeMillis();

        if (CarePause_26_10_26.paused(this)) {
            foregroundPackage = pkg;
            return;
        }
        if (isHomePackage(pkg)) {
            // 从确认页上滑回桌面：结束这次停留，不再把覆盖层盖回来。
            if (activePackage != null) finishSession("home", 0, false);
            if (encouragePkg != null) encourageLeft(now);
            foregroundPackage = pkg;
            if (overlay != null) overlay.hide();
            return;
        }
        if (encouragePkg != null && encouragePkg.equals(pkg)) {
            foregroundPackage = pkg;
            encourageForeground(now);
            if (encouragePkg == null && interactive()) {
                if (Apps_26_10_25.encouraged(this, pkg)
                        && !Apps_26_10_25.sensitive(this, pkg)) encourageEnter(pkg, now);
                else if (monitored(pkg)) startSession(pkg, now);
            }
            return;
        }
        if (encouragePkg != null) {
            encourageLeft(now);
        }
        if (Apps_26_10_25.encouraged(this, pkg) && !Apps_26_10_25.sensitive(this, pkg)) {
            if (activePackage != null && !activePackage.equals(pkg)) onTargetLeft(now);
            encourageEnter(pkg, now);
            return;
        }

        if (pkg.equals(foregroundPackage)) {
            sameForeground(pkg, now);
            return;
        }
        if (Apps_26_10_25.sensitive(this, pkg)) {
            // 敏感应用：当作离开目标应用，但不把包名写入快照/记录。
            if (activePackage != null && !activePackage.equals(pkg)) onTargetLeft(now);
            foregroundPackage = null;
            return;
        }
        foregroundPackage = pkg;
        persist();

        if (activePackage != null && !activePackage.equals(pkg)) {
            onTargetLeft(now);
        }

        if (activePackage != null && activePackage.equals(pkg)) {
            lastTargetSeenWall = now;
            handler.removeCallbacks(leaveHide);
            if (waitingForChoice) {
                leftWall = 0;
                if (overlay != null && !overlay.isShowing()) showCurrentOverlay();
                return;
            }
            if (leftWall > 0) {
                if (now - leftWall <= GRACE) {
                    leftWall = 0;
                    handler.removeCallbacks(graceCheck);
                    if (dueWall > 0 && now >= dueWall) {
                        showReview();
                    } else {
                        resumeForeground(now);
                        scheduleDue();
                    }
                } else {
                    finishSession("left_natural", 0, false);
                    startSession(pkg, now);
                }
                return;
            }
            if (dueWall > 0 && now >= dueWall) {
                showReview();
                return;
            }
            resumeForeground(now);
            return;
        }

        if (!monitored(pkg)) return;
        if (activePackage != null) finishSession("switched", 0, false);
        if (interactive()) startSession(pkg, now);
    }

    @Override
    public void onInterrupt() {
        persist();
        if (overlay != null) overlay.hide();
    }

    @Override
    public void onDestroy() {
        if (db != null) db.serviceEvent("disconnected", "无障碍服务已断开", System.currentTimeMillis());
        persist();
        handler.removeCallbacksAndMessages(null);
        if (overlay != null) overlay.hide();
        if (encourageBanner != null) encourageBanner.hide();
        if (alternativeOffer != null) alternativeOffer.hide();
        if (encouragePkg != null) endEncourage("service_destroyed", System.currentTimeMillis());
        if (receiverRegistered) {
            try {
                unregisterReceiver(screenReceiver);
            } catch (RuntimeException ignored) {
            }
            receiverRegistered = false;
        }
        super.onDestroy();
    }

    // ---------------- 事件与状态 ----------------

    private void sameForeground(String pkg, long now) {
        if (encouragePkg != null && encouragePkg.equals(pkg)) {
            encourageForeground(now);
            return;
        }
        if (activePackage != null && activePackage.equals(pkg)) {
            lastTargetSeenWall = now;
            handler.removeCallbacks(leaveHide);
        }
        if (activePackage == null) {
            if (monitored(pkg) && interactive()) startSession(pkg, now);
            return;
        }
        if (waitingForChoice) {
            if (overlay != null && !overlay.isShowing()) showCurrentOverlay();
            return;
        }
        if (dueWall > 0 && now >= dueWall) {
            showReview();
            return;
        }
        if (allowedWall > 0) resumeForeground(now);
    }

    private void onTargetLeft(long now) {
        if (leftWall == 0) {
            leftWall = now;
            pauseForeground(now);
            handler.postDelayed(graceCheck, GRACE + 100);
        }
        // 广告/过渡页会在目标应用内闪一下；延迟隐藏，目标很快回来时不打断确认页。
        handler.removeCallbacks(leaveHide);
        handler.postDelayed(leaveHide, 300);
        persist();
    }

    private void onScreenAvailable(long now) {
        if (!interactive()) return;
        if (encouragePkg != null && encouragePkg.equals(foregroundPackage)) {
            encourageForeground(now);
            if (encouragePkg != null) return;
        }
        if (activePackage == null) {
            if (foregroundPackage != null && Apps_26_10_25.encouraged(this, foregroundPackage)
                    && !Apps_26_10_25.sensitive(this, foregroundPackage)) {
                encourageEnter(foregroundPackage, now);
                return;
            }
            if (foregroundPackage != null && monitored(foregroundPackage) && interactive()) {
                startSession(foregroundPackage, now);
            }
            return;
        }
        if (!activePackage.equals(foregroundPackage)) return;
        if (waitingForChoice) {
            if (overlay != null && !overlay.isShowing()) showCurrentOverlay();
        } else if (dueWall > 0 && now >= dueWall) {
            showReview();
        } else if (allowedWall > 0) {
            resumeForeground(now);
        }
    }

    private void startSession(String pkg, long now) {
        if (encourageBanner != null) encourageBanner.hide();
        sessionId = db.startSession(pkg, Apps_26_10_25.label(this, pkg), now);
        activePackage = pkg;
        label = Apps_26_10_25.label(this, pkg);
        purpose = null;
        detail = "";
        durationMinutes = Apps_26_10_25.defaultMinutes(this, pkg);
        emergency = false;
        waitingForChoice = true;
        review = false;
        reviewLogged = false;
        page = 1;
        startedWall = now;
        allowedWall = 0;
        dueWall = 0;
        leftWall = 0;
        foregroundMs = 0;
        fgEnterWall = 0;
        lastTargetSeenWall = now;
        handler.removeCallbacks(dueCheck);
        handler.removeCallbacks(graceCheck);
        int priorOpens = db.allowedCountForPackage(pkg, StatsMath_26_10_25.startOfDay(now), now);
        todayOpenCount = priorOpens + 1;
        db.event(sessionId, activePackage, "prompt_shown", null, null, null, now);
        persist();
        if (interactive() && overlay != null) {
            overlay.showPrompt(activePackage, label, detail, todayOpenCount);
        }
    }

    private void allow(int minutes, boolean emergencyFlag, boolean first) {
        long now = System.currentTimeMillis();
        waitingForChoice = false;
        review = false;
        reviewLogged = false;
        page = 0;
        emergency = emergencyFlag;
        if (!emergencyFlag) durationMinutes = minutes;
        dueWall = now + minutes * MINUTE;
        if (first) {
            allowedWall = now;
            db.markAllowed(sessionId, minutes, emergency, dueWall, now);
        } else {
            db.extendDue(sessionId, dueWall);
        }
        if (activePackage != null && activePackage.equals(foregroundPackage)) fgEnterWall = now;
        db.event(sessionId, activePackage, "allowed", purpose, detail,
                String.valueOf(minutes), now);
        overlay.hide();
        persist();
        scheduleDue();
    }

    private void showReview() {
        if (encourageBanner != null) encourageBanner.hide();
        if (activePackage == null || !activePackage.equals(foregroundPackage) || !interactive()) return;
        long now = System.currentTimeMillis();
        waitingForChoice = true;
        review = true;
        handler.removeCallbacks(dueCheck);
        if (!reviewLogged) {
            db.bumpReview(sessionId);
            db.event(sessionId, activePackage, "review_shown", purpose, detail, null, now);
            reviewLogged = true;
        }
        int elapsed = (int) Math.max(1, Math.round(
                (now - (allowedWall > 0 ? allowedWall : now)) / 60000.0));
        overlay.showReview(activePackage, label, purpose, detail, emergency, elapsed, durationMinutes);
        persist();
    }

    private void showCurrentOverlay() {
        if (activePackage == null || overlay == null) return;
        if (review) {
            showReview();
        } else {
            restoreOverlay();
        }
    }

    private void restoreOverlay() {
        if (activePackage == null || overlay == null) return;
        if (review) {
            showReview();
        } else if (page == 2 && purpose != null) {
            overlay.showDuration(activePackage, label, purpose, detail, durationMinutes);
        } else if (page == 3) {
            overlay.showUnsure(activePackage, label, durationMinutes);
        } else {
            overlay.showPrompt(activePackage, label, detail, todayOpenCount);
        }
    }

    private void finishSession(String status, long savedEstMs, boolean alreadyEnded) {
        long now = System.currentTimeMillis();
        pauseForeground(now);
        if (sessionId > 0) {
            if (!alreadyEnded) db.markEnded(sessionId, status, savedEstMs, now);
            db.event(sessionId, activePackage, "ended", purpose, detail, status, now);
        }
        handler.removeCallbacks(dueCheck);
        handler.removeCallbacks(graceCheck);
        handler.removeCallbacks(leaveHide);
        overlay.hide();
        activePackage = null;
        label = null;
        purpose = null;
        detail = "";
        durationMinutes = Apps_26_10_25.DEFAULT_MINUTES;
        emergency = false;
        waitingForChoice = false;
        review = false;
        reviewLogged = false;
        page = 1;
        sessionId = -1;
        todayOpenCount = 0;
        startedWall = 0;
        allowedWall = 0;
        dueWall = 0;
        leftWall = 0;
        foregroundMs = 0;
        fgEnterWall = 0;
        SessionState_26_10_25.clear(this);
    }

    // ---------------- 鼓励应用 ----------------

    private void encourageEnter(String pkg, long now) {
        encourageEnter(pkg, now, false);
    }

    private void encourageEnter(String pkg, long now, boolean temporary) {
        if (encouragePkg != null && encouragePkg.equals(pkg)) {
            encourageForeground(now);
            return;
        }
        if (encouragePkg != null) endEncourage("switched", now);
        encouragePkg = pkg;
        encourageStartWall = now;
        encourageFgEnterWall = now;
        encourageLeftWall = 0;
        encourageMs = 0;
        foregroundPackage = pkg;
        encourageId = db.startEncourageSession(pkg, Apps_26_10_25.label(this, pkg), now, temporary);
        int interval = Apps_26_10_25.encourageInterval(this, pkg);
        encourageNextPromptWall = interval * MINUTE;
        if (Apps_26_10_25.encourageOnOpen(this, pkg) && interactive()) {
            showEncourageBanner(Apps_26_10_25.openingEncourageMessage(this, pkg), "open");
        }
        scheduleEncouragePrompt();
    }

    private void encourageForeground(long now) {
        if (encouragePkg == null) return;
        if (encourageLeftWall > 0) {
            if (now - encourageLeftWall <= ENCOURAGE_GRACE) {
                encourageFgEnterWall = now;
                encourageLeftWall = 0;
            } else {
                endEncourage("left_natural", now);
                return;
            }
        }
        if (encourageFgEnterWall == 0) encourageFgEnterWall = now;
        if (encourageNextPromptWall > 0 && encourageElapsed(now) >= encourageNextPromptWall
                && Apps_26_10_25.encourageDuring(this, encouragePkg)) {
            showEncourageBanner(Apps_26_10_25.nextEncourageMessage(this, encouragePkg), "during");
            encourageNextPromptWall = encourageElapsed(now)
                    + Apps_26_10_25.encourageInterval(this, encouragePkg) * MINUTE;
        }
        scheduleEncouragePrompt();
    }

    private void encourageLeft(long now) {
        if (encouragePkg == null || encourageLeftWall > 0) return;
        pauseEncourageForeground(now);
        encourageLeftWall = now;
        if (encourageBanner != null) encourageBanner.hide();
        handler.removeCallbacks(encourageDueCheck);
        handler.postDelayed(encourageGraceCheck, ENCOURAGE_GRACE + 100);
    }

    private void pauseEncourageForeground(long now) {
        if (encouragePkg != null && encourageFgEnterWall > 0) {
            encourageMs += Math.max(0, now - encourageFgEnterWall);
            if (encourageId > 0) db.addEncourageForeground(encourageId, Math.max(0, now - encourageFgEnterWall));
            encourageFgEnterWall = 0;
        }
    }

    private void endEncourage(String status, long now) {
        if (encouragePkg == null) return;
        pauseEncourageForeground(now);
        if (encourageId > 0) db.endEncourageSession(encourageId, status, now);
        if (encourageBanner != null) encourageBanner.hide();
        handler.removeCallbacks(encourageDueCheck);
        handler.removeCallbacks(encourageGraceCheck);
        encouragePkg = null;
        encourageId = -1;
        encourageStartWall = 0;
        encourageLeftWall = 0;
        encourageFgEnterWall = 0;
        encourageMs = 0;
        encourageNextPromptWall = 0;
    }

    private void showEncourageBanner(String message, String kind) {
        if (message == null || message.isEmpty()) return;
        if (overlay != null && overlay.isShowing()) return;
        if (encourageBanner != null) encourageBanner.show(message);
        db.event(-1, encouragePkg, "encourage_shown", null, null, kind, System.currentTimeMillis());
    }

    private final Runnable encourageDueCheck = new Runnable() {
        @Override
        public void run() {
            if (encouragePkg == null || !screenOn || !interactive()) return;
            if (!encouragePkg.equals(foregroundPackage) || encourageLeftWall > 0) return;
            if (overlay != null && overlay.isShowing()) return;
            long now = System.currentTimeMillis();
            if (encourageNextPromptWall <= 0 || encourageElapsed(now) < encourageNextPromptWall) {
                scheduleEncouragePrompt();
                return;
            }
            if (Apps_26_10_25.encourageDuring(IntentAccessibilityService_26_10_25.this, encouragePkg)) {
                showEncourageBanner(Apps_26_10_25.nextEncourageMessage(
                        IntentAccessibilityService_26_10_25.this, encouragePkg), "during");
                encourageNextPromptWall = encourageElapsed(now)
                        + Apps_26_10_25.encourageInterval(IntentAccessibilityService_26_10_25.this, encouragePkg) * MINUTE;
            }
            scheduleEncouragePrompt();
        }
    };

    private final Runnable encourageGraceCheck = new Runnable() {
        @Override
        public void run() {
            if (encouragePkg == null || encourageLeftWall <= 0) return;
            long now = System.currentTimeMillis();
            if (now - encourageLeftWall <= ENCOURAGE_GRACE) return;
            if (!encouragePkg.equals(foregroundPackage)) endEncourage("left_natural", now);
        }
    };

    private void scheduleEncouragePrompt() {
        handler.removeCallbacks(encourageDueCheck);
        if (encouragePkg == null || encourageLeftWall > 0 || encourageFgEnterWall == 0
                || !encouragePkg.equals(foregroundPackage) || !screenOn || !interactive()) return;
        if (!Apps_26_10_25.encourageDuring(this, encouragePkg)) return;
        long delay = Math.max(1000, encourageNextPromptWall - encourageElapsed(System.currentTimeMillis()));
        handler.postDelayed(encourageDueCheck, delay);
    }

    private long encourageElapsed(long now) {
        return encourageMs + (encourageFgEnterWall > 0 ? Math.max(0, now - encourageFgEnterWall) : 0);
    }

    private void pauseForeground(long now) {
        if (fgEnterWall > 0) {
            long delta = Math.max(0, now - fgEnterWall);
            foregroundMs += delta;
            if (sessionId > 0) db.addForeground(sessionId, delta);
            fgEnterWall = 0;
        }
    }

    private void resumeForeground(long now) {
        if (fgEnterWall == 0 && activePackage != null && activePackage.equals(foregroundPackage)) {
            fgEnterWall = now;
        }
    }

    private void scheduleDue() {
        handler.removeCallbacks(dueCheck);
        long now = System.currentTimeMillis();
        if (dueWall > now) handler.postDelayed(dueCheck, dueWall - now);
    }

    // ---------------- 覆盖层回调 ----------------

    @Override
    public void onPurpose(String selectedPurpose) {
        if (sessionId <= 0) return;
        long now = System.currentTimeMillis();
        if ("还没想清楚".equals(selectedPurpose)) {
            purpose = null;
            detail = "";
            db.updatePurpose(sessionId, null, null);
            db.event(sessionId, activePackage, "unsure", null, null, null, now);
            page = 3;
            persist();
            overlay.showUnsure(activePackage, label, durationMinutes);
            return;
        }
        purpose = selectedPurpose;
        detail = "";
        db.updatePurpose(sessionId, purpose, detail);
        db.event(sessionId, activePackage, "purpose_chosen", purpose, detail, null, now);
        page = 2;
        persist();
        overlay.showDuration(activePackage, label, purpose, detail, durationMinutes);
    }

    @Override
    public void onEmergency() {
        long now = System.currentTimeMillis();
        purpose = "应急使用";
        detail = "";
        emergency = true;
        db.updatePurpose(sessionId, purpose, detail);
        db.event(sessionId, activePackage, "emergency", purpose, null, null, now);
        allow(EMERGENCY_MINUTES, true, true);
    }

    @Override
    public void onTemporaryEncourage() {
        if (activePackage == null || sessionId <= 0) return;
        String pkg = activePackage;
        long now = System.currentTimeMillis();
        db.event(sessionId, pkg, "temporary_encourage", null, null, null, now);
        finishSession("switched_to_encourage", 0, false);
        encourageEnter(pkg, now, true);
    }

    @Override
    public void onDecline() {
        long now = System.currentTimeMillis();
        db.event(sessionId, activePackage, "decline", purpose, detail, null, now);
        db.markDeclined(sessionId, now);
        finishSession("declined", 0, true);
        showAlternative();
        performGlobalAction(GLOBAL_ACTION_HOME);
    }

    @Override
    public void onDuration(int minutes, String typedDetail) {
        long now = System.currentTimeMillis();
        detail = typedDetail == null ? "" : typedDetail;
        minutes = Math.max(1, Math.min(180, minutes));
        db.updatePurpose(sessionId, purpose, detail);
        db.event(sessionId, activePackage, "duration_chosen", purpose, detail,
                String.valueOf(minutes), now);
        allow(minutes, false, true);
    }

    @Override
    public void onBack(String typedDetail) {
        long now = System.currentTimeMillis();
        detail = typedDetail == null ? "" : typedDetail;
        purpose = null;
        db.updatePurpose(sessionId, null, detail);
        db.event(sessionId, activePackage, "back_to_purpose", null, detail, null, now);
        page = 1;
        persist();
        overlay.showPrompt(activePackage, label, detail, todayOpenCount);
    }

    @Override
    public void onUnsureRelax() {
        long now = System.currentTimeMillis();
        purpose = "放松一下";
        detail = "";
        db.updatePurpose(sessionId, purpose, detail);
        db.event(sessionId, activePackage, "purpose_chosen", purpose, detail, "from_unsure", now);
        allow(durationMinutes, false, true);
    }

    @Override
    public void onContinue(int minutes) {
        long now = System.currentTimeMillis();
        minutes = Math.max(1, Math.min(180, minutes));
        db.event(sessionId, activePackage, "review_continue", purpose, detail,
                String.valueOf(minutes), now);
        db.bumpContinue(sessionId);
        allow(minutes, false, false);
    }

    @Override
    public void onChangePurpose() {
        long now = System.currentTimeMillis();
        db.event(sessionId, activePackage, "review_change", purpose, detail, null, now);
        purpose = null;
        detail = "";
        waitingForChoice = true;
        review = false;
        reviewLogged = false;
        db.updatePurpose(sessionId, null, null);
        page = 1;
        persist();
        overlay.showPrompt(activePackage, label, detail, todayOpenCount);
    }

    @Override
    public void onLeave() {
        long now = System.currentTimeMillis();
        db.event(sessionId, activePackage, "review_leave", purpose, detail, null, now);
        long saved = StatsMath_26_10_25.savedEstimateMs(durationMinutes);
        finishSession("left", saved, false);
        showAlternative();
        performGlobalAction(GLOBAL_ACTION_HOME);
    }

    private void showAlternative() {
        if (alternativeOffer == null) return;
        alternativeOffer.show(new AlternativeOffer_26_10_26.Listener() {
            @Override
            public void onOpenApp(String pkg) {
                try {
                    android.content.Intent intent = getPackageManager()
                            .getLaunchIntentForPackage(pkg);
                    if (intent != null) {
                        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                } catch (RuntimeException ignored) {
                }
            }

            @Override
            public void onSmallAction() {
                Ui_26_10_25.toast(IntentAccessibilityService_26_10_25.this,
                        "去喝口水吧，回来再继续。");
            }

            @Override
            public void onHome() {
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
        });
    }

    @Override
    public void onError(String reason) {
        if (db != null) db.serviceEvent("overlay_error", reason, System.currentTimeMillis());
        Notify_26_10_25.overlayFallback(this, label);
        if (activePackage != null && sessionId > 0) {
            emergency = true;
            purpose = "应急使用";
            detail = "";
            db.event(sessionId, activePackage, "overlay_error", purpose, null, reason,
                    System.currentTimeMillis());
            allow(EMERGENCY_MINUTES, true, true);
        }
    }

    // ---------------- 持久化与工具 ----------------

    private void persist() {
        SessionState_26_10_25.Snapshot s = new SessionState_26_10_25.Snapshot();
        s.sessionId = sessionId;
        s.pkg = activePackage;
        s.label = label;
        s.purpose = purpose;
        s.detail = detail;
        s.durationMinutes = durationMinutes;
        s.emergency = emergency;
        s.waitingForChoice = waitingForChoice;
        s.review = review;
        s.dueWall = dueWall;
        s.leftWall = leftWall;
        s.allowedWall = allowedWall;
        s.startedWall = startedWall;
        s.foregroundMs = foregroundMs;
        // 只记录目标应用；其他应用（包括敏感应用）的包名不写入快照。
        s.foreground = (activePackage != null && activePackage.equals(foregroundPackage))
                ? foregroundPackage : null;
        s.page = page;
        SessionState_26_10_25.save(this, s);
    }

    private void restoreState() {
        SessionState_26_10_25.Snapshot s = SessionState_26_10_25.load(this);
        if (!s.active()) return;
        sessionId = s.sessionId;
        activePackage = s.pkg;
        label = s.label;
        purpose = s.purpose;
        detail = s.detail;
        durationMinutes = s.durationMinutes;
        emergency = s.emergency;
        waitingForChoice = s.waitingForChoice;
        review = s.review;
        reviewLogged = false;
        dueWall = s.dueWall;
        leftWall = s.leftWall;
        allowedWall = s.allowedWall;
        startedWall = s.startedWall;
        foregroundMs = s.foregroundMs;
        foregroundPackage = s.foreground;
        page = s.page;
        if (activePackage != null && activePackage.equals(foregroundPackage)) {
            lastTargetSeenWall = System.currentTimeMillis();
        }
        db.serviceEvent("restore", "恢复会话 " + activePackage, System.currentTimeMillis());
        if (waitingForChoice) {
            handler.postDelayed(() -> {
                if (activePackage != null && activePackage.equals(foregroundPackage)
                        && interactive() && overlay != null) {
                    restoreOverlay();
                }
            }, 500);
        } else {
            scheduleDue();
        }
    }

    private void registerScreenReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        ContextCompat.registerReceiver(this, screenReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
        receiverRegistered = true;
    }

    private void refreshInputMethods() {
        imePackages.clear();
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                for (InputMethodInfo info : imm.getEnabledInputMethodList()) {
                    imePackages.add(info.getPackageName());
                }
            }
        } catch (RuntimeException ignored) {
        }
    }

    /** 输入法、系统面板与权限弹窗不是“切换到了别的应用”，不应结束或重置会话。 */
    private boolean isIgnoredWindow(String pkg) {
        if (pkg.equals("com.android.systemui")) return true;
        if (pkg.startsWith("com.android.permissioncontroller")) return true;
        if (pkg.startsWith("com.google.android.permissioncontroller")) return true;
        if (imePackages.contains(pkg)) return true;
        return pkg.contains("inputmethod") || pkg.contains("input_method");
    }

    private boolean isHomePackage(String pkg) {
        try {
            android.content.Intent home = new android.content.Intent(
                    android.content.Intent.ACTION_MAIN)
                    .addCategory(android.content.Intent.CATEGORY_HOME);
            android.content.pm.ResolveInfo info = getPackageManager()
                    .resolveActivity(home, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
            return info != null && info.activityInfo != null
                    && pkg.equals(info.activityInfo.packageName);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private boolean selected(String pkg) {
        SharedPreferences prefs = getSharedPreferences("selected_apps", MODE_PRIVATE);
        return prefs.getBoolean(pkg, false);
    }

    private boolean monitored(String pkg) {
        return selected(pkg) && !Apps_26_10_25.sensitive(this, pkg);
    }

    private boolean interactive() {
        PowerManager power = (PowerManager) getSystemService(POWER_SERVICE);
        if (power == null || !power.isInteractive() || !screenOn) return false;
        KeyguardManager keyguard = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        return keyguard == null || !keyguard.isKeyguardLocked();
    }
}
