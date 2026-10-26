package com.intentpause.app;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** 主动点开的小纸条；自写内容只保存在本机。 */
public final class NuoNuoNotes_26_10_26 {
    private static final String PREFS = "my_notes";
    private static final String KEY = "notes";
    private static final String[] TITLES = {
            "今天读到这里", "先从一点开始", "就是想放松一下"
    };
    private static final String[] BODIES = {
            "书可以明天接着读，这一页也不用急着读完。想继续，就再留一会儿；有点累了，合上也可以。这段时间原本就是你留给自己的，怎么安排，听自己的。",
            "打开了想用的应用，可以先做眼前这一点：翻一页书，听一段课，或者写下脑子里的一个念头。开始之后再看看，今天想做到哪里。糯糯已经找好旁边的位置，负责给你打气。",
            "今天就是想看点轻松的东西，那就给自己留一会儿。到约好的时间，再看看还想不想继续。想看就重新选一段时间，想停就先到这里。休息怎么安排，也听自己的。"
    };

    private NuoNuoNotes_26_10_26() {}

    public static void show(Activity activity, int index) {
        if (index < 0 || index >= TITLES.length) return;
        TextView body = Ui_26_10_25.text(activity, BODIES[index], 16,
                Ui_26_10_25.INK, false);
        body.setLineSpacing(Ui_26_10_25.dp(activity, 6), 1f);
        GentleSheet_26_10_26.show(activity, TITLES[index], body, null, null);
    }

    private static JSONArray load(Context context) {
        try {
            return new JSONArray(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(KEY, "[]"));
        } catch (JSONException error) {
            return new JSONArray();
        }
    }

    private static void save(Context context, JSONArray notes) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY, notes.toString()).apply();
    }

    public static void showMine(Activity activity) {
        JSONArray notes = load(activity);
        LinearLayout body = Ui_26_10_25.vertical(activity);
        body.addView(Ui_26_10_25.hint(activity,
                "写给自己的话只留在这台手机；想看时再打开。"));
        Dialog[] sheet = new Dialog[1];
        if (notes.length() == 0) {
            Ui_26_10_25.add(body, Ui_26_10_25.text(activity,
                    "还没有纸条，写一句也好。", 15, Ui_26_10_25.MUTED, false), 16);
        }
        for (int i = notes.length() - 1; i >= 0; i--) {
            final int index = i;
            JSONObject note = notes.optJSONObject(i);
            if (note == null) continue;
            LinearLayout card = Ui_26_10_25.card(activity);
            card.addView(Ui_26_10_25.text(activity, note.optString("title", "我的纸条"),
                    16, Ui_26_10_25.INK, true));
            String content = note.optString("body", "");
            Ui_26_10_25.add(card, Ui_26_10_25.hint(activity,
                    content.length() > 55 ? content.substring(0, 55) + "…" : content), 6);
            card.setOnClickListener(v -> {
                sheet[0].dismiss();
                edit(activity, index);
            });
            card.setContentDescription("编辑纸条，" + note.optString("title", "我的纸条"));
            Ui_26_10_25.add(body, card, 10);
        }
        TextView add = Ui_26_10_25.action(activity, "＋ 写一张纸条", () -> {
            sheet[0].dismiss();
            edit(activity, -1);
        });
        add.setGravity(Gravity.CENTER);
        add.setBackground(Ui_26_10_25.ripple(activity, Ui_26_10_25.GREEN_SOFT, 14));
        add.setMinHeight(Ui_26_10_25.dp(activity, 52));
        Ui_26_10_25.add(body, add, 16);
        sheet[0] = GentleSheet_26_10_26.show(activity, "我的纸条", body, null, null);
    }

    public static void edit(Activity activity, int index) {
        JSONArray notes = load(activity);
        JSONObject current = index >= 0 ? notes.optJSONObject(index) : null;
        LinearLayout body = Ui_26_10_25.vertical(activity);
        body.addView(GentleSheet_26_10_26.label(activity, "标题"));
        EditText title = GentleSheet_26_10_26.input(activity,
                current == null ? "" : current.optString("title"), "给它起个名字", false);
        body.addView(title);
        body.addView(GentleSheet_26_10_26.label(activity, "想写的话"));
        EditText text = GentleSheet_26_10_26.input(activity,
                current == null ? "" : current.optString("body"), "写给此刻的自己…", true);
        body.addView(text);
        Dialog[] sheet = new Dialog[1];
        if (current != null) {
            TextView delete = Ui_26_10_25.action(activity, "删除这张纸条", () -> {
                JSONArray next = new JSONArray();
                for (int i = 0; i < notes.length(); i++) if (i != index) next.put(notes.opt(i));
                save(activity, next);
                sheet[0].dismiss();
                Ui_26_10_25.toast(activity, "纸条已删除");
            });
            delete.setGravity(Gravity.CENTER);
            Ui_26_10_25.add(body, delete, 12);
        }
        sheet[0] = GentleSheet_26_10_26.show(activity,
                index < 0 ? "写一张纸条" : "编辑纸条", body, "保存", () -> {
                    String bodyText = text.getText().toString().trim();
                    if (bodyText.isEmpty()) {
                        text.setError("先写一句想留下的话");
                        text.requestFocus();
                        return false;
                    }
                    String heading = title.getText().toString().trim();
                    JSONObject changed = new JSONObject();
                    try {
                        changed.put("title", heading.isEmpty() ? "我的纸条" : heading);
                        changed.put("body", bodyText);
                        changed.put("updated", System.currentTimeMillis());
                        if (index >= 0) notes.put(index, changed);
                        else notes.put(changed);
                    } catch (JSONException ignored) {}
                    save(activity, notes);
                    Ui_26_10_25.toast(activity, "纸条已保存");
                    return true;
                });
    }
}
