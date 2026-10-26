package com.intentpause.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** 主动停柱状图：日 24 小时 / 周 7 天 / 月按天。 */
public class PeriodBarsView_26_10_26 extends View {
    public interface Listener {
        void onBar(int index);
    }

    private int[] values = new int[0];
    private String[] labels = new String[0];
    private int highlight = -1;
    private float appear = 1f;
    private Listener listener;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();

    public PeriodBarsView_26_10_26(Context context) {
        super(context);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setData(int[] values, String[] labels, int highlight, boolean animate) {
        this.values = values == null ? new int[0] : values;
        this.labels = labels == null ? new String[0] : labels;
        this.highlight = highlight;
        if (animate && !Ui_26_10_25.reduceMotion(getContext())) {
            appear = 0f;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(360);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(a -> {
                appear = (float) a.getAnimatedValue();
                invalidate();
            });
            animator.start();
        } else {
            appear = 1f;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0 || values.length == 0) return;
        int n = values.length;
        int max = 1;
        for (int v : values) max = Math.max(max, v);
        float left = dp(4);
        float right = w - dp(4);
        float bottom = h - dp(22);
        float top = dp(10);
        float gap = n > 16 ? dp(1.5f) : dp(5);
        float barW = Math.max(dp(2), (right - left - gap * (n - 1)) / n);
        paint.setTextSize(dp(9));
        paint.setColor(Ui_26_10_25.MUTED);
        paint.setTextAlign(Paint.Align.CENTER);
        int labelStep = n <= 8 ? 1 : n <= 14 ? 2 : n <= 24 ? 6 : Math.max(1, n / 6);
        for (int i = 0; i < n; i++) {
            float x = left + i * (barW + gap);
            float ratio = values[i] / (float) max;
            float barH = Math.max(values[i] == 0 ? dp(1) : dp(4), (bottom - top) * ratio * appear);
            paint.setColor(i == highlight ? Ui_26_10_25.SAGE : Ui_26_10_25.GREEN);
            if (i == highlight) paint.setColor(0xFF6F9C84);
            rect.set(x, bottom - barH, x + barW, bottom);
            canvas.drawRoundRect(rect, dp(3), dp(3), paint);
            if (i % labelStep == 0 && i < labels.length && labels[i] != null) {
                paint.setColor(Ui_26_10_25.MUTED);
                canvas.drawText(labels[i], x + barW / 2f, h - dp(6), paint);
            }
        }
        paint.setColor(Ui_26_10_25.LINE);
        canvas.drawRect(left, bottom + dp(1), right, bottom + dp(2), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || listener == null || values.length == 0) {
            return super.onTouchEvent(event);
        }
        float left = dp(4);
        float right = getWidth() - dp(4);
        int n = values.length;
        float gap = n > 16 ? dp(1.5f) : dp(5);
        float barW = Math.max(dp(2), (right - left - gap * (n - 1)) / n);
        int index = (int) ((event.getX() - left) / (barW + gap));
        if (index >= 0 && index < n) {
            listener.onBar(index);
            return true;
        }
        return super.onTouchEvent(event);
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
