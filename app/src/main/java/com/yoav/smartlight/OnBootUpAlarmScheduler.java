package com.yoav.smartlight;

/**
 * Created by Yoav on 8/12/2017.
 */

import android.app.IntentService;
import android.content.Intent;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a emberlightService on a separate handler thread.
 */
public class OnBootUpAlarmScheduler extends IntentService {

	public OnBootUpAlarmScheduler() {
		super("OnBootUpAlarmScheduler");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			// TODO: Reschedule all enabled alarms
		}
	}
}
