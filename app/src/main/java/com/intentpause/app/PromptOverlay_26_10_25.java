package com.intentpause.app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** 由无障碍服务创建的确认覆盖层：单个窗口内换页，避免重建导致的滚动跳变。 */
public final class PromptOverlay_26_10_25 {
    public interface Listener {
        void onPurpose(String purpose);

        void onEmergency();

        void onDecline();

        void onDuration(int minutes, String detail);

        void onBack(String detail);

        void onUnsureRelax();

        void onContinue();

        void onChangePurpose();

        void onLeave();

        void onError(String reason);
    }

    private final Context context;
    private final Listener listener;
    private final WindowManager windows;
    private FrameLayout root;
    private ScrollView scroll;
    private LinearLayout page;
    private EditText detailInput;
    private String detail = "";

    public PromptOverlay_26_10_25(Context context, Listener listener) {
        this.context = context;
        this.listener = listener;
        this.windows = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public boolean isShowing() {
        return root != null;
    }

    public String currentDetail() {
        if (detailInput != null) return detailInput.getText().toString().trim();
        return detail == null ? "" : detail;
    }

    public void showPrompt(String pkg, String label, String savedDetail) {
        detailInput = null;
        detail = savedDetail == null ? "" : savedDetail;
        if (!ensureView()) return;
        LinearLayout content = Ui_26_10_25.vertical(context);
        content.addView(header(pkg, label));
        Ui_26_10_25.add(content, Ui_26_10_25.title(context, "这次打开，想做什么？"), 22);
        Ui_26_10_25.add(content, Ui_26_10_25.subtitle(context, "选一个目的，再选时长。想休息也可以。"), 10);

        LinearLayout rowA = new LinearLayout(context);
        rowA.setOrientation(LinearLayout.HORIZONTAL);
        rowA.addView(purposeCard("查找内容", () -> listener.onPurpose("查找内容")), weight(context));
        rowA.addView(purposeCard("回复消息", () -> listener.onPurpose("回复消息")), weight(context));
        Ui_26_10_25.add(content, rowA, 26);

        LinearLayout rowB = new LinearLayout(context);
        rowB.setOrientation(LinearLayout.HORIZONTAL);
        rowB.addView(purposeCard("放松一下", () -> listener.onPurpose("放松一下")), weight(context));
        rowB.addView(purposeCard("还没想清楚", () -> listener.onPurpose("还没想清楚")), weight(context));
        Ui_26_10_25.add(content, rowB, 12);

        Button emergency = Ui_26_10_25.ghost(context, "应急使用 3 分钟");
        emergency.setOnClickListener(v -> listener.onEmergency());
        Ui_26_10_25.add(content, emergency, 22);

        Button decline = Ui_26_10_25.ghost(context, "先不打开");
        decline.setOnClickListener(v -> listener.onDecline());
        Ui_26_10_25.add(content, decline, 4);

        TextView trust = Ui_26_10_25.text(context,
                "只在本机显示，不读取屏幕内容，也不上传。", 12, Ui_26_10_25.MUTED, false);
        Ui_26_10_25.add(content, trust, 18);
        showPage(content);
    }

    public void showDuration(String pkg, String label, String purpose, String savedDetail) {
        detail = savedDetail == null ? "" : savedDetail;
        if (!ensureView()) return;
        LinearLayout content = Ui_26_10_25.vertical(context);
        content.addView(backHeader(pkg, label, () -> {
            detail = currentDetail();
            listener.onBack(detail);
        }));
        Ui_26_10_25.add(content, Ui_26_10_25.title(context, "这次打算用多久？"), 20);
        Ui_26_10_25.add(content, Ui_26_10_25.subtitle(context,
                purpose == null ? "点一下就进入。" : "目的：" + purpose + "。点一下就进入。"), 10);

        Ui_26_10_25.add(content, Ui_26_10_25.text(context,
                "具体要做什么？可不填", 14, Ui_26_10_25.MUTED, false), 22);
        detailInput = new EditText(context);
        detailInput.setTextSize(15);
        detailInput.setTextColor(Ui_26_10_25.INK);
        detailInput.setHintTextColor(Ui_26_10_25.MUTED);
        detailInput.setHint("比如：查一个菜谱 / 回两条消息");
        detailInput.setSingleLine(true);
        detailInput.setInputType(InputType.TYPE_CLASS_TEXT);
        detailInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        detailInput.setBackground(Ui_26_10_25.roundStroke(context, Color.WHITE, Ui_26_10_25.LINE, 14));
        detailInput.setPadding(Ui_26_10_25.dp(context, 14), Ui_26_10_25.dp(context, 12),
                Ui_26_10_25.dp(context, 14), Ui_26_10_25.dp(context, 12));
        if (detail != null && !detail.isEmpty()) detailInput.setText(detail);
        Ui_26_10_25.add(content, detailInput, 8);

        Ui_26_10_25.add(content, Ui_26_10_25.text(context, "本次使用时长", 14,
                Ui_26_10_25.MUTED, false), 20);
        LinearLayout durations = new LinearLayout(context);
        durations.setOrientation(LinearLayout.HORIZONTAL);
        int[] options = {5, 10, 20};
        for (int minutes : options) {
            Button pill = Ui_26_10_25.pill(context, minutes + " 分钟", minutes == 10);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    Ui_26_10_25.dp(context, 48), 1f);
            lp.setMargins(Ui_26_10_25.dp(context, 3), 0, Ui_26_10_25.dp(context, 3), 0);
            pill.setLayoutParams(lp);
            final int chosen = minutes;
            pill.setOnClickListener(v -> {
                detail = currentDetail();
                listener.onDuration(chosen, detail);
            });
            durations.addView(pill);
        }
        Ui_26_10_25.add(content, durations, 8);

        Button decline = Ui_26_10_25.ghost(context, "先不打开");
        decline.setOnClickListener(v -> listener.onDecline());
        Ui_26_10_25.add(content, decline, 12);
        showPage(content);
    }

