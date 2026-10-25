package com.intentpause.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/** 环形时间占比图。 */
public class RingChart_26_10_25 extends View {
    public static class Slice {
        public String label;
        public long ms;
        public int color;

        public Slice(String label, long ms, int color) {
            this.label = label;
            this.ms = ms;
            this.color = color;
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private final List<Slice> slices = new ArrayList<>();

    public RingChart_26_10_25(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setData(List<Slice> data) {
        slices.clear();
        if (data != null) slices.addAll(data);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, Math.min(width, (int) dp(220)));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float size = Math.min(getWidth(), getHeight());
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float stroke = dp(22);
        float radius = size / 2f - stroke;
        RectF arc = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);

        long total = 0;
        for (Slice slice : slices) total += Math.max(0, slice.ms);
        paint.setStrokeWidth(stroke);
        paint.setStrokeCap(Paint.Cap.BUTT);
        if (total <= 0) {
            paint.setColor(Ui_26_10_25.LINE);
            canvas.drawArc(arc, 0, 360, false, paint);
        } else {
            float start = -90;
            float gapDegrees = slices.size() > 1 ? 1.6f : 0f;
            for (Slice slice : slices) {
                float sweep = slice.ms * 360f / total;
                if (sweep <= 0) continue;
                paint.setColor(slice.color);
                canvas.drawArc(arc, start + gapDegrees / 2f, Math.max(1f, sweep - gapDegrees),
                        false, paint);
                start += sweep;
            }
        }

        textPaint.setColor(Ui_26_10_25.INK);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(dp(18));
        canvas.drawText(StatsMath_26_10_25.duration(total), cx, cy + dp(2), textPaint);
        textPaint.setColor(Ui_26_10_25.MUTED);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(dp(11));
        canvas.drawText("总时长", cx, cy + dp(18), textPaint);
    }

    private float dp(float value) {
        return value * density;
    }

    public static int paletteAt(int index) {
        int[] colors = {
                Ui_26_10_25.CHART_GREEN, Ui_26_10_25.CHART_SAGE, Ui_26_10_25.CHART_MIST,
                Ui_26_10_25.CHART_SAND, Ui_26_10_25.CHART_CLAY, Ui_26_10_25.LINE
        };
        return colors[Math.max(0, index) % colors.length];
    }
}
