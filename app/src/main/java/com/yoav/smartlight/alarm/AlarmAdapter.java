package com.yoav.smartlight.alarm;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.google.gson.Gson;
import com.nex3z.togglebuttongroup.MultiSelectToggleGroup;
import com.yoav.smartlight.R;
import com.yoav.smartlight.TimePickerFragment;
import net.cachapa.expandablelayout.ExpandableLayout;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.MyViewHolder> {

	public static final int UNSELECTED = -1;
	private RecyclerView recyclerView;
	public int selectedItem = UNSELECTED;
	private MultiSelector mMultiSelector = new MultiSelector();
	public ArrayList<Alarm> alarmList;
	private AlarmActionListener alarmActionListener;
	private TextView backgroundText;

	AlarmAdapter(ArrayList<Alarm> alarmList, RecyclerView recyclerView, TextView backgroundText, AlarmActionListener alarmActionListener) {
		this.alarmList = alarmList;
		this.recyclerView = recyclerView;
		this.alarmActionListener = alarmActionListener;
		this.backgroundText = backgroundText;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, int position) {
		backgroundText.setVisibility(View.GONE);
		final Alarm alarmObject = alarmList.get(holder.getAdapterPosition());
		holder.headerIndex.setText(holder.getAdapterPosition() + 1 + ". ");
		holder.headerText.setText(alarmObject.toString());
		holder.itemHeader.setSelected(false);
		holder.itemHeader.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!mMultiSelector.isSelectable()) {
					MyViewHolder lastHolder = (MyViewHolder) recyclerView.findViewHolderForAdapterPosition(selectedItem);
					if (lastHolder != null)
						lastHolder.expandableLayout.collapse();
					if (holder.getAdapterPosition() == selectedItem)
						selectedItem = UNSELECTED;
					else {
						holder.expandableLayout.expand();
						selectedItem = holder.getAdapterPosition();
					}
				} else
					mMultiSelector.tapSelection(holder);
			}
		});
		holder.itemHeader.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				MyViewHolder lastHolder = (MyViewHolder) recyclerView.findViewHolderForAdapterPosition(selectedItem);
				if (lastHolder != null)
					lastHolder.expandableLayout.collapse();
				if (holder.getAdapterPosition() == selectedItem)
					selectedItem = UNSELECTED;
				if (!mMultiSelector.isSelectable()) {
					AppCompatActivity activity = (AppCompatActivity) v.getContext();
					activity.startSupportActionMode(holder.mActionModeCallback);
					mMultiSelector.setSelectable(true);
					mMultiSelector.setSelected(holder, true);
				}
				return true;
			}
		});
		holder.expandableLayout.collapse(false);
		holder.expandableLayout.setInterpolator(new OvershootInterpolator());
		holder.expandableLayout.setOnExpansionUpdateListener(holder);
		holder.expandedSwitch.setChecked(alarmObject.activated);
		holder.expandedSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (holder.toggleGroup.getCheckedIds().isEmpty())
						alarmActionListener.onSetSingleAlarm(alarmObject);
					else
						alarmActionListener.onSetRepeatedAlarm(alarmObject);
				} else
					alarmActionListener.onCancelAlarm(alarmObject);
				alarmObject.activated = !alarmObject.activated;
