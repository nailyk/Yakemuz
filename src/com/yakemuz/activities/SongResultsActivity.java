package com.yakemuz.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.yakemuz.R;
import com.yakemuz.preferences.MyPreferenceActivity;

public class SongResultsActivity extends Activity {

	TextView artist, title, release_name;
	ImageView album_cover, whosampled_logo, spotify_logo, deezer_logo;
	String link_whosampled, link_deezer, link_spotify;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_results);
		// Show the Up button in the action bar.
		setupActionBar();
		artist = (TextView) findViewById(R.id.artist);
		title = (TextView) findViewById(R.id.title);
		release_name = (TextView) findViewById(R.id.release_name);
		album_cover = (ImageView) findViewById(R.id.album_cover);
		whosampled_logo = (ImageView) findViewById(R.id.whosampled_logo);
		spotify_logo = (ImageView) findViewById(R.id.spotify_logo);
		deezer_logo = (ImageView) findViewById(R.id.deezer_logo);

		Intent intent = getIntent();
		Bundle results = intent.getExtras();
		artist.setText(results.getString("artist"));
		title.setText(results.getString("title"));
		if (results.getString("release_name") != null) {
			release_name.setText(results.getString("release_name"));
		}
		else {
			findViewById(R.id.layout_release_name).setVisibility(View.GONE);
		}
		if (results.getString("cover_filepath") != null) {
			album_cover.setImageBitmap(BitmapFactory.decodeFile(results.getString("cover_filepath")));
		}
		if (results.getString("link_spotify") != null) {	
			link_spotify = results.getString("link_spotify");
			spotify_logo.setVisibility(View.VISIBLE);
			spotify_logo.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (! link_spotify.isEmpty()) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						intent.setData(Uri.parse(link_spotify));
						startActivity(intent);
					}
				}});
		}

		if (results.getString("link_deezer") != null) {	
			link_deezer = results.getString("link_deezer");
			deezer_logo.setVisibility(View.VISIBLE);
			deezer_logo.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (! link_deezer.isEmpty()) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						intent.setData(Uri.parse(link_deezer));
						startActivity(intent);
					}
				}});
		}

		if (results.getString("link_whosampled") != null) {	
			link_whosampled = results.getString("link_whosampled");
			whosampled_logo.setVisibility(View.VISIBLE);
			whosampled_logo.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					if (! link_whosampled.isEmpty()) {
						Intent intent = new Intent();
						intent.setAction(Intent.ACTION_VIEW);
						intent.addCategory(Intent.CATEGORY_BROWSABLE);
						intent.setData(Uri.parse(link_whosampled));
						startActivity(intent);
					}
				}});
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.song_results, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;

		case R.id.action_settings:
			startActivity(new Intent(this, MyPreferenceActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
