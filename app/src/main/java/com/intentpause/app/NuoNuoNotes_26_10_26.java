package com.intentpause.app;

import android.app.Activity;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

/** 用户主动点开的短文。使用中的鼓励仍只显示一句短提示。 */
public final class NuoNuoNotes_26_10_26 {
    private static final String[] TITLES = {
            "今天读到这里", "先从一点开始", "就是想放松一下"
    };
    private static final String[] BODIES = {
            "书可以明天接着读，这一页也不用急着读完。想继续，就再留一会儿；有点累了，合上也可以。这段时间原本就是你留给自己的，怎么安排，听自己的。",
            "打开了想用的应用，可以先做眼前这一点：翻一页书，听一段课，或者写下脑子里的一个念头。开始之后再看看，今天想做到哪里。糯糯已经找好旁边的位置，负责给你打气。",
            "今天就是想看点轻松的东西，那就给自己留一会儿。到约好的时间，再看看还想不想继续。想看就重新选一段时间，想停就先到这里。休息怎么安排，也听自己的。"
    };

    private NuoNuoNotes_26_10_26() {
    }

    public static void show(Activity activity, int index) {
        if (index < 0 || index >= TITLES.length) return;
        ScrollView scroll = new ScrollView(activity);
        TextView body = Ui_26_10_25.text(activity, BODIES[index], 16,
                Ui_26_10_25.INK, false);
        body.setLineSpacing(Ui_26_10_25.dp(activity, 5), 1f);
        body.setPadding(Ui_26_10_25.dp(activity, 22), Ui_26_10_25.dp(activity, 14),
                Ui_26_10_25.dp(activity, 22), Ui_26_10_25.dp(activity, 14));
        scroll.addView(body);
        new AlertDialog.Builder(activity)
                .setTitle(TITLES[index])
                .setView(scroll)
                .setPositiveButton("好", null)
                .show();
    }
}
