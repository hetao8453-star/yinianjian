package com.intentpause.app;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private final List<ResolveInfo> apps = new ArrayList<>();
    private LinearLayout root;
    private TextView state;
    private LinearLayout appRows;
    private EditText search;
    private SharedPreferences prefs;
    private PackageManager pm;
    private int ink = Color.rgb(37,44,40);
    private int green = Color.rgb(54,94,75);
    private int pale = Color.rgb(231,239,233);
    private int paper = Color.rgb(247,246,242);

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("selected_apps", MODE_PRIVATE);
        pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN); intent.addCategory(Intent.CATEGORY_LAUNCHER);
        Set<String> seen = new HashSet<>();
        for (ResolveInfo app : pm.queryIntentActivities(intent,0)) {
            if (app.activityInfo == null) continue;
            String pkg = app.activityInfo.packageName;
            if (!pkg.equals(getPackageName()) && seen.add(pkg)) apps.add(app);
        }
        Collections.sort(apps, Comparator.comparing(x -> x.loadLabel(pm).toString()));
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(paper);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setFocusableInTouchMode(true); root.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        root.setPadding(dp(24),dp(42),dp(24),dp(36)); scroll.addView(root); scroll.setClipToPadding(false); setContentView(scroll);
        ViewCompat.setOnApplyWindowInsetsListener(scroll, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        add(root,text("把选择留给自己",28,true),0);
        add(root,text("选一些应用，在打开时停一下。你可以主动选择做事，也可以主动休息。",16,false),12);
        state = text("正在检查提醒状态…",16,true);
        state.setPadding(dp(18),dp(18),dp(18),dp(18)); state.setBackground(rounded(Color.WHITE,18));
        add(root,state,28);
        add(root,button("开启应用使用提醒",true,()->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))),14);
        add(root,text("仅识别所选应用是否进入前台；目的保留在当前会话，不读取屏幕文字，也不上传。开启后可随时在系统设置中关闭。",14,false),16);
        add(root,text("需要停一下的应用",22,true),32);
        search = new EditText(this); search.setSingleLine(true); search.setTextSize(16);
        search.setHint("搜索应用"); search.setBackground(rounded(Color.WHITE,14));
        search.setPadding(dp(16),dp(13),dp(16),dp(13)); add(root,search,16);
        appRows = new LinearLayout(this); appRows.setOrientation(LinearLayout.VERTICAL); add(root,appRows,12);
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after) { }
            @Override public void onTextChanged(CharSequence s,int start,int before,int count) { renderApps(); }
            @Override public void afterTextChanged(android.text.Editable e) { }
        });
        add(root,text("初版提示",18,true),30);
        add(root,text("默认 10 分钟后，如果你还在那个应用里，会再次询问。短暂切走不超过 1 分钟，原计时不变。",15,false),10);
        renderApps();
        root.requestFocus();
        scroll.post(() -> scroll.scrollTo(0, 0));
    }
    @Override protected void onResume() { super.onResume(); if (state != null) state.setText(serviceEnabled() ? "提醒已开启" : "提醒待开启：请在系统设置中允许“意图提醒 · 应用使用提示”"); }
    private boolean serviceEnabled() {
        AccessibilityManager manager = (AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
        if (manager == null) return false;
        for (AccessibilityServiceInfo info: manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            if (info.getResolveInfo()!=null && info.getResolveInfo().serviceInfo!=null &&
                getPackageName().equals(info.getResolveInfo().serviceInfo.packageName)) return true;
        }
        return false;
    }
    private void renderApps() {
        if (appRows == null || search == null) return;
        appRows.removeAllViews();
        String term = search.getText().toString().trim().toLowerCase(Locale.ROOT);
        int count = 0;
        for (ResolveInfo info : apps) {
            String title = info.loadLabel(pm).toString();
            String pkg = info.activityInfo.packageName;
            if (!term.isEmpty() && !title.toLowerCase(Locale.ROOT).contains(term) && !pkg.toLowerCase(Locale.ROOT).contains(term)) continue;
            if (++count > 100) break;
            LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(14),dp(8),dp(14),dp(8)); row.setBackground(rounded(Color.WHITE,14));
            ImageView icon = new ImageView(this); icon.setImageDrawable(info.loadIcon(pm));
            LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(34),dp(34)); row.addView(icon,ip);
            TextView name = text(title,16,false); name.setPadding(dp(12),0,0,0);
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0,dp(52),1); name.setGravity(Gravity.CENTER_VERTICAL); row.addView(name,np);
            CheckBox check = new CheckBox(this); check.setChecked(prefs.getBoolean(pkg,false));
            check.setContentDescription("监测"+title);
            check.setOnCheckedChangeListener((v,checked)->prefs.edit().putBoolean(pkg,checked).apply());
            row.addView(check,new LinearLayout.LayoutParams(dp(48),dp(48)));
            row.setOnClickListener(v->check.setChecked(!check.isChecked()));
            add(appRows,row,8);
        }
        if (count == 0) add(appRows,text("没有找到可启动的应用",15,false),12);
    }
    private int dp(float n) { return (int)(n*getResources().getDisplayMetrics().density+.5f); }
    private GradientDrawable rounded(int color,int radius) { GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d; }
    private TextView text(String value,int sp,boolean bold) { TextView v=new TextView(this);v.setText(value);v.setTextSize(sp);v.setTextColor(ink);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v; }
    private void add(LinearLayout box,View child,int top) { LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);p.topMargin=dp(top);box.addView(child,p); }
    private Button button(String value,boolean primary,Runnable action) { Button b=new Button(this);b.setText(value);b.setAllCaps(false);b.setTextSize(16);b.setMinHeight(dp(52));b.setTextColor(primary?Color.WHITE:ink);b.setBackground(rounded(primary?green:pale,14));b.setOnClickListener(v->action.run());return b; }
}
