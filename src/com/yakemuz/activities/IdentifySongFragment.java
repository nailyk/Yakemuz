package com.yakemuz.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;

import com.yakemuz.echonestAPI.EchonestAPI;
import com.yakemuz.model.Song;
import com.yakemuz.model.Track;
import com.yakemuz.partnersAPI.DeezerAPI;
import com.yakemuz.partnersAPI.SpotifyAPI;
import com.yakemuz.partnersAPI.WhosampledAPI;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;

public class IdentifySongFragment extends Fragment {

	static interface IdentifySongListener {
		void onProgressUpdate(String... params);
		void onPostExecute(Bundle results);
		void onCancelled();
	}

	private IdentifySongListener listener;
	private IdentifySongTask task;

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			listener = (IdentifySongListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement IdentifySongListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// Tell the framework to try to keep this fragment around
		// during a configuration change.
		setRetainInstance(true);
	}

	@Override
	public void onDetach() {
		super.onDestroy();
		listener = null;
	}

	public void start(String fp_code) {
		// Create and execute the background task.
		task = new IdentifySongTask();
		task.execute(fp_code);	
	}

	private class IdentifySongTask extends AsyncTask<String, String, Bundle> {

		Bundle results = new Bundle();
		EchonestAPI echonest_api = new EchonestAPI();
		DeezerAPI deezer_api = new DeezerAPI();
		SpotifyAPI spotify_api = new SpotifyAPI();
		WhosampledAPI whosampled_api = new WhosampledAPI();

		@Override
		protected Bundle doInBackground(String... params) {
			Song song = null;
			try {
				song = this.echonest_api.identifySong(params[0]);
			} catch (Exception e) {
				e.printStackTrace();
				publishProgress(e.getMessage());
				cancel(true);
				return null;
			}
			try {
				results.putString("artist", song.getArtistName());
				results.putString("title", song.getTitle());
				Track track_deezer = song.getTrackOld("deezer");
				if (track_deezer != null) {
					String track_id = new String(track_deezer.getForeignID());
					Map<String, String> map = deezer_api.getTrackInfos(track_id);
					results.putString("release_name", map.get("release_name"));
					results.putString("release_date", map.get("release_date"));
					results.putString("link_deezer", map.get("link_deezer"));
					Bitmap cover = deezer_api.getTrackReleaseCover(map.get("cover"));

					// Convert bitmap to byte array
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					cover.compress(CompressFormat.PNG, 0 /* ignored for PNG */, bos);
					byte[] bitmapdata = bos.toByteArray();

					// create a file to write bitmap data
					File f = new File(getActivity().getCacheDir(),"cover");
					f.createNewFile();

					// write the bytes in file
					FileOutputStream fos = new FileOutputStream(f);
					fos.write(bitmapdata);
					fos.close();

					results.putString("cover_filepath", f.getAbsolutePath());
				}

				Track track_spotify = song.getTrackOld("spotify");
				if (track_spotify != null) {
					String track_id = new String(track_spotify.getForeignID());
					Map<String, String> map = spotify_api.getTrackInfos(track_id);
					if (results.get("release_name") != null)
						results.putString("release_name", map.get("release_name"));
					if (results.get("release_date") != null)
						results.putString("release_date", map.get("release_date"));
					results.putString("link_spotify", map.get("link_spotify"));
				}

				Track track_whosampled = song.getTrackOld("whosampled");
				if (track_whosampled != null) {
					String track_id = new String(track_whosampled.getForeignID());
					results.putString("link_whosampled", whosampled_api.getLink(track_id));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return results;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			if (listener != null) {
				listener.onProgressUpdate(values[0]);
			}
		}

		@Override
		protected void onPostExecute(Bundle results) {
			if (listener != null) {
				listener.onPostExecute(results);
			}
		}

		@Override
		protected void onCancelled() {
			if (listener != null) {
				listener.onCancelled();
			}
		}
	}
}
