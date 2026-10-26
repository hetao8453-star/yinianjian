package com.intentpause.app;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** 每朵可点的小花对应一条会话；最多展示最近 8 条，避免触摸区域相叠。 */
public class GardenView_26_10_26 extends View {
    public interface MomentListener {
        void onMoment(IntentDb_26_10_25.GardenMoment moment);
    }

    public static final int MAX_VISIBLE = 8;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rect = new RectF();
    private final List<IntentDb_26_10_25.GardenMoment> moments = new ArrayList<>();
    private final List<RectF> hitAreas = new ArrayList<>();
    private Bitmap mascot;
    private ValueAnimator sway;
    private ValueAnimator entrance;
    private ValueAnimator mascotReturn;
    private ValueAnimator mascotReaction;
    private float phase;
    private float appear = 1f;
    private float mascotDx, mascotDy, reaction;
    private float downX, downY;
    private boolean pressedMascot, mascotDragging;
    private MomentListener listener;
    private int pressedIndex = -1;

    public GardenView_26_10_26(Context context) {
        super(context);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        try {
            mascot = BitmapFactory.decodeResource(getResources(),
                    R.drawable.mascot_nuonuo_cutout, options);
        } catch (RuntimeException ignored) {}
        setContentDescription("糯糯的花园，点按小花可看记录，糯糯可以拖动");
        setClickable(true);
    }

    public void setListener(MomentListener listener) { this.listener = listener; }

