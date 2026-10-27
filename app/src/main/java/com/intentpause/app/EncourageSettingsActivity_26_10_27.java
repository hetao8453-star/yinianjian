package com.intentpause.app;

import android.os.Bundle;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/** 每个鼓励应用的设置页：先选何时出现，再写自己的话。 */
public final class EncourageSettingsActivity_26_10_27 extends AppCompatActivity {
    public static final String EXTRA_PACKAGE = "package";
    private String pkg;
    private Switch onOpen;
    private Switch during;
    private Switch preset;
    private EditText interval;
    private LinearLayout duringOptions;
    private LinearLayout phraseList;
    private RadioGroup order;
    private int randomId;
    private int sequentialId;
    private final List<EditText> phrases = new ArrayList<>();
    private final List<Button> intervalChips = new ArrayList<>();

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        pkg = getIntent().getStringExtra(EXTRA_PACKAGE);
        if (pkg == null || pkg.isEmpty()) { finish(); return; }
        Ui_26_10_25.applyWindow(this);
        LinearLayout root = Ui_26_10_25.vertical(this);
        root.setBackgroundColor(Ui_26_10_25.PAPER);
        LinearLayout header = Ui_26_10_25.horizontal(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(Ui_26_10_25.dp(this, 12), Ui_26_10_25.dp(this, 8),
                Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 8));
        header.addView(Ui_26_10_25.iconButton(this, R.drawable.ic_back, 48, this::finish));
        TextView title = Ui_26_10_25.text(this,
                Apps_26_10_25.label(this, pkg) + " · 鼓励", 20, Ui_26_10_25.INK, true);
        header.addView(title);
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout page = Ui_26_10_25.vertical(this);
        page.setPadding(Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 8),
                Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 28));
        scroll.addView(page);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout opening = Ui_26_10_25.card(this);
        opening.addView(Ui_26_10_25.text(this, "打开时", 18, Ui_26_10_25.INK, true));
        opening.addView(Ui_26_10_25.hint(this, "进入应用时，在底部轻轻出现一句。"));
        onOpen = option("给一句鼓励", Apps_26_10_25.encourageOnOpen(this, pkg));
        Ui_26_10_25.add(opening, onOpen, 8);
        Ui_26_10_25.add(page, opening, 0);

        LinearLayout ongoing = Ui_26_10_25.card(this);
        ongoing.addView(Ui_26_10_25.text(this, "持续使用时", 18,
                Ui_26_10_25.INK, true));
        ongoing.addView(Ui_26_10_25.hint(this,
                "只累计亮屏且应用在前台的时间，切走或锁屏时暂停。"));
        during = option("隔一段时间轻提醒", Apps_26_10_25.encourageDuring(this, pkg));
        Ui_26_10_25.add(ongoing, during, 8);
        duringOptions = Ui_26_10_25.vertical(this);
        Ui_26_10_25.add(duringOptions, GentleSheet_26_10_26.label(this, "间隔多久"), 8);
        LinearLayout quick = Ui_26_10_25.horizontal(this);
        for (int minutes : new int[]{10, 20, 30}) {
            Button chip = Ui_26_10_25.pill(this, minutes + "分钟", false);
            chip.setOnClickListener(v -> interval.setText(String.valueOf(minutes)));
            intervalChips.add(chip);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                    Ui_26_10_25.dp(this, 48), 1f);
            lp.leftMargin = Ui_26_10_25.dp(this, 4);
            quick.addView(chip, lp);
        }
        duringOptions.addView(quick);
        interval = GentleSheet_26_10_26.input(this,
                String.valueOf(Apps_26_10_25.encourageInterval(this, pkg)),
                "10–120 分钟", false);
        interval.setInputType(InputType.TYPE_CLASS_NUMBER);
        interval.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateIntervalChips(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        updateIntervalChips(interval.getText().toString());
        Ui_26_10_25.add(duringOptions, interval, 10);
        preset = option("轮换糯糯的内置短句",
                Apps_26_10_25.presetEnabled(this, pkg));
        Ui_26_10_25.add(duringOptions, preset, 10);
        TextView orderTitle = GentleSheet_26_10_26.label(this, "短句顺序");
        duringOptions.addView(orderTitle);
        order = new RadioGroup(this);
        order.setOrientation(RadioGroup.HORIZONTAL);
        randomId = View.generateViewId();
        sequentialId = View.generateViewId();
        RadioButton random = new RadioButton(this);
        random.setId(randomId);
        random.setText("随机");
        random.setButtonTintList(ColorStateList.valueOf(Ui_26_10_25.GREEN));
        RadioButton sequential = new RadioButton(this);
        sequential.setId(sequentialId);
        sequential.setText("按顺序");
        sequential.setButtonTintList(ColorStateList.valueOf(Ui_26_10_25.GREEN));
        order.addView(random);
        order.addView(sequential);
        order.check("sequential".equals(Apps_26_10_25.rotation(this, pkg))
                ? sequentialId : randomId);
        duringOptions.addView(order);
        ongoing.addView(duringOptions);
        duringOptions.setVisibility(during.isChecked() ? View.VISIBLE : View.GONE);
        during.setOnCheckedChangeListener((button, checked) ->
                duringOptions.setVisibility(checked ? View.VISIBLE : View.GONE));
        Ui_26_10_25.add(page, ongoing, 12);

        LinearLayout own = Ui_26_10_25.card(this);
        own.addView(Ui_26_10_25.text(this, "写给自己的话", 18,
                Ui_26_10_25.INK, true));
        own.addView(Ui_26_10_25.hint(this,
                "打开时优先用第一句；保留多句，使用中可选择轮换。"));
        phraseList = Ui_26_10_25.vertical(this);
        for (String phrase : Apps_26_10_25.customEncourageMessages(this, pkg))
            addPhrase(phrase);
        if (phrases.isEmpty()) addPhrase("");
        Ui_26_10_25.add(own, phraseList, 12);
        Button add = Ui_26_10_25.button(this, "＋ 添加一句", false);
        add.setOnClickListener(v -> addPhrase(""));
        Ui_26_10_25.add(own, add, 10);
        Button preview = Ui_26_10_25.button(this, "看看底部提示的效果", false);
        preview.setOnClickListener(v -> preview());
        Ui_26_10_25.add(own, preview, 10);
        Ui_26_10_25.add(page, own, 12);

        Button save = Ui_26_10_25.button(this, "保存鼓励方式", true);
        save.setOnClickListener(v -> save());
        LinearLayout.LayoutParams saveLp = new LinearLayout.LayoutParams(-1,
                Ui_26_10_25.dp(this, 52));
        saveLp.setMargins(Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 8),
                Ui_26_10_25.dp(this, 20), Ui_26_10_25.dp(this, 10));
        root.addView(save, saveLp);
        setContentView(root);
        Ui_26_10_25.applyInsets(root);
    }

    private Switch option(String label, boolean checked) {
        Switch control = new Switch(this);
        control.setText(label);
        control.setTextColor(Ui_26_10_25.INK);
        control.setTextSize(15);
        control.setChecked(checked);
        control.setThumbTintList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{Ui_26_10_25.GREEN, 0xFFDFE3DB}));
        control.setTrackTintList(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_checked}, new int[]{}},
                new int[]{0xFFB8D4C2, 0xFFC8CEC6}));
        control.setMinHeight(Ui_26_10_25.dp(this, 48));
        return control;
    }

    private void updateIntervalChips(String text) {
        String value = text.trim();
        int[] options = {10, 20, 30};
        for (int i = 0; i < intervalChips.size(); i++) {
            boolean selected = value.equals(String.valueOf(options[i]));
            Button chip = intervalChips.get(i);
            chip.setBackground(Ui_26_10_25.ripple(this,
                    selected ? Ui_26_10_25.GREEN : Ui_26_10_25.GREEN_SOFT, 999));
            chip.setTextColor(selected ? Color.WHITE : Ui_26_10_25.GREEN);
            chip.setSelected(selected);
        }
    }

    private void addPhrase(String value) {
        EditText field = GentleSheet_26_10_26.input(this, value,
                phrases.isEmpty() ? "例如：想读的话，就从这一页开始" : "再写一句", false);
        field.setSingleLine(false);
        field.setMinLines(1);
        LinearLayout row = Ui_26_10_25.horizontal(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.addView(field, new LinearLayout.LayoutParams(0, -2, 1f));
        Button remove = Ui_26_10_25.pill(this, "删除", false);
        remove.setContentDescription("删除这句鼓励");
        remove.setOnClickListener(v -> {
            phraseList.removeView(row);
            phrases.remove(field);
            if (phrases.isEmpty()) addPhrase("");
        });
        LinearLayout.LayoutParams removeLp = new LinearLayout.LayoutParams(
                Ui_26_10_25.dp(this, 72), Ui_26_10_25.dp(this, 48));
        removeLp.leftMargin = Ui_26_10_25.dp(this, 8);
        row.addView(remove, removeLp);
        phrases.add(field);
        Ui_26_10_25.add(phraseList, row, 8);
    }

    private void preview() {
        String sample = null;
        for (EditText field : phrases) {
            if (!field.getText().toString().trim().isEmpty()) {
                sample = field.getText().toString().trim();
                break;
            }
        }
        if (sample == null) sample = Apps_26_10_25.openingEncourageMessage(this, pkg);
        TextView banner = Ui_26_10_25.text(this, sample, 16, Ui_26_10_25.INK, false);
        banner.setPadding(Ui_26_10_25.dp(this, 18), Ui_26_10_25.dp(this, 16),
                Ui_26_10_25.dp(this, 18), Ui_26_10_25.dp(this, 16));
        banner.setBackground(Ui_26_10_25.round(this, Ui_26_10_25.GREEN_SOFT, 18));
        GentleSheet_26_10_26.show(this, "底部提示预览", banner, null, null);
    }

    private void save() {
        int minutes;
        try { minutes = Integer.parseInt(interval.getText().toString().trim()); }
        catch (Exception ignored) { minutes = 0; }
        if (during.isChecked() && (minutes < 10 || minutes > 120)) {
            interval.setError("请输入 10–120 分钟");
            interval.requestFocus();
            return;
        }
        if (minutes >= 10 && minutes <= 120)
            Apps_26_10_25.setEncourageInterval(this, pkg, minutes);
        Apps_26_10_25.setEncourageOnOpen(this, pkg, onOpen.isChecked());
        Apps_26_10_25.setEncourageDuring(this, pkg, during.isChecked());
        Apps_26_10_25.setPresetEnabled(this, pkg, preset.isChecked());
        Apps_26_10_25.setRotation(this, pkg,
                order.getCheckedRadioButtonId() == sequentialId ? "sequential" : "random");
        List<String> lines = new ArrayList<>();
        for (EditText field : phrases) {
            String line = field.getText().toString().trim();
            if (!line.isEmpty()) lines.add(line);
        }
        Apps_26_10_25.setCustomEncourageMessages(this, pkg, lines);
        Ui_26_10_25.toast(this, "鼓励方式已保存");
        finish();
    }
}
