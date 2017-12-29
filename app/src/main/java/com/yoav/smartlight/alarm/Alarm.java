package com.yoav.smartlight.alarm;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Yoav on 7/18/2017.
 */

public class Alarm implements Parcelable {
	public int hour;
	public int minutes;
	boolean activated;
	int dimmingPeriod;
	Set<Integer> weekDays; // Contains the repeating days (Sunday = 1, ...)
	public int id;

	public Alarm(int hour, int minutes) {
		this.hour = hour;
		this.minutes = minutes;
		id = Integer.valueOf("" + hour + minutes);
		weekDays = new LinkedHashSet<>();
		dimmingPeriod = 15;
	}

	@Override
	public String toString() {
		return String.format("%02d", hour) + ":" + String.format("%02d", minutes);
	}

	boolean isActivatedToday() {
		if (weekDays.isEmpty())
			return true;
		Calendar calendar = Calendar.getInstance();
		for (int day : weekDays) {
			if (day == calendar.get(Calendar.DAY_OF_WEEK)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Utilities to marshall and unmarshall a {@code Parcelable} to and from a byte array.
	 */
	static byte[] marshall(Parcelable parcelable) {
		Parcel parcel = Parcel.obtain();
		parcelable.writeToParcel(parcel, 0);
		byte[] bytes = parcel.marshall();
		parcel.recycle();
		return bytes;
	}

	private static Parcel unmarshall(byte[] bytes) {
		Parcel parcel = Parcel.obtain();
		parcel.unmarshall(bytes, 0, bytes.length);
		parcel.setDataPosition(0); // This is extremely important!
		return parcel;
	}

	static <T> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
		Parcel parcel = unmarshall(bytes);
		T result = creator.createFromParcel(parcel);
		parcel.recycle();
		return result;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(hour);
		dest.writeInt(minutes);
		dest.writeInt(id);
		dest.writeInt(activated ? 1 : 0);
		dest.writeInt(dimmingPeriod);
		dest.writeArray(weekDays.toArray());
	}

	private static Alarm create(Parcel in) {
		Alarm alarm = new Alarm(in.readInt(), in.readInt());
		alarm.id = in.readInt();
		alarm.activated = in.readInt() != 0;
		alarm.dimmingPeriod = in.readInt();
		for (Object item : in.readArray(LinkedHashSet.class.getClassLoader()))
			alarm.weekDays.add((Integer) item);
		return alarm;
	}

	public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
		@Override
		public Alarm createFromParcel(Parcel source) {
			return Alarm.create(source);
		}

		@Override
		public Alarm[] newArray(int size) {
			return new Alarm[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}
}