    public void setMoments(List<IntentDb_26_10_25.GardenMoment> latest, boolean animate) {
        moments.clear();
        if (latest != null) moments.addAll(latest.subList(0,
                Math.min(MAX_VISIBLE, latest.size())));
        if (entrance != null) entrance.cancel();
        if (animate && !Ui_26_10_25.reduceMotion(getContext())) {
            appear = 0f;
            entrance = ValueAnimator.ofFloat(0f, 1f);
            entrance.setDuration(520);
            entrance.setInterpolator(new DecelerateInterpolator());
            entrance.addUpdateListener(a -> {
                appear = (float) a.getAnimatedValue();
                invalidate();
            });
            entrance.start();
        } else {
            appear = 1f;
            invalidate();
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (Ui_26_10_25.reduceMotion(getContext())) return;
        sway = ValueAnimator.ofFloat(0f, (float) (Math.PI * 2));
        sway.setDuration(4800);
        sway.setRepeatCount(ValueAnimator.INFINITE);
        sway.addUpdateListener(a -> {
            phase = (float) a.getAnimatedValue();
            if (getVisibility() == VISIBLE) invalidate();
        });
        sway.start();
    }

    @Override protected void onDetachedFromWindow() {
        if (sway != null) sway.cancel();
        if (entrance != null) entrance.cancel();
        if (mascotReturn != null) mascotReturn.cancel();
        if (mascotReaction != null) mascotReaction.cancel();
        super.onDetachedFromWindow();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) return;
        boolean night = isNight();
        canvas.save();
        path.reset();
        path.addRoundRect(new RectF(0, 0, w, h), dp(18), dp(18), Path.Direction.CW);
        canvas.clipPath(path);
        paint.setShader(new LinearGradient(0, 0, 0, h,
                night ? 0xFFE4EAE5 : 0xFFF0F5EA,
                night ? 0xFFF5F0E6 : 0xFFF8F5E9, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        drawSky(canvas, w, h, night);
        drawGround(canvas, w, h);
        hitAreas.clear();
        float gardenWidth = w - dp(78);
        float cell = gardenWidth / 4f;
        for (int i = 0; i < moments.size(); i++) {
            int row = i / 4;
            int col = i % 4;
            float x = cell * (col + 0.5f) + dp(3);
            float y = h * 0.48f + row * dp(52);
            hitAreas.add(new RectF(x - dp(24), y - dp(26),
                    x + dp(24), y + dp(23)));
            drawFlower(canvas, x, y, i, moments.get(i).encouraged);
        }
        if (mascot != null) {
            float size = dp(70);
            float left = w - size - dp(3) + mascotDx;
            float top = h - size - dp(3) + mascotDy;
            canvas.save();
            float breath = Ui_26_10_25.reduceMotion(getContext())
                    ? 1f : 1f + (float) Math.sin(phase) * 0.015f;
            paint.setColor(0x44719472);
            canvas.drawOval(left + dp(14), top + size - dp(12),
                    left + size - dp(9), top + size - dp(3), paint);
            if (mascotDragging || reaction > 0f) {
                paint.setColor(0xFFDDD4A5);
                float spread = dp(4) * reaction;
                canvas.drawCircle(left + dp(5) - spread, top + dp(24), dp(2.2f), paint);
                canvas.drawCircle(left + size - dp(1) + spread, top + dp(15), dp(2.5f), paint);
                paint.setColor(0xFFB7CDAF);
                canvas.drawCircle(left + size - dp(7), top - dp(1) - spread, dp(2f), paint);
            }
            if (mascotDragging && !Ui_26_10_25.reduceMotion(getContext()))
                canvas.rotate(Math.max(-11f, Math.min(11f, mascotDx / dp(50) * 11f)),
                        left + size / 2f, top + size / 2f);
            canvas.scale(breath, breath, left + size / 2f, top + size);
            paint.setAlpha((int) (255 * appear));
            canvas.drawBitmap(mascot, null,
                    new RectF(left, top, left + size, top + size), paint);
            paint.setAlpha(255);
            canvas.restore();
        }
        canvas.restore();
    }

    private void drawSky(Canvas canvas, int w, int h, boolean night) {
        float x = w - dp(35);
        float y = dp(32);
        paint.setColor(night ? 0xFFF1DFC1 : 0xFFF3DFAF);
        canvas.drawCircle(x, y, dp(11), paint);
        if (night) {
            paint.setColor(0xFFE4EAE5);
            canvas.drawCircle(x + dp(5), y - dp(4), dp(9), paint);
            paint.setColor(0xFFA6BBAE);
            for (int i = 0; i < 4; i++)
                canvas.drawCircle(dp(23 + i * 52), dp(19 + (i % 2) * 15), dp(1.5f), paint);
        } else {
            paint.setColor(0x99D9BD82);
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4;
                float dx = (float) Math.cos(angle);
                float dy = (float) Math.sin(angle);
                canvas.drawLine(x + dx * dp(15), y + dy * dp(15),
                        x + dx * dp(19), y + dy * dp(19), paint);
            }
            paint.setColor(0xAAFFFFFF);
            drawCloud(canvas, dp(34), dp(30), 1f);
            drawCloud(canvas, w * .47f, dp(48), .72f);
        }
    }

    private void drawCloud(Canvas canvas, float x, float y, float scale) {
        canvas.save();
        canvas.scale(scale, scale, x, y);
        canvas.drawOval(x, y, x + dp(42), y + dp(10), paint);
        canvas.drawCircle(x + dp(13), y - dp(2), dp(9), paint);
        canvas.drawCircle(x + dp(25), y - dp(4), dp(11), paint);
        canvas.restore();
    }

    private void drawGround(Canvas canvas, int w, int h) {
        paint.setColor(0xFFDEE9DA);
        path.reset();
        path.moveTo(0, h * .55f);
        path.cubicTo(w * .25f, h * .48f, w * .6f, h * .60f, w, h * .49f);
        path.lineTo(w, h);
        path.lineTo(0, h);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(0x88F9F5E9);
        for (int i = 0; i < 5; i++) {
            float x = dp(19) + i * (w - dp(50)) / 5f;
            float y = h * .91f + (i % 2) * dp(5);
            canvas.drawCircle(x, y, dp(1.5f), paint);
        }
        paint.setColor(0xFFE8DEC6);
        path.reset();
        path.moveTo(0, h * .80f);
        path.cubicTo(w * .3f, h * .75f, w * .66f, h * .85f, w, h * .77f);
        path.lineTo(w, h);
        path.lineTo(0, h);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawFlower(Canvas canvas, float x, float y, int index, boolean encouraged) {
        canvas.save();
        float wiggle = Ui_26_10_25.reduceMotion(getContext()) ? 0f
                : (float) Math.sin(phase + index * 0.75f) * 2.6f;
        canvas.rotate(wiggle, x, y + dp(19));
        canvas.translate(0, (1f - appear) * dp(8));
        paint.setColor(0xFF789D7C);
        paint.setStrokeWidth(dp(1.8f));
        paint.setStyle(Paint.Style.STROKE);
        path.reset();
        path.moveTo(x, y - dp(1));
        path.quadTo(x + dp(2), y + dp(9), x, y + dp(20));
        canvas.drawPath(path, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF88AD8B);
        canvas.drawOval(x - dp(9), y + dp(7), x - dp(1), y + dp(12), paint);
        canvas.drawOval(x + dp(1), y + dp(10), x + dp(8), y + dp(14), paint);
        // 两种同尺寸的小花表示两类记录，花瓣数区分类型，不表示好坏或成长等级。
        int petals = encouraged ? 5 : 4;
        int flowerColor = encouraged ? 0xFFF1C6AF : 0xFFF7EAD2;
        float cy = y - dp(8);
        float radius = dp(encouraged ? 6.5f : 6.8f);
        paint.setColor(0x33758D70);
        canvas.drawCircle(x, cy + dp(1.5f), dp(12), paint);
        for (int petal = 0; petal < petals; petal++) {
            double angle = Math.PI * 2 * petal / petals - Math.PI / 2;
            float px = x + (float) Math.cos(angle) * dp(6.6f);
            float py = cy + (float) Math.sin(angle) * dp(6.6f);
            paint.setColor(flowerColor);
            canvas.drawCircle(px, py, radius, paint);
        }
        paint.setColor(encouraged ? 0xFFC78264 : 0xFFB08E54);
        canvas.drawCircle(x, cy, dp(3.6f), paint);
        paint.setColor(0x88FFFFFF);
        canvas.drawCircle(x - dp(1), cy - dp(1), dp(1), paint);
        paint.setAlpha(255);
        canvas.restore();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            pressedMascot = mascot != null && mascotArea().contains(downX, downY);
            mascotDragging = false;
            pressedIndex = -1;
            if (pressedMascot) {
                if (mascotReturn != null) mascotReturn.cancel();
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            }
            for (int i = 0; i < hitAreas.size() && i < moments.size(); i++) {
                if (hitAreas.get(i).contains(event.getX(), event.getY())) {
                    pressedIndex = i;
                    return true;
                }
            }
            return false;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = event.getX() - downX;
            float dy = event.getY() - downY;
            if (pressedMascot) {
                int slop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if (!mascotDragging && Math.hypot(dx, dy) > slop) {
                    mascotDragging = true;
                    if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                }
                if (mascotDragging) {
                    float baseX = getWidth() - dp(73);
                    float baseY = getHeight() - dp(73);
                    mascotDx = Math.max(-baseX, Math.min(dp(3), dx));
                    mascotDy = Math.max(-baseY, Math.min(dp(3), dy));
                    invalidate();
                }
                return true;
            }
            if (Math.hypot(dx, dy) > ViewConfiguration.get(getContext()).getScaledTouchSlop())
                pressedIndex = -1;
            return pressedIndex >= 0;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (pressedMascot) {
                if (mascotDragging) returnMascot();
                else reactMascot();
                pressedMascot = false;
                mascotDragging = false;
                performClick();
                return true;
            }
            int index = pressedIndex;
            pressedIndex = -1;
            if (index >= 0 && index < hitAreas.size()
                    && hitAreas.get(index).contains(event.getX(), event.getY())) {
                if (listener != null) listener.onMoment(moments.get(index));
                performClick();
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (pressedMascot) returnMascot();
            pressedMascot = false;
            mascotDragging = false;
            pressedIndex = -1;
            return true;
        }
        return pressedIndex >= 0 || pressedMascot;
    }

    private RectF mascotArea() {
        return new RectF(getWidth() - dp(82) + mascotDx,
                getHeight() - dp(82) + mascotDy,
                getWidth() + mascotDx, getHeight() + mascotDy);
    }

    private void returnMascot() {
        if (mascotReturn != null) mascotReturn.cancel();
        final float startX = mascotDx, startY = mascotDy;
        if (Ui_26_10_25.reduceMotion(getContext())) {
            mascotDx = mascotDy = 0f;
            invalidate();
            return;
        }
        mascotReturn = ValueAnimator.ofFloat(0f, 1f);
        mascotReturn.setDuration(430);
        mascotReturn.setInterpolator(new OvershootInterpolator(.45f));
        mascotReturn.addUpdateListener(a -> {
            float t = (float) a.getAnimatedValue();
            mascotDx = startX * (1f - t);
            mascotDy = startY * (1f - t);
            invalidate();
        });
        mascotReturn.start();
    }

    private void reactMascot() {
        if (Ui_26_10_25.reduceMotion(getContext())) return;
        if (mascotReaction != null) mascotReaction.cancel();
        mascotReaction = ValueAnimator.ofFloat(1f, 0f);
        mascotReaction.setDuration(650);
        mascotReaction.addUpdateListener(a -> {
            reaction = (float) a.getAnimatedValue();
            invalidate();
        });
        mascotReaction.start();
    }

    @Override public boolean performClick() {
        super.performClick();
        return true;
    }

    private boolean isNight() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour < 6 || hour >= 18;
    }

    private float dp(float value) {
        return Ui_26_10_25.dp(getContext(), value);
    }
}
