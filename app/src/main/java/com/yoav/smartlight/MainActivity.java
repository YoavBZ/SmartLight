package com.yoav.smartlight;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.yoav.smartlight.alarm.AlarmsFragment;
import com.yoav.smartlight.emberlight.EmberlightFragment;
import com.yoav.smartlight.emberlight.EmberlightService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	private DrawerLayout mDrawer;
	private EmberlightService emberlightService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		mDrawer.addDrawerListener(toggle);
		toggle.syncState();
		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		emberlightService = new EmberlightService(this);
		if (!emberlightService.isAuthorized()) {
			emberlightService.authorize();
		}

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame, new AlarmsFragment(), "alarmsFragment")
				.commit();
	}

	public EmberlightService getEmberlightService() {
		return emberlightService;
	}

	@Override
	protected void onStart() {
		super.onStart();
		checkIntent(getIntent());
	}

	@Override
	public void onBackPressed() {
		if (mDrawer.isDrawerOpen(GravityCompat.START)) {
			mDrawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		emberlightService.close();
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		checkIntent(intent);
	}

	private void checkIntent(@Nullable Intent intent) {
		if (intent != null) {
			String action = intent.getAction();
			switch (action) {
				case "com.yoav.smartlight.HANDLE_AUTHORIZATION_RESPONSE":
					if (!intent.hasExtra("USED_INTENT")) {
						emberlightService.handleAuthorizationResponse(intent);
						intent.putExtra("USED_INTENT", true);
					}
					break;
				default:
					// do nothing
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean isAuthorized = emberlightService.isAuthorized();
		menu.findItem(R.id.action_authorize).setVisible(!isAuthorized);
		menu.findItem(R.id.action_sign_out).setVisible(isAuthorized);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_authorize:
				emberlightService.authorize();
				return true;
			case R.id.action_sign_out:
				emberlightService.signOut();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
		// Handle navigation view item clicks here.
		Fragment fragment = null;
		String tag = null;
		switch (menuItem.getItemId()) {
			case (R.id.alarms):
				fragment = new AlarmsFragment();
				tag = "Alarms";
				break;
			case (R.id.emberlight):
				fragment = new EmberlightFragment();
				tag = "Emberlight";
				break;
			case (R.id.about):
				break;
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.frame, fragment, tag).commit();

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);

		return true;
	}
}