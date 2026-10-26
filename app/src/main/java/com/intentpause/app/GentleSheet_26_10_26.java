package com.intentpause.app;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** 与主界面统一的底部编辑面板，内容与按钮均使用清晰的系统字体。 */
public final class GentleSheet_26_10_26 {
    public interface SaveAction { boolean save(); }

    private GentleSheet_26_10_26() {}

    public static Dialog show(Activity activity, String title, View content,
                              String saveLabel, SaveAction onSave) {
        Dialog dialog = new Dialog(activity);
        LinearLayout root = Ui_26_10_25.vertical(activity);
        root.setBackground(Ui_26_10_25.round(activity, Ui_26_10_25.CARD, 24));
        root.setPadding(Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 20),
                Ui_26_10_25.dp(activity, 20), Ui_26_10_25.dp(activity, 18));
        TextView heading = Ui_26_10_25.text(activity, title, 20, Ui_26_10_25.INK, true);
        root.addView(heading);
        ScrollView scroll = new MaxScroll(activity);
        scroll.setFillViewport(false);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.addView(content);
        LinearLayout.LayoutParams sc = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sc.topMargin = Ui_26_10_25.dp(activity, 16);
        root.addView(scroll, sc);
        LinearLayout actions = Ui_26_10_25.horizontal(activity);
        Button cancel = Ui_26_10_25.button(activity, "取消", false);
        cancel.setOnClickListener(v -> dialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(0,
                Ui_26_10_25.dp(activity, 52), 1f));
        if (saveLabel != null) {
            Button save = Ui_26_10_25.button(activity, saveLabel, true);
            save.setOnClickListener(v -> {
                if (onSave == null || onSave.save()) dialog.dismiss();
            });
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0,
                    Ui_26_10_25.dp(activity, 52), 1f);
            sp.leftMargin = Ui_26_10_25.dp(activity, 10);
            actions.addView(save, sp);
        }
        Ui_26_10_25.add(root, actions, 20);
        dialog.setContentView(root);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.gravity = Gravity.BOTTOM;
            attributes.dimAmount = 0.42f;
            window.setAttributes(attributes);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dialog.show();
        if (window != null) window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;
    }

    public static TextView label(Activity activity, String label) {
        TextView view = Ui_26_10_25.text(activity, label, 14, Ui_26_10_25.INK, true);
        view.setPadding(0, Ui_26_10_25.dp(activity, 16), 0,
                Ui_26_10_25.dp(activity, 7));
        return view;
    }

    public static EditText input(Activity activity, String value, String hint, boolean multiline) {
        EditText field = new EditText(activity);
        field.setText(value);
        field.setHint(hint);
        field.setTextColor(Ui_26_10_25.INK);
        field.setHintTextColor(Ui_26_10_25.MUTED);
        field.setTypeface(Typeface.SANS_SERIF);
        field.setTextSize(16);
        field.setSingleLine(!multiline);
        if (multiline) field.setMinLines(3);
        field.setGravity(multiline ? Gravity.TOP : Gravity.CENTER_VERTICAL);
        field.setPadding(Ui_26_10_25.dp(activity, 14),
                Ui_26_10_25.dp(activity, 12), Ui_26_10_25.dp(activity, 14),
                Ui_26_10_25.dp(activity, 12));
        field.setBackground(Ui_26_10_25.roundStroke(activity, Color.WHITE,
                Ui_26_10_25.LINE, 14));
        return field;
    }

    private static final class MaxScroll extends ScrollView {
        MaxScroll(Activity activity) { super(activity); }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int max = (int) (getResources().getDisplayMetrics().heightPixels * 0.68f);
            super.onMeasure(widthMeasureSpec,
                    View.MeasureSpec.makeMeasureSpec(max, View.MeasureSpec.AT_MOST));
        }
    }
}
