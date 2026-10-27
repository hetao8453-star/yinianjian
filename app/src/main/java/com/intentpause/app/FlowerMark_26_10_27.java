package com.intentpause.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/** 图例里使用与花园相同语义的四瓣、五瓣小花，避免字体图标随系统变化。 */
public final class FlowerMark_26_10_27 extends View {
    private final boolean encouraged;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FlowerMark_26_10_27(Context context, boolean encouraged) {
        super(context);
        this.encouraged = encouraged;
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float radius = Math.min(getWidth(), getHeight()) * 0.22f;
        int petals = encouraged ? 5 : 4;
        paint.setColor(encouraged ? 0xFFF4BDA9 : 0xFF9FC9AD);
        for (int i = 0; i < petals; i++) {
            double angle = i * Math.PI * 2 / petals - Math.PI / 2;
            canvas.drawCircle(cx + (float) Math.cos(angle) * radius,
                    cy + (float) Math.sin(angle) * radius, radius * 0.74f, paint);
        }
        paint.setColor(encouraged ? 0xFFB87955 : 0xFF5D8B70);
        canvas.drawCircle(cx, cy, radius * 0.48f, paint);
    }
}
