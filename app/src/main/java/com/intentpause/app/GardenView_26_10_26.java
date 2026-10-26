package com.intentpause.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/** 小缓的花园：受控尖叶 + 鼓励圆叶，最多画 24 片。 */
public class GardenView_26_10_26 extends View {
    private static final int MAX_LEAVES = 24;
    private int controlled;
    private int encouraged;
    private int total;
    private float appear = 1f;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF oval = new RectF();
    private Bitmap mascot;

    public GardenView_26_10_26(Context context) {
        super(context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        paint.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        try {
            mascot = BitmapFactory.decodeResource(getResources(), R.drawable.mascot_nuonuo_cutout);
        } catch (Exception ignored) {
        }
        setContentDescription("糯糯的花园");
    }

    public void setData(int controlled, int encouraged, int total, boolean animate) {
        this.controlled = Math.max(0, controlled);
        this.encouraged = Math.max(0, encouraged);
        this.total = Math.max(0, total);
        if (animate && !Ui_26_10_25.reduceMotion(getContext())) {
            appear = 0f;
            ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(420);
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
        if (w <= 0 || h <= 0) return;

        // 浅绿花床 + 暖沙土带
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x336F9C84);
        path.reset();
        path.moveTo(0, h * 0.46f);
        path.cubicTo(w * 0.25f, h * 0.40f, w * 0.72f, h * 0.50f, w, h * 0.43f);
        path.lineTo(w, h);
        path.lineTo(0, h);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(0x66DCC9A8);
        path.reset();
        path.moveTo(0, h * 0.78f);
        path.cubicTo(w * 0.30f, h * 0.70f, w * 0.70f, h * 0.86f, w, h * 0.76f);
        path.lineTo(w, h);
        path.lineTo(0, h);
        path.close();
        canvas.drawPath(path, paint);

        int drawCount = Math.min(total, MAX_LEAVES);
        if (drawCount > 0) {
            int controlledDraw = controlled;
            int encouragedDraw = encouraged;
            if (controlledDraw + encouragedDraw > drawCount) {
                float ratio = drawCount / (float) (controlledDraw + encouragedDraw);
                controlledDraw = Math.round(controlledDraw * ratio);
                encouragedDraw = Math.max(0, drawCount - controlledDraw);
            }
            int index = 0;
            for (int i = 0; i < controlledDraw; i++) {
                drawLeaf(canvas, w, h, index++, false, i);
            }
            for (int i = 0; i < encouragedDraw; i++) {
                drawLeaf(canvas, w, h, index++, true, i);
            }
        }

        if (mascot != null) {
            int size = dp(62);
            int left = w - size - dp(8);
            int top = h - size - dp(4);
            paint.setAlpha((int) (255 * appear));
            canvas.drawBitmap(mascot, null, new RectF(left, top, left + size, top + size), paint);
            paint.setAlpha(255);
        }
    }

    private void drawLeaf(Canvas canvas, int w, int h, int index, boolean round, int order) {
        int cols = 8;
        int col = index % cols;
        int row = index / cols;
        float cellW = (w - dp(22)) / (float) cols;
        float x = dp(12) + col * cellW + cellW / 2f + (row % 2 == 0 ? -dp(2) : dp(2));
        float y = h * 0.70f - row * dp(17) + (index % 3) * dp(2);
        if (appear < 1f && index == Math.min(total, MAX_LEAVES) - 1) {
            y += (1f - appear) * dp(8);
        }
        paint.setAlpha((int) (230 * appear));

        // 茎
        paint.setColor(0xFF6F9C84);
        paint.setStrokeWidth(dp(1.2f));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(x, y + dp(8), x, y + dp(16), paint);
        paint.setStyle(Paint.Style.FILL);

        if (round) {
            paint.setColor(0xFFA9C3B4);
            oval.set(x - dp(7), y - dp(6), x + dp(7), y + dp(6));
            canvas.drawOval(oval, paint);
            paint.setColor(0x556F9C84);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(1));
            canvas.drawOval(oval, paint);
            paint.setStyle(Paint.Style.FILL);
        } else {
            paint.setColor(order % 3 == 0 ? 0xFF4F7A5E : 0xFF5F8B70);
            path.reset();
            path.moveTo(x, y + dp(10));
            path.cubicTo(x - dp(8), y + dp(2), x - dp(6), y - dp(7), x, y - dp(10));
            path.cubicTo(x + dp(6), y - dp(7), x + dp(8), y + dp(2), x, y + dp(10));
            path.close();
            canvas.drawPath(path, paint);
        }
        paint.setAlpha(255);
    }

    private int dp(float v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