    public void showUnsure(String pkg, String label, int durationMinutes) {
        detailInput = null;
        if (!ensureView()) return;
        LinearLayout content = Ui_26_10_25.vertical(context);
        content.addView(backHeader(pkg, label, () -> listener.onBack(currentDetail())));
        Ui_26_10_25.add(content, Ui_26_10_25.title(context, "还没想清楚，也没关系"), 22);
        Ui_26_10_25.add(content, Ui_26_10_25.subtitle(context,
                "想休息也可以，主动选一下就好。"), 10);
        Button relax = Ui_26_10_25.button(context, "主动放松 " + durationMinutes + " 分钟", true);
        relax.setOnClickListener(v -> listener.onUnsureRelax());
        Ui_26_10_25.add(content, relax, 30);
        Button decline = Ui_26_10_25.button(context, "先不打开", false);
        decline.setOnClickListener(v -> listener.onDecline());
        Ui_26_10_25.add(content, decline, 12);
        showPage(content);
    }

    public void showReview(String pkg, String label, String purpose, String savedDetail,
                           boolean emergency, int elapsedMinutes, int plannedMinutes) {
        detailInput = null;
        detail = savedDetail == null ? "" : savedDetail;
        if (!ensureView()) return;
        LinearLayout content = Ui_26_10_25.vertical(context);
        content.addView(header(pkg, label));
        Ui_26_10_25.add(content, Ui_26_10_25.title(context,
                emergency ? "3 分钟到了" : "时间到了，现在想怎么选？"), 22);

        LinearLayout card = Ui_26_10_25.card(context);
        Ui_26_10_25.add(card, Ui_26_10_25.text(context, "刚才的目的", 13,
                Ui_26_10_25.MUTED, false), 0);
        String remembered = purpose == null ? "尚未选择" : purpose;
        if (detail != null && !detail.isEmpty()) remembered += "：" + detail;
        Ui_26_10_25.add(card, Ui_26_10_25.text(context, remembered, 17, Ui_26_10_25.INK, true), 8);
        Ui_26_10_25.add(card, Ui_26_10_25.hint(context,
                "本次已过去 " + elapsedMinutes + " 分钟。数据只记在本机。"), 10);
        Ui_26_10_25.add(content, card, 20);

        Button keep = Ui_26_10_25.button(context, "继续 " + plannedMinutes + " 分钟", true);
        keep.setOnClickListener(v -> listener.onContinue());
        Ui_26_10_25.add(content, keep, 26);

        Button change = Ui_26_10_25.button(context, "换个目的", false);
        change.setOnClickListener(v -> listener.onChangePurpose());
        Ui_26_10_25.add(content, change, 10);

        Button leave = Ui_26_10_25.button(context, "现在离开", false);
        leave.setOnClickListener(v -> listener.onLeave());
        Ui_26_10_25.add(content, leave, 10);
        showPage(content);
    }

