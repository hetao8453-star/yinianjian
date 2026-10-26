package com.intentpause.app;

import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** 独立的应用选择页：搜索、已选优先、分批展开。 */
public class AppPickerActivity_26_10_25 extends AppCompatActivity {
    private static final int PAGE_SIZE = 30;

    private LinearLayout listContainer;
    private EditText search;
    private TextView selectedCount;
    private ScrollView scroll;
    private final List<ResolveInfo> all = new ArrayList<>();
    private final List<ResolveInfo> filtered = new ArrayList<>();
    private String term = "";
    private int shown;
    private boolean sensitiveMode;
    private boolean encourageMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui_26_10_25.applyWindow(this);

        LinearLayout root = Ui_26_10_25.vertical(this);
        root.setBackgroundColor(Ui_26_10_25.PAPER);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        root.setLayoutParams(rootParams);

        sensitiveMode = "sensitive".equals(getIntent().getStringExtra("mode"));
        encourageMode = "encourage".equals(getIntent().getStringExtra("mode"));
        LinearLayout header = Ui_26_10_25.horizontal(this);
        header.setPadding(Ui_26_10_25.dp(this, 12), Ui_26_10_25.dp(this, 8),
                Ui_26_10_25.dp(this, 12), Ui_26_10_25.dp(this, 6));
        header.addView(Ui_26_10_25.iconButton(this, R.drawable.ic_back, 44, this::finish));
        header.addView(Ui_26_10_25.text(this,
                        sensitiveMode ? "敏感应用" : encourageMode ? "鼓励应用" : "选择应用",
                        19, Ui_26_10_25.INK, true),
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        selectedCount = Ui_26_10_25.text(this, "", 12, Ui_26_10_25.MUTED, false);
        selectedCount.setPadding(0, 0, Ui_26_10_25.dp(this, 10), 0);
        header.addView(selectedCount);
        TextView done = Ui_26_10_25.action(this, "完成", this::finish);
        header.addView(done);
        root.addView(header);

        if (sensitiveMode || encourageMode) {
            TextView note = Ui_26_10_25.hint(this, sensitiveMode
                    ? "这些应用打开时不会停一下，也不记录。适合银行、密码、支付类。"
                    : "这些应用打开时不限制，只在打开时和使用中给一句轻鼓励。");
            note.setPadding(Ui_26_10_25.dp(this, 16), 0, Ui_26_10_25.dp(this, 16),
                    Ui_26_10_25.dp(this, 8));
            root.addView(note);
        }

        LinearLayout searchRow = Ui_26_10_25.horizontal(this);
        searchRow.setBackground(Ui_26_10_25.roundStroke(this, Color.WHITE, Ui_26_10_25.LINE, 14));
        searchRow.setPadding(Ui_26_10_25.dp(this, 12), 0, Ui_26_10_25.dp(this, 8), 0);
        View searchIcon = new ImageView(this);
        ((ImageView) searchIcon).setImageResource(R.drawable.ic_search);
        ((ImageView) searchIcon).setColorFilter(Ui_26_10_25.MUTED);
        searchRow.addView(searchIcon, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 20)));
        search = new EditText(this);
        search.setHint("搜索应用名或包名");
        search.setHintTextColor(Ui_26_10_25.MUTED);
        search.setTextColor(Ui_26_10_25.INK);
        search.setTextSize(15);
        search.setSingleLine(true);
        search.setBackgroundColor(Color.TRANSPARENT);
        search.setPadding(Ui_26_10_25.dp(this, 10), 0, 0, 0);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                term = s.toString().trim().toLowerCase(Locale.ROOT);
                filter();
                render(true);
            }
        });
        searchRow.addView(search, new LinearLayout.LayoutParams(0,
                Ui_26_10_25.dp(this, 52), 1f));
        TextView clear = Ui_26_10_25.action(this, "清除", () -> search.setText(""));
        searchRow.addView(clear);
        LinearLayout.LayoutParams srp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(this, 52));
        srp.setMargins(Ui_26_10_25.dp(this, 16), 0, Ui_26_10_25.dp(this, 16), 0);
        root.addView(searchRow, srp);

        scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        listContainer = Ui_26_10_25.vertical(this);
        listContainer.setPadding(Ui_26_10_25.dp(this, 16), Ui_26_10_25.dp(this, 12),
                Ui_26_10_25.dp(this, 16), Ui_26_10_25.dp(this, 32));
        scroll.addView(listContainer, new ScrollView.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);
        Ui_26_10_25.applyInsets(root);

        all.addAll(Apps_26_10_25.launcherApps(this));
        filter();
        render(true);
    }

    private void filter() {
        filtered.clear();
        List<ResolveInfo> selected = new ArrayList<>();
        List<ResolveInfo> others = new ArrayList<>();
        for (ResolveInfo info : all) {
            String pkg = info.activityInfo.packageName;
            String label = info.loadLabel(getPackageManager()).toString();
            if (!term.isEmpty() && !label.toLowerCase(Locale.ROOT).contains(term)
                    && !pkg.toLowerCase(Locale.ROOT).contains(term)) continue;
            if (isSelected(pkg)) selected.add(info);
            else others.add(info);
        }
        filtered.addAll(selected);
        filtered.addAll(others);
    }

    private void render(boolean reset) {
        if (reset) {
            listContainer.removeAllViews();
            shown = 0;
        }
        int limit = Math.min(filtered.size(), shown + PAGE_SIZE);
        for (int i = shown; i < limit; i++) {
            listContainer.addView(appRow(filtered.get(i)), rowParams());
        }
        shown = limit;
        if (shown < filtered.size()) {
            TextView more = Ui_26_10_25.action(this,
                    "加载更多（还有 " + (filtered.size() - shown) + " 个）", () -> render(false));
            more.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(this, 48));
            lp.topMargin = Ui_26_10_25.dp(this, 10);
            listContainer.addView(more, lp);
        }
        if (filtered.isEmpty()) {
            listContainer.addView(Ui_26_10_25.hint(this, "没有找到匹配的应用"));
        }
        updateCount();
    }

    private LinearLayout.LayoutParams rowParams() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(this, 58));
        lp.topMargin = Ui_26_10_25.dp(this, 8);
        return lp;
    }

    private View appRow(ResolveInfo info) {
        String pkg = info.activityInfo.packageName;
        String label = info.loadLabel(getPackageManager()).toString();
        LinearLayout row = Ui_26_10_25.horizontal(this);
        row.setBackground(Ui_26_10_25.rippleStroke(this, Color.WHITE, Ui_26_10_25.LINE, 16));
        row.setPadding(Ui_26_10_25.dp(this, 14), 0, Ui_26_10_25.dp(this, 8), 0);
        ImageView icon = new ImageView(this);
        icon.setImageDrawable(info.loadIcon(getPackageManager()));
        row.addView(icon, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(this, 32), Ui_26_10_25.dp(this, 32)));
        LinearLayout box = Ui_26_10_25.vertical(this);
        box.setPadding(Ui_26_10_25.dp(this, 12), 0, 0, 0);
        box.addView(Ui_26_10_25.text(this, label, 15, Ui_26_10_25.INK, true));
        box.addView(Ui_26_10_25.text(this, pkg, 11, Ui_26_10_25.MUTED, false));
        row.addView(box, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        CheckBox check = new CheckBox(this);
        check.setChecked(isSelected(pkg));
        check.setContentDescription((sensitiveMode ? "标记" : encourageMode ? "鼓励" : "监测") + label);
        check.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setSelected(pkg, isChecked);
            if (sensitiveMode && isChecked) askDeleteHistory(pkg, label);
            updateCount();
        });
        row.addView(check, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(this, 48), Ui_26_10_25.dp(this, 48)));
        row.setOnClickListener(v -> check.setChecked(!check.isChecked()));
        return row;
    }

    private void askDeleteHistory(String pkg, String label) {
        new AlertDialog.Builder(this)
                .setTitle("删除“" + label + "”的历史记录？")
                .setMessage("标记为敏感应用后，之后打开它不会弹确认页，也不会记录。此前的记录要一起删除吗？")
                .setNegativeButton("保留历史", null)
                .setPositiveButton("删除历史", (dialog, which) ->
                        IntentDb_26_10_25.get(this).deleteAppRecords(pkg))
                .show();
    }

    private boolean isSelected(String pkg) {
        if (sensitiveMode) return Apps_26_10_25.sensitive(this, pkg);
        if (encourageMode) return Apps_26_10_25.encouraged(this, pkg);
        return Apps_26_10_25.selected(this, pkg);
    }

    private void setSelected(String pkg, boolean on) {
        if (sensitiveMode) {
            Apps_26_10_25.setSensitive(this, pkg, on);
            if (on) {
                Apps_26_10_25.setSelected(this, pkg, false);
                Apps_26_10_25.setEncouraged(this, pkg, false);
            }
        } else if (encourageMode) {
            Apps_26_10_25.setEncouraged(this, pkg, on);
            if (on) {
                Apps_26_10_25.setSelected(this, pkg, false);
                Apps_26_10_25.setSensitive(this, pkg, false);
            }
        } else {
            Apps_26_10_25.setSelected(this, pkg, on);
            if (on) {
                Apps_26_10_25.setEncouraged(this, pkg, false);
                Apps_26_10_25.setSensitive(this, pkg, false);
            }
        }
    }

    private void updateCount() {
        int count = sensitiveMode ? Apps_26_10_25.sensitivePackages(this).size()
                : encourageMode ? Apps_26_10_25.encouragePackages(this).size()
                : Apps_26_10_25.selectedPackages(this).size();
        selectedCount.setText((sensitiveMode ? "已标记 " : encourageMode ? "已鼓励 " : "已选 ")
                + count + " 个");
    }
}
