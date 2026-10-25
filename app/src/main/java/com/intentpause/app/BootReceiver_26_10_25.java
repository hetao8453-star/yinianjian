package com.intentpause.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** 开机后清理失效的计时快照，并在无障碍未开启时提醒一次。 */
public class BootReceiver_26_10_25 extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        long now = System.currentTimeMillis();
        IntentDb_26_10_25 db = IntentDb_26_10_25.get(context);
        db.serviceEvent("boot", "开机完成", now);

        SessionState_26_10_25.Snapshot snapshot = SessionState_26_10_25.load(context);
        if (snapshot.active()) {
            db.markEnded(snapshot.sessionId, "interrupted", 0, now);
            db.event(snapshot.sessionId, snapshot.pkg, "interrupted", snapshot.purpose,
                    snapshot.detail, "reboot", now);
            SessionState_26_10_25.clear(context);
        }

        if (!Perms_26_10_25.accessibilityEnabled(context)) {
            Notify_26_10_25.bootCheck(context);
        }
    }
}