    public void hide() {
        if (root == null) return;
        try {
            windows.removeView(root);
        } catch (RuntimeException ignored) {
        }
        root = null;
        scroll = null;
        page = null;
        detailInput = null;
    }

    private boolean ensureView() {
        if (root != null) return true;
        try {
            root = new FrameLayout(context);
            root.setBackgroundColor(Ui_26_10_25.PAPER);
            scroll = new ScrollView(context);
            scroll.setFillViewport(true);
            scroll.setClipToPadding(false);
            page = new LinearLayout(context);
            page.setOrientation(LinearLayout.VERTICAL);
            page.setPadding(Ui_26_10_25.dp(context, 24), Ui_26_10_25.dp(context, 18),
                    Ui_26_10_25.dp(context, 24), Ui_26_10_25.dp(context, 28));
            scroll.addView(page, new ScrollView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            root.addView(scroll, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                scroll.setPadding(0, 0, 0, ime.bottom);
                return insets;
            });
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                    PixelFormat.TRANSLUCENT);
            lp.gravity = Gravity.TOP;
            lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
            lp.setTitle("慢一拍");
            windows.addView(root, lp);
            ViewCompat.requestApplyInsets(root);
            return true;
        } catch (RuntimeException e) {
            root = null;
            listener.onError(e.getClass().getSimpleName());
            return false;
        }
    }

    private void showPage(View content) {
        if (page == null) return;
        page.removeAllViews();
        page.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        content.setAlpha(0f);
        content.setTranslationY(Ui_26_10_25.dp(context, 10));
        content.animate().alpha(1f).translationY(0f).setDuration(150).start();
        scroll.scrollTo(0, 0);
    }

    private View header(String pkg, String label) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = new ImageView(context);
        Drawable drawable = Apps_26_10_25.icon(context, pkg);
        if (drawable != null) icon.setImageDrawable(drawable);
        else icon.setImageResource(R.drawable.ic_launcher_foreground);
        row.addView(icon, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(context, 30), Ui_26_10_25.dp(context, 30)));
        TextView name = Ui_26_10_25.text(context, label == null ? "这个应用" : label,
                14, Ui_26_10_25.MUTED, false);
        name.setPadding(Ui_26_10_25.dp(context, 10), 0, 0, 0);
        row.addView(name);
        return row;
    }

    private View backHeader(String pkg, String label, Runnable onBack) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        ImageView back = new ImageView(context);
        back.setImageResource(R.drawable.ic_back);
        back.setColorFilter(Ui_26_10_25.INK);
        back.setPadding(0, Ui_26_10_25.dp(context, 6), Ui_26_10_25.dp(context, 10),
                Ui_26_10_25.dp(context, 6));
        back.setOnClickListener(v -> onBack.run());
        row.addView(back, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(context, 30), Ui_26_10_25.dp(context, 30)));
        ImageView icon = new ImageView(context);
        Drawable drawable = Apps_26_10_25.icon(context, pkg);
        if (drawable != null) icon.setImageDrawable(drawable);
        else icon.setImageResource(R.drawable.ic_launcher_foreground);
        row.addView(icon, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(context, 26), Ui_26_10_25.dp(context, 26)));
        TextView name = Ui_26_10_25.text(context, label == null ? "这个应用" : label,
                14, Ui_26_10_25.MUTED, false);
        name.setPadding(Ui_26_10_25.dp(context, 10), 0, 0, 0);
        row.addView(name);
        return row;
    }

    private View purposeCard(String text, Runnable onClick) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setBackground(Ui_26_10_25.rippleStroke(context, Color.WHITE, Ui_26_10_25.LINE, 18));
        card.setPadding(Ui_26_10_25.dp(context, 10), Ui_26_10_25.dp(context, 14),
                Ui_26_10_25.dp(context, 10), Ui_26_10_25.dp(context, 14));
        TextView label = Ui_26_10_25.text(context, text, 17, Ui_26_10_25.INK, true);
        label.setGravity(Gravity.CENTER);
        card.addView(label);
        card.setOnClickListener(v -> onClick.run());
        return card;
    }

    private LinearLayout.LayoutParams weight(Context context) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                Ui_26_10_25.dp(context, 96), 1f);
        lp.setMargins(Ui_26_10_25.dp(context, 4), 0, Ui_26_10_25.dp(context, 4), 0);
        return lp;
    }
}
