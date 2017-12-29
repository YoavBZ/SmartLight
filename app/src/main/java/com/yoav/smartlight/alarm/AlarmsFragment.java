package com.yoav.smartlight.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yoav.smartlight.R;
import com.yoav.smartlight.TimePickerFragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.Calendar;

public class AlarmsFragment extends Fragment implements AlarmActionListener {

	AlarmAdapter mAdapter;
	RecyclerView recyclerView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.alarms_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				TimePickerFragment timePicker = new TimePickerFragment();
				timePicker.show(getFragmentManager(), "timePicker");
			}
		});
		TextView backgroundText = (TextView) view.findViewById(R.id.background_text);
		recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
		RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		// Collapse expanded item when clicking outside the RecyclerView
		recyclerView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (mAdapter.selectedItem != -1) {
						AlarmAdapter.MyViewHolder holder = (AlarmAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(mAdapter.selectedItem);
						holder.itemHeader.callOnClick();
					}
				}
				return false;
			}
		});

		String json = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("alarmList", null);
		@SuppressWarnings("unchecked") ArrayList<Alarm> alarmList = json != null ? (ArrayList) new Gson().fromJson(json, new TypeToken<ArrayList<Alarm>>() {
		}.getType()) : new ArrayList<>();

		mAdapter = new AlarmAdapter(alarmList, recyclerView, backgroundText, this);
		recyclerView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onResume() {
		super.onResume();
		EventBus.getDefault().register(this);
	}

	@Override
	public void onPause() {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
		editor.putString("alarmList", new Gson().toJson(mAdapter.alarmList));
		editor.apply();
		EventBus.getDefault().unregister(this);
		super.onPause();
	}

	@Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
	public void onMessage(Integer idToDisable) {
		for (Alarm alarm : mAdapter.alarmList) {
			if (alarm.id == idToDisable) {
				alarm.activated = false;
				mAdapter.notifyDataSetChanged();
				mAdapter.selectedItem = AlarmAdapter.UNSELECTED;
				EventBus.getDefault().removeAllStickyEvents();
				break;
			}
		}
	}

	@Override
	public void onSetRepeatedAlarm(Alarm alarm) {
		AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getContext(), AlarmManagerBroadcastReceiver.class);
		intent.putExtra("one_time", Boolean.FALSE);
		intent.putExtra(AlarmManagerBroadcastReceiver.EXTRA_ALARM, Alarm.marshall(alarm));
		long triggerAtMillis = prepareTimeAndNotify(alarm.hour, alarm.minutes);
		PendingIntent pi = PendingIntent.getBroadcast(getContext(), Integer.valueOf("" + alarm.hour + alarm.minutes), intent, 0);
		// Repeat daily
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, AlarmManager.INTERVAL_DAY, pi);
	}

	@Override
	public void onCancelAlarm(Alarm alarm) {
		Intent intent = new Intent(getContext(), AlarmManagerBroadcastReceiver.class);
		PendingIntent sender = PendingIntent.getBroadcast(getContext(), Integer.valueOf("" + alarm.hour + alarm.minutes), intent, 0);
		sender.cancel();
		AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
	}

	@Override
	public void onSetSingleAlarm(Alarm alarm) {
		AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getContext(), AlarmManagerBroadcastReceiver.class);
		intent.putExtra("one_time", Boolean.TRUE);
		intent.putExtra(AlarmManagerBroadcastReceiver.EXTRA_ALARM, Alarm.marshall(alarm));
		long triggerAtMillis = prepareTimeAndNotify(alarm.hour, alarm.minutes);
		PendingIntent pi = PendingIntent.getBroadcast(getContext(), alarm.id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
	}

	private long prepareTimeAndNotify(int hour, int minutes) {
		// Removes index number and creates Time object
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, hour);
		calendar.set(Calendar.MINUTE, minutes);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		if (calendar.before(Calendar.getInstance()))
			// Time has already passed!
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		toastRemainingTime(calendar);
		return calendar.getTimeInMillis();
	}

	private void toastRemainingTime(Calendar calendar) {
		Period period = new Period(calendar.getTimeInMillis() - System.currentTimeMillis());
		String formatter = new PeriodFormatterBuilder().appendDays()
				.appendSuffix(" day", " days")
				.appendSeparator(", ", " and ")
				.appendHours()
				.appendSuffix(" hour", " hours")
				.appendSeparator(", ", " and ")
				.appendMinutes()
				.appendSuffix(" minute", " minutes")
				.toFormatter()
				.print(period);
		Toast.makeText(getContext(), "Alarm set for " + (formatter.isEmpty() ? "1 minute" : formatter) + " from now", Toast.LENGTH_LONG)
				.show();
	}
}
