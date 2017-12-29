package com.yoav.smartlight;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.RecyclerView;
import android.widget.TimePicker;
import com.yoav.smartlight.alarm.Alarm;
import com.yoav.smartlight.alarm.AlarmAdapter;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Sets TimePickerDialog time to current time
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, 1);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);

		// Create a new instance of TimePickerDialog and return it
		return new TimePickerDialog(getActivity(), R.style.DialogTheme, this, hour, minute, false);
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		// Adds new alarm
		RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
		AlarmAdapter adapter = (AlarmAdapter) recyclerView.getAdapter();
		if (getArguments() == null) {
			Alarm alarm = new Alarm(hourOfDay, minute);
			adapter.alarmList.add(alarm);
			adapter.notifyItemInserted(adapter.alarmList.size() - 1);
		} else {
			int adapterPosition = getArguments().getInt("adapterPosition");
			Alarm alarm = adapter.alarmList.get(adapterPosition);
			alarm.hour = hourOfDay;
			alarm.minutes = minute;
			AlarmAdapter.MyViewHolder holder = ((AlarmAdapter.MyViewHolder) recyclerView.findViewHolderForAdapterPosition(adapterPosition));
			holder.headerText.setText(alarm.toString());
			holder.expandedSwitch.setChecked(false);
			adapter.selectedItem = AlarmAdapter.UNSELECTED;
			adapter.notifyItemChanged(adapterPosition);
		}
	}
}