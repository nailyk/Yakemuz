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

public class MainActivity extends Activity implements AudioFingerprinterListener, IdentifySongFragment.IdentifySongListener {

	RecordFragment mRecordFragment;
	IdentifySongFragment mIdentifySongFragment;

	TextView status;
	ImageButton recordButton;
	// processing will be set to 'true' from the start of the fingerprinting process to the end of the IdentifySong task
	boolean processing = false;
	boolean isInternetPresent = false;
	// Connection detector class
	NetworkState net_state;
	Animation rotation;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean("is_processing", processing);
		outState.putCharSequence("status", status.getText());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);
		processing = savedInstanceState.getBoolean("is_processing");
		if (processing) {
			/* status could be "recording" if the fingerprint thread is still running
			 * or "matching" if the identifySongTask is being executed
			 */
			status.setText(savedInstanceState.getCharSequence("status"));
			recordButton.startAnimation(rotation);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FragmentManager fm = getFragmentManager();

		// Check to see if we have retained the worker fragment.
		mRecordFragment = (RecordFragment)fm.findFragmentByTag("RECORD_FRAGMENT");
		mIdentifySongFragment = (IdentifySongFragment) fm.findFragmentByTag("IDENTIFY_SONG_FRAGMENT");

		// If not retained (or first time running), we need to create it.
		if (mRecordFragment == null) {
			FragmentTransaction ft = fm.beginTransaction();
			mRecordFragment = new RecordFragment();
			mIdentifySongFragment = new IdentifySongFragment();
			ft.add(mRecordFragment, "RECORD_FRAGMENT");
			ft.add(mIdentifySongFragment, "IDENTIFY_SONG_FRAGMENT");
			ft.commit();
		}

		rotation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		net_state = new NetworkState(this);
		status = (TextView) findViewById(R.id.t_status);
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
	}

	@Override
	public void didFinishListening(String fp_code) {
		status.setText("Matching...");
		mIdentifySongFragment.start(fp_code);
	}

	@Override
	public void willStartListening() {
		status.setText("Listening...");
		processing = true;
	}

	@Override
	public void didFailWithException(Exception e) {
		status.setText("Error: " + e);
		recordButton.clearAnimation();
		processing = false;	
	}

	@Override
	public void didInterrupted() {
		status.setText(R.string.t_status);
		recordButton.clearAnimation();
		processing = false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onPostExecute(Bundle results) {
		recordButton.clearAnimation();
		processing = false;			
		Intent intent = new Intent(MainActivity.this, SongResultsActivity.class);
		intent.putExtra("results", results);
		status.setText(R.string.t_status);
		startActivity(intent);	
	}

	@Override
	public void onProgressUpdate(String... values) {
		status.setText(values[0]);
	}

	@Override
	public void onCancelled() {
		recordButton.clearAnimation();
		processing = false;
	}
}