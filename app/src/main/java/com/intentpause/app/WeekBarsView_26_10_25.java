package com.intentpause.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

/** 一周节奏：周一到周日的打开次数柱状图。 */
public class WeekBarsView_26_10_25 extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float density;
    private int[] values = new int[7];
    private int highlight = -1;

    public WeekBarsView_26_10_25(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT);
    }

    public void setData(int[] values, int highlightIndex) {
        this.values = values == null || values.length != 7 ? new int[7] : values;
        this.highlight = highlightIndex;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, (int) dp(150));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float bottom = height - dp(22);
        float top = dp(18);
        float slot = width / 7f;
        float barWidth = slot * 0.46f;
        int max = 1;
        for (int value : values) max = Math.max(max, value);

        paint.setColor(Ui_26_10_25.LINE);
        canvas.drawRect(0, bottom, width, bottom + dp(1), paint);

        String[] labels = {"一", "二", "三", "四", "五", "六", "日"};
        textPaint.setTextSize(dp(10));
        for (int i = 0; i < 7; i++) {
            float cx = slot * i + slot / 2f;
            int value = values[i];
            float barHeight = (bottom - top) * (value / (float) max);
            if (value > 0) barHeight = Math.max(barHeight, dp(3));
            float left = cx - barWidth / 2f;
            RectF rect = new RectF(left, bottom - barHeight, left + barWidth, bottom);
            paint.setColor(i == highlight ? Ui_26_10_25.CHART_GREEN : Ui_26_10_25.CHART_SAGE);
            if (value == 0) paint.setColor(Ui_26_10_25.LINE);
            canvas.drawRoundRect(rect, dp(4), dp(4), paint);

            textPaint.setColor(Ui_26_10_25.INK);
            canvas.drawText(String.valueOf(value), cx, bottom - barHeight - dp(5), textPaint);
            textPaint.setColor(Ui_26_10_25.MUTED);
            canvas.drawText(labels[i], cx, height - dp(6), textPaint);
        }
    }

    private float dp(float value) {
        return value * density;
    }
}
