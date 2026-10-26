package com.intentpause.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 每日总结的近似闹钟；到点发通知，并在下次启动时补看。 */
public class DailySummaryReceiver_26_10_26 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DailySummary_26_10_26.deliver(context);
        DailySummary_26_10_26.schedule(context);
    }
}