//				saveAlarms();
			}
		});
		final AnimationSet inAnimation = setInAnimation();
		final AnimationSet outAnimation = setOutAnimation(holder);

		for (int checkedDayIndex : alarmObject.weekDays) {
			int checkedDayId = holder.toggleGroup.getChildAt(checkedDayIndex - 1).getId();
			holder.toggleGroup.check(checkedDayId);
		}
		if (!holder.toggleGroup.getCheckedIds().isEmpty()) {
			holder.repeatIcon.setVisibility(View.VISIBLE);
			holder.repeatIcon.startAnimation(inAnimation);
		}
		holder.toggleGroup.setOnCheckedChangeListener(new MultiSelectToggleGroup.OnCheckedStateChangeListener() {
			@Override
			public void onCheckedStateChanged(MultiSelectToggleGroup group, int checkedId, boolean isChecked) {
				View checkedDay = group.findViewById(checkedId);
				int checkedDayIndex = group.indexOfChild(checkedDay);
				if (isChecked)
					alarmObject.weekDays.add(checkedDayIndex + 1);
				else
					alarmObject.weekDays.remove(checkedDayIndex + 1);

				if (group.getCheckedIds().isEmpty())
					holder.repeatIcon.startAnimation(outAnimation);
				else if (isChecked && group.getCheckedIds().size() == 1) {
					holder.repeatIcon.setVisibility(View.VISIBLE);
					holder.repeatIcon.startAnimation(inAnimation);
				}
			}
		});
		holder.seekBar.setProgress(alarmObject.dimmingPeriod);
		holder.seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
			@Override
			public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
			}

			@Override
			public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
				alarmObject.dimmingPeriod = seekBar.getProgress();
			}
		});
		holder.editTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerFragment timePicker = new TimePickerFragment();
				Bundle bundle = new Bundle();
				bundle.putInt("adapterPosition", holder.getAdapterPosition());
				timePicker.setArguments(bundle);
				timePicker.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), "timePicker");
			}
		});
	}

	private AnimationSet setInAnimation() {
		AnimationSet inAnimation = new AnimationSet(false);
		AlphaAnimation inAlphaAnimation = new AlphaAnimation(0, 1);
		inAlphaAnimation.setDuration(200);
		inAnimation.addAnimation(inAlphaAnimation);
		RotateAnimation inRotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		inRotateAnimation.setStartOffset(200);
		inAnimation.addAnimation(inRotateAnimation);
		inAnimation.setDuration(1000);
		return inAnimation;
	}

	private AnimationSet setOutAnimation(final MyViewHolder holder) {
		AnimationSet outAnimation = new AnimationSet(false);
		AlphaAnimation outAlphaAnimation = new AlphaAnimation(1, 0);
		outAlphaAnimation.setStartOffset(200);
		outAlphaAnimation.setDuration(200);
		outAnimation.addAnimation(outAlphaAnimation);
		outAnimation.addAnimation(new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f));
		outAnimation.setDuration(1000);
		outAnimation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				holder.repeatIcon.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});
		return outAnimation;
	}

	private void saveAlarms() {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(recyclerView.getContext())
				.edit();
		editor.putString("alarmList", new Gson().toJson(alarmList));
		editor.apply();
	}

	@Override
	public int getItemCount() {
		return alarmList.size();
	}

	public class MyViewHolder extends SwappingHolder implements ExpandableLayout.OnExpansionUpdateListener {

		RelativeLayout itemHeader;
		ExpandableLayout expandableLayout;
		public TextView headerText;
		TextView headerIndex;
		MultiSelectToggleGroup toggleGroup;
		public Switch expandedSwitch;
		DiscreteSeekBar seekBar;
		ImageView repeatIcon;
		ImageButton editTime;

		MyViewHolder(final View itemView) {
			super(itemView, mMultiSelector);
			expandedSwitch = (Switch) itemView.findViewById(R.id.expanded_switch);
			itemHeader = (RelativeLayout) itemView.findViewById(R.id.item_header);
			headerIndex = (TextView) itemView.findViewById(R.id.header_index);
			headerText = (TextView) itemView.findViewById(R.id.header_text);
			expandableLayout = (ExpandableLayout) itemView.findViewById(R.id.expandable_layout);
			toggleGroup = (MultiSelectToggleGroup) itemView.findViewById(R.id.group_weekdays);
			seekBar = (DiscreteSeekBar) itemView.findViewById(R.id.seekBar);
			repeatIcon = (ImageView) itemView.findViewById(R.id.repeat_icon);
			editTime = (ImageButton) itemView.findViewById(R.id.edit_time);
		}

		@Override
		public void onExpansionUpdate(float expansionFraction, int state) {
			recyclerView.smoothScrollToPosition(getAdapterPosition());
		}

		private ModalMultiSelectorCallback mActionModeCallback = new ModalMultiSelectorCallback(mMultiSelector) {
			@Override
			public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
				super.onCreateActionMode(actionMode, menu);
				((Activity) recyclerView.getContext()).getMenuInflater().inflate(R.menu.selection_mode_menu, menu);
				// Sets all switches to be not Clickable
				for (int i = 0; i < recyclerView.getChildCount(); i++) {
					RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
					((MyViewHolder) viewHolder).expandedSwitch.setClickable(false);
				}
				return true;
			}

			@Override
			public void onDestroyActionMode(ActionMode actionMode) {
				super.onDestroyActionMode(actionMode);
				// Sets all switches to be Clickable
				for (int i = 0; i < recyclerView.getChildCount(); i++) {
					((MyViewHolder) recyclerView.findViewHolderForAdapterPosition(i)).expandedSwitch.setClickable(true);
				}
			}

			@Override
			public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
				if (menuItem.getItemId() == R.id.menu_item_delete) {
					actionMode.finish();
					for (int i = alarmList.size(); i >= 0; i--) {
						if (mMultiSelector.isSelected(i, 0)) {
							// Cancels the intent
							expandedSwitch.setChecked(false);
							alarmList.remove(i);
							notifyItemRemoved(i);
							if (alarmList.isEmpty())
								backgroundText.setVisibility(View.VISIBLE);
						}
					}
					mMultiSelector.clearSelections();
					return true;
				}
				return false;
			}
		};

	}
}
