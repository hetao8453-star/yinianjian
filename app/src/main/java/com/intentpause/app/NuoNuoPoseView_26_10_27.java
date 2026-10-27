package com.intentpause.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.appcompat.widget.AppCompatImageView;

/** 四格角色状态：安静、抬头、眨眼、被摸头。仅在交互时短暂切换。 */
public final class NuoNuoPoseView_26_10_27 extends AppCompatImageView {
    private static Bitmap cachedSheet;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Bitmap sheet;
    private int pose;
    private final Runnable idle = () -> { pose = 0; invalidate(); };

    public NuoNuoPoseView_26_10_27(Context context) {
        super(context);
        if (cachedSheet == null || cachedSheet.isRecycled())
            cachedSheet = BitmapFactory.decodeResource(getResources(),
                    R.drawable.mascot_nuonuo_poses_26_10_27);
        sheet = cachedSheet;
        setContentDescription("糯糯，轻点或长按可以互动，也可以拖动");
    }

    public void react(int nextPose) {
        removeCallbacks(idle);
        pose = Math.max(0, Math.min(3, nextPose));
        invalidate();
        postDelayed(idle, Ui_26_10_25.reduceMotion(getContext()) ? 300 : 850);
    }

    @Override protected void onDraw(Canvas canvas) {
        if (sheet == null) {
            super.onDraw(canvas);
            return;
        }
        int cellW = sheet.getWidth() / 2;
        int cellH = sheet.getHeight() / 2;
        int col = pose % 2;
        int row = pose / 2;
        canvas.drawBitmap(sheet,
                new Rect(col * cellW, row * cellH,
                        (col + 1) * cellW, (row + 1) * cellH),
                new RectF(0, 0, getWidth(), getHeight()), paint);
    }

    @Override protected void onDetachedFromWindow() {
        removeCallbacks(idle);
        super.onDetachedFromWindow();
    }
}
