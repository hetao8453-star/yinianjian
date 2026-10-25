package com.intentpause.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** 月历热力图：按当天打开次数着色，点某天切换日期。 */
public class HeatmapView_26_10_25 extends View {
    public interface Listener {
        void onDayClick(int year, int month, int day);
    }

    private static final int[] LEVEL_COLORS = {
            0xFFEFEEE8, 0xFFDDE9E0, 0xFFC0D6C8, 0xFF93B6A1, 0xFF628E76
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private int year;
    private int month;
    private int selectedDay = 1;
    private int daysInMonth = 31;
    private Map<String, Integer> counts = new HashMap<>();
    private Listener listener;
    private final RectF[] cellRects = new RectF[42];
    private final int[] cellDays = new int[42];
    private float gap;
    private float cellSize;
    private float headerHeight;

    public HeatmapView_26_10_25(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        gap = dp(3);
        headerHeight = dp(18);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT);
        setClickable(true);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setData(int year, int month, Map<String, Integer> counts, int selectedDay) {
        this.year = year;
        this.month = month;
        this.counts = counts == null ? new HashMap<>() : counts;
        this.selectedDay = selectedDay;
        requestLayout();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        cellSize = (width - gap * 6) / 7f;
        int height = (int) (headerHeight + (cellSize + gap) * 6 + dp(2));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (cellSize <= 0) return;
        String[] weekdays = {"一", "二", "三", "四", "五", "六", "日"};
        textPaint.setTextSize(dp(10));
        textPaint.setColor(Ui_26_10_25.MUTED);
        for (int i = 0; i < 7; i++) {
            float cx = i * (cellSize + gap) + cellSize / 2f;
            canvas.drawText(weekdays[i], cx, dp(12), textPaint);
        }

        Calendar calendar = Calendar.getInstance(Locale.CHINA);
        calendar.set(year, month, 1);
        int firstIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7; // Monday=0
        daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int max = 1;
        for (int value : counts.values()) max = Math.max(max, value);

        textPaint.setTextSize(dp(10));
        for (int i = 0; i < 42; i++) {
            int day = i - firstIndex + 1;
            cellDays[i] = day;
            RectF rect = new RectF(
                    (i % 7) * (cellSize + gap),
                    headerHeight + (i / 7) * (cellSize + gap),
                    (i % 7) * (cellSize + gap) + cellSize,
                    headerHeight + (i / 7) * (cellSize + gap) + cellSize);
            cellRects[i] = rect;
            if (day < 1 || day > daysInMonth) continue;
            String key = String.format(Locale.CHINA, "%04d-%02d-%02d", year, month + 1, day);
            int count = counts.getOrDefault(key, 0);
            int level = count == 0 ? 0 : StatsMath_26_10_25.clamp(
                    1 + (int) Math.round((count * 3.0) / Math.max(1, max - 1)), 1, 4);
            paint.setColor(LEVEL_COLORS[level]);
            canvas.drawRoundRect(rect, dp(7), dp(7), paint);

            textPaint.setColor(level >= 3 ? Color.WHITE : Ui_26_10_25.INK);
            canvas.drawText(String.valueOf(day), rect.centerX(),
                    rect.centerY() + dp(3.5f), textPaint);

            if (day == selectedDay) {
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(2));
                paint.setColor(Ui_26_10_25.GREEN);
                canvas.drawRoundRect(rect, dp(7), dp(7), paint);
                paint.setStyle(Paint.Style.FILL);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP) return true;
        float x = event.getX();
        float y = event.getY();
        for (int i = 0; i < cellRects.length; i++) {
            RectF rect = cellRects[i];
            if (rect == null || cellDays[i] < 1 || cellDays[i] > daysInMonth) continue;
            if (rect.contains(x, y) && listener != null) {
                listener.onDayClick(year, month, cellDays[i]);
                return true;
            }
        }
        return true;
    }

    private float dp(float value) {
        return value * density;
    }
}
