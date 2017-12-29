package com.yoav.smartlight.emberlight;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.yoav.smartlight.MainActivity;
import com.yoav.smartlight.R;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Created by Yoav on 7/29/2017.
 */
public class EmberlightFragment extends Fragment {

	public EmberlightService emberlightService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		emberlightService = ((MainActivity) getContext()).getEmberlightService();
		return inflater.inflate(R.layout.emberlight_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Button getToken = (Button) view.findViewById(R.id.get_token);
		final TextView token = (TextView) view.findViewById(R.id.token);
		getToken.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				token.setText(emberlightService.getAccessToken());
			}
		});
		Button testApi = (Button) view.findViewById(R.id.test_api);
		testApi.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				emberlightService.getInfo();
			}
		});
		Button turnOn = (Button) view.findViewById(R.id.turn_on);
		turnOn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				emberlightService.turnLightOn();
			}
		});
		Button turnOff = (Button) view.findViewById(R.id.turn_off);
		turnOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				emberlightService.turnLightOff();
			}
		});
		Button toggle = (Button) view.findViewById(R.id.toggle);
		toggle.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				emberlightService.toggleLight();
			}
		});
		Button dim = (Button) view.findViewById(R.id.dim);
		final DiscreteSeekBar seekBar = (DiscreteSeekBar) view.findViewById(R.id.seekBar);
		dim.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				emberlightService.dimLights(seekBar.getProgress());
			}
		});
	}
}

