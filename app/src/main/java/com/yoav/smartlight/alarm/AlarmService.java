package com.yoav.smartlight.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import com.yoav.smartlight.MainActivity;
import com.yoav.smartlight.R;
import com.yoav.smartlight.emberlight.EmberlightService;
import org.greenrobot.eventbus.EventBus;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;

import static com.yoav.smartlight.alarm.AlarmManagerBroadcastReceiver.*;

/**
 * Created by Yoav on 8/15/2017.
 */

public class AlarmService extends Service {

	private CountDownTimer notificationTimer;
	private CountDownTimer emberlightTimer;
	private NotificationManager notificationManager;
	private int alarmId;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final Context context = getApplicationContext();
		final EmberlightService emberlight = new EmberlightService(context);

		if (intent.getAction() != null) {
			// When clicking the notification button
			switch (intent.getAction()) {
				case "turnOn":
					emberlight.dimLights(100);
					break;
				case "turnOff":
					emberlight.turnLightOff();
					break;
			}
			stopSelf();
			return START_NOT_STICKY;
		}

		byte[] alarmBytes = intent.getByteArrayExtra(EXTRA_ALARM);
		// Unmarshall the bytes into a parcel and create our Alarm with it.
		final Alarm alarm = Alarm.unmarshall(alarmBytes, Alarm.CREATOR);
		if (alarm == null)
			throw new IllegalStateException("No alarm received");

		alarmId = alarm.id;
		notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		notificationTimer = new CountDownTimer(alarm.dimmingPeriod * 60000, 1000) {

			Intent turnOffIntent = new Intent(context, AlarmManagerBroadcastReceiver.class).setAction(ACTION_TURN_OFF_NOW)
					.putExtra(EXTRA_ALARM, Alarm.marshall(alarm));
			PendingIntent piTurnOff = PendingIntent.getBroadcast(context, alarmId, turnOffIntent, PendingIntent.FLAG_CANCEL_CURRENT);
			Intent turnOnIntent = new Intent(context, AlarmManagerBroadcastReceiver.class).setAction(ACTION_TURN_ON_NOW)
					.putExtra(EXTRA_ALARM, Alarm.marshall(alarm));
			PendingIntent piTurnOn = PendingIntent.getBroadcast(context, alarmId, turnOnIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			@Override
			public void onTick(final long millisUntilFinished) {
				String duration = new PeriodFormatterBuilder().minimumPrintedDigits(2)
						.printZeroAlways()
						.appendMinutes()
						.appendSeparator(":")
						.appendSeconds()
						.toFormatter()
						.print(new Period(millisUntilFinished));
				Notification notification = new NotificationCompat.Builder(context).setPriority(NotificationCompat.PRIORITY_MAX)
						.setSmallIcon(R.mipmap.ic_emberlight)
						.setContentTitle("SmartLight")
						.setContentText("Light will be fully on in: " + duration)
						.setOngoing(true)
						.addAction(R.mipmap.ic_launcher, "Turn Off", piTurnOff)
						.addAction(R.mipmap.ic_launcher, "Turn On Now", piTurnOn)
						.build();
				notificationManager.notify("AlarmNotification", alarmId, notification);
			}

			@Override
			public void onFinish() {
				notificationManager.cancel("AlarmNotification", alarmId);
				Intent intent = new Intent(context, MainActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);
				Notification notification = new NotificationCompat.Builder(context).setSmallIcon(R.mipmap.ic_emberlight)
						.setContentTitle("SmartLight")
						.setContentText("Light is on! :)")
						.setSubText("Click here to open SmartLight")
						.setContentIntent(contentIntent)
						.setAutoCancel(true)
						.build();
				notificationManager.notify((int) System.currentTimeMillis(), notification);
			}
		}.start();

		// Change dimLevel every minute
		emberlightTimer = new CountDownTimer(alarm.dimmingPeriod * 60000, 60000) {

			@Override
			public void onTick(final long millisUntilFinished) {
				// Run AsyncTask every minute
				emberlight.dimLights((int) ((double) (alarm.dimmingPeriod * 60000 - millisUntilFinished) / (alarm.dimmingPeriod * 60000) * 100));
			}

			@Override
			public void onFinish() {
				stopSelf();
			}
		}.start();

		// Disable current alarm if no repeat has been set
		//noinspection UnnecessaryBoxing - must be an object for EventBus
		EventBus.getDefault().postSticky(new Integer(alarmId));

		return START_NOT_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		Log.d("AlarmService", "Destroying service");
		notificationManager.cancel("AlarmNotification", alarmId);
		notificationTimer.cancel();
		emberlightTimer.cancel();
		super.onDestroy();
	}
}