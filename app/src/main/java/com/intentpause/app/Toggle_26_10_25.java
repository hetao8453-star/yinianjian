package com.intentpause.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

/** 轻量开关：避免系统 Switch 在自定义主题下的状态绘制问题。 */
public class Toggle_26_10_25 extends View {
    public interface OnToggleRequested {
        void onToggleRequested(boolean next);
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private boolean checked;
    private float progress;
    private OnToggleRequested listener;

    public Toggle_26_10_25(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        setClickable(true);
        setFocusable(true);
    }

    public void setOnToggleRequested(OnToggleRequested listener) {
        this.listener = listener;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked, boolean animate) {
        this.checked = checked;
        float target = checked ? 1f : 0f;
        if (!animate) {
            progress = target;
            invalidate();
            return;
        }
        animate().cancel();
        if (android.os.Build.VERSION.SDK_INT >= 12) {
            android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(progress, target);
            animator.setDuration(140);
            animator.addUpdateListener(a -> {
                progress = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        } else {
            progress = target;
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) dp(48), (int) dp(28));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float radius = height / 2f;
        int trackColor = Ui_26_10_25.blend(Ui_26_10_25.LINE, Ui_26_10_25.GREEN, progress);
        paint.setColor(trackColor);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);

        float thumbRadius = radius - dp(3.5f);
        float left = radius;
        float right = width - radius;
        float cx = left + (right - left) * progress;
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx, height / 2f, thumbRadius, paint);
        paint.setColor(0x22000000);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(0.8f));
        canvas.drawCircle(cx, height / 2f, thumbRadius, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(android.view.MotionEvent event) {
        if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
            boolean next = !checked;
            if (listener != null) listener.onToggleRequested(next);
            else setChecked(next, true);
            performClick();
            return true;
        }
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private float dp(float value) {
        return value * density;
    }
}
