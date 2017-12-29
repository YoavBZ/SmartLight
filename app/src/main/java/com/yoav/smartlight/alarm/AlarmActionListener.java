package com.yoav.smartlight.alarm;

/**
 * Created by Yoav on 8/1/2017.
 */

interface AlarmActionListener {

	void onSetRepeatedAlarm(Alarm alarm);

	void onSetSingleAlarm(Alarm alarm);

	void onCancelAlarm(Alarm alarm);

}
