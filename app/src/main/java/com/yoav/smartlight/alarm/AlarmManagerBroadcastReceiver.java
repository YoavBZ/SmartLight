package com.yoav.smartlight.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

/**
 * Created by Yoav on 7/31/2017.
 */
public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {

	private static final String TAG = "AlarmManagerBroadcastReceiver";
	public static final String ACTION_TURN_OFF_NOW = "com.yoav.smartlight.action.TURN_OFF_NOW";
	public static final String ACTION_TURN_ON_NOW = "com.yoav.smartlight.action.TURN_ON_NOW";
	private static final String DEFAULT_ACTION = "default";
	public static final String EXTRA_ALARM = "com.yoav.smartlight.extra.ALARM";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		//Acquire the lock
		wakeLock.acquire();

		byte[] alarmBytes = intent.getByteArrayExtra(EXTRA_ALARM);
		// Unmarshall the bytes into a parcel and create our Alarm with it.
		Alarm alarm = Alarm.unmarshall(alarmBytes, Alarm.CREATOR);
		if (alarm == null)
			throw new IllegalStateException("No alarm received");

		if (!alarm.isActivatedToday()) {
			wakeLock.release();
			return;
		}
		String action = intent.getAction();
		switch (action != null ? action : DEFAULT_ACTION) {
			case (DEFAULT_ACTION): {
				Intent alarmIntent = new Intent(context, AlarmService.class);
				alarmIntent.putExtra(EXTRA_ALARM, alarmBytes);
				context.startService(alarmIntent);
				break;
			}
			case (ACTION_TURN_OFF_NOW): {
				Intent dismissIntent = new Intent(context, AlarmManagerBroadcastReceiver.class);
				PendingIntent sender = PendingIntent.getBroadcast(context, alarm.id, dismissIntent, 0);
				sender.cancel();
				AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarmManager.cancel(sender);
				Intent turnOffIntent = new Intent(context, AlarmService.class);
				turnOffIntent.setAction("turnOff");
				context.startService(turnOffIntent);
				break;
			}
			case (ACTION_TURN_ON_NOW): {
				Intent dismissIntent = new Intent(context, AlarmManagerBroadcastReceiver.class);
				PendingIntent sender = PendingIntent.getBroadcast(context, alarm.id, dismissIntent, 0);
				sender.cancel();
				AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				alarmManager.cancel(sender);
				Intent turnOffIntent = new Intent(context, AlarmService.class);
				turnOffIntent.setAction("turnOn");
				context.startService(turnOffIntent);
				break;
			}
		}
		//Release the lock
		wakeLock.release();
	}
}
