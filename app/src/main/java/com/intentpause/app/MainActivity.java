package com.intentpause.app;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/** 三个 Tab 的容器：首页 / 记录 / 我的。 */
public class MainActivity extends AppCompatActivity {
    private FrameLayout content;
    private LinearLayout navBar;
    private final List<View> navItems = new ArrayList<>();
    private HomeTab_26_10_25 home;
    private RecordsTab_26_10_25 records;
    private MineTab_26_10_25 mine;
    private int currentTab = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Ui_26_10_25.applyWindow(this);
        Notify_26_10_25.ensureChannel(this);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Ui_26_10_25.PAPER);
        LinearLayout column = Ui_26_10_25.vertical(this);
        column.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        content = new FrameLayout(this);
        column.addView(content, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        column.addView(Ui_26_10_25.divider(this));
        navBar = buildNav();
        column.addView(navBar, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui_26_10_25.dp(this, 58)));

        root.addView(column);
        setContentView(root);
        Ui_26_10_25.applyInsets(root);
        showTab(0);
        if (savedInstanceState == null && !Perms_26_10_25.accessibilityEnabled(this)) {
            Ui_26_10_25.toast(this, "先开启无障碍服务，慢一拍才能在你打开应用时停一下");
            root.postDelayed(() -> {
                if (!Perms_26_10_25.accessibilityEnabled(this)) {
                    Perms_26_10_25.openAccessibilitySettings(this);
                }
            }, 1200);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrent();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mine != null) mine.refresh();
    }

    private LinearLayout buildNav() {
        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setBackgroundColor(Ui_26_10_25.PAPER);
        bar.addView(navItem("首页", R.drawable.ic_nav_home, 0), navWeight());
        bar.addView(navItem("记录", R.drawable.ic_nav_records, 1), navWeight());
        bar.addView(navItem("我的", R.drawable.ic_nav_mine, 2), navWeight());
        return bar;
    }

    private LinearLayout.LayoutParams navWeight() {
        return new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
    }

    private View navItem(String label, int iconRes, int index) {
        LinearLayout item = Ui_26_10_25.vertical(this);
        item.setGravity(Gravity.CENTER);
        ImageView icon = new ImageView(this);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Ui_26_10_25.MUTED);
        item.addView(icon, new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(this, 22), Ui_26_10_25.dp(this, 22)));
        TextView text = Ui_26_10_25.text(this, label, 11, Ui_26_10_25.MUTED, false);
        text.setGravity(Gravity.CENTER);
        text.setPadding(0, Ui_26_10_25.dp(this, 4), 0, 0);
        item.addView(text);
        item.setOnClickListener(v -> showTab(index));
        navItems.add(item);
        return item;
    }

    private void showTab(int index) {
        if (index == currentTab) {
            refreshCurrent();
            return;
        }
        currentTab = index;
        content.removeAllViews();
        View tab;
        if (index == 0) {
            if (home == null) home = new HomeTab_26_10_25(this);
            tab = home;
        } else if (index == 1) {
            if (records == null) records = new RecordsTab_26_10_25(this);
            tab = records;
        } else {
            if (mine == null) mine = new MineTab_26_10_25(this);
            tab = mine;
        }
        content.addView(tab, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        if (tab instanceof Refreshable_26_10_25) ((Refreshable_26_10_25) tab).refresh();
        updateNav();
    }

    private void refreshCurrent() {
        if (currentTab == 0 && home != null) home.refresh();
        else if (currentTab == 1 && records != null) records.refresh();
        else if (currentTab == 2 && mine != null) mine.refresh();
    }

    private void updateNav() {
        for (int i = 0; i < navItems.size(); i++) {
            LinearLayout item = (LinearLayout) navItems.get(i);
            boolean selected = i == currentTab;
            int color = selected ? Ui_26_10_25.GREEN : Ui_26_10_25.MUTED;
            ImageView icon = (ImageView) item.getChildAt(0);
            icon.setColorFilter(color);
            TextView text = (TextView) item.getChildAt(1);
            text.setTextColor(color);
            text.setTypeface(null, selected ? android.graphics.Typeface.BOLD : android.graphics.Typeface.NORMAL);
        }
    }
}
