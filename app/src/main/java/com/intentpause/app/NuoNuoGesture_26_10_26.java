package com.intentpause.app;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.OvershootInterpolator;

/** 糯糯可被轻轻拖起；放手回原位，不改变页面布局和按钮位置。 */
public final class NuoNuoGesture_26_10_26 {
    public interface DragListener {
        void onDragStart();
        void onDragEnd();
    }
    private NuoNuoGesture_26_10_26() {}

    public static void attach(View mascot) {
        attach(mascot, null);
    }

    public static void attach(View mascot, DragListener listener) {
        final float[] origin = new float[2];
        final boolean[] dragging = {false};
        final int slop = ViewConfiguration.get(mascot.getContext()).getScaledTouchSlop();
        mascot.setClickable(true);
        mascot.setContentDescription("糯糯，轻点会点头，也可以拖动");
        mascot.setOnClickListener(v -> Ui_26_10_25.nodOnce(v));
        mascot.setOnTouchListener((v, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    origin[0] = event.getRawX();
                    origin[1] = event.getRawY();
                    dragging[0] = false;
                    v.animate().cancel();
                    if (v.getParent() != null) v.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - origin[0];
                    float dy = event.getRawY() - origin[1];
                    if (!dragging[0] && Math.hypot(dx, dy) > slop) {
                        dragging[0] = true;
                        if (listener != null) listener.onDragStart();
                        if (v.getParent() != null) v.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    if (dragging[0]) {
                        float limit = Ui_26_10_25.dp(v.getContext(), 52);
                        v.setTranslationX(Math.max(-limit, Math.min(limit, dx)));
                        v.setTranslationY(Math.max(-limit, Math.min(limit, dy)));
                        if (!Ui_26_10_25.reduceMotion(v.getContext())) {
                            v.setRotation(Math.max(-11f, Math.min(11f, dx / limit * 11f)));
                            v.setScaleX(1.09f);
                            v.setScaleY(0.94f);
                        }
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!dragging[0] && event.getActionMasked() == MotionEvent.ACTION_UP)
                        v.performClick();
                    v.animate().translationX(0).translationY(0).rotation(0)
                            .scaleX(1).scaleY(1)
                            .setDuration(Ui_26_10_25.reduceMotion(v.getContext()) ? 0 : 420)
                            .setInterpolator(new OvershootInterpolator(0.55f)).start();
                    if (dragging[0] && listener != null) listener.onDragEnd();
                    dragging[0] = false;
                    return true;
                default:
                    return false;
            }
        });
    }
}
