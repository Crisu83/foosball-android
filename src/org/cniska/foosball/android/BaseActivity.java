package org.cniska.foosball.android;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


public class BaseActivity extends FragmentActivity {

	private static final String TAG = "BaseActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		Logger.info(TAG, "Activity resumed.");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Logger.info(TAG, "Activity paused.");
		super.onPause();
	}

	protected void initActionBar(boolean showTitle, boolean homeButtonEnabled) {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(showTitle);
		actionBar.setHomeButtonEnabled(homeButtonEnabled);
	}

	protected void startMainActivity() {
		Logger.info(TAG, "Sending intent to start MainActivity.");
		Intent intent = new Intent(this, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}