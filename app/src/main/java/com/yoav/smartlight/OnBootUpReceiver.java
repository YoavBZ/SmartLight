package com.yoav.smartlight;

/**
 * Created by Yoav on 8/12/2017.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class OnBootUpReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// Note that this will be called when the device boots up, not when the app first launches.
		// We may have a lot of alarms to reschedule, so do this in the background using an IntentService.
		context.startService(new Intent(context, OnBootUpAlarmScheduler.class));
	}
}
