package com.yakemuz.activities;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yakemuz.R;
import com.yakemuz.activities.RecordFragment.AudioFingerprinterListener;
import com.yakemuz.util.NetworkState;

public class MainActivity extends Activity implements AudioFingerprinterListener {

	RecordFragment mRecordFragment;
	TextView status;
	ImageButton recordButton;
	// processing will be set to 'true' from the start of the fingerprinting/matching process to its end
	boolean processing = false;
	boolean isInternetPresent = false;
	// Connection detector class
	NetworkState net_state;
	Animation rotation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		status = (TextView) findViewById(R.id.t_status);
		rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		net_state = new NetworkState(this);
		recordButton = (ImageButton) findViewById(R.id.b_record);
		recordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (processing) {
					mRecordFragment.stop();
				}
				else {
					// get Internet status
					isInternetPresent = net_state.isConnectingToInternet();
					if (!isInternetPresent) {
						Toast toast = Toast.makeText(MainActivity.this, "no Internet connection",Toast.LENGTH_SHORT);
						toast.show();
					}
					else {
						// start logo animation
						recordButton.startAnimation(rotation);
						// start fingerprint
						mRecordFragment.fingerprint(20);
					}
				}
			}
		});

		FragmentManager fm = getFragmentManager();

		// Check to see if we have retained the worker fragment.
		mRecordFragment = (RecordFragment)fm.findFragmentByTag("RECORD_FRAGMENT");

		if (mRecordFragment != null) {
			switch (mRecordFragment.getState()) {
			case RecordFragment.STATE_LISTENING:
				processing = true;
				status.setText("Listening...");
				break;
			case RecordFragment.STATE_MATCHING:
				processing = true;
				status.setText("Matching...");
				break;	
			}
		}		
		// If not retained (or first time running), we need to create it.
		else {
			FragmentTransaction ft = fm.beginTransaction();
			mRecordFragment = new RecordFragment();
			ft.add(mRecordFragment, "RECORD_FRAGMENT");
			ft.commit();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (rotation.hasStarted()) {
			rotation.cancel();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (processing) {
			recordButton.startAnimation(rotation);	
		}
	}


	@Override
	public void willStartListening() {
		processing = true;
		status.setText("Listening...");
	}

	@Override
	public void didFinishListening() {
		status.setText("Matching...");
	}

	@Override
	public void didFinishMatching(Bundle results) {
		processing = false;	
		rotation.cancel();		
		Intent intent = new Intent(MainActivity.this, SongResultsActivity.class);
		intent.putExtras(results);
		status.setText(R.string.t_status);
		startActivity(intent);
	}

	@Override
	public void didFailWithException(Exception e) {
		processing = false;			
		status.setText(e.getMessage());
		rotation.cancel();
	}

	@Override
	public void didInterrupted() {
		status.setText(R.string.t_status);
		rotation.cancel();
		processing = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}