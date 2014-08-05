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

import edu.gvsu.masl.echoprint.Codegen;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;

public class RecordFragment extends Fragment implements Runnable {

	/**
	 * Interface for the fingerprinter listener<br>
	 * Contains the different delegate methods for the fingerprinting process
	 */
	static interface AudioFingerprinterListener {

		/**
		 * Called when the fingerprinter is going to process
		 */
		public void willStartListening();

		/**
		 * Called when the fingerprinter process has finished
		 */
		public void didFinishListening();

		/**
		 * Called when the matching process has finished
		 */
		public void didFinishMatching(Bundle results);
		/**
		 * Called if there is an error / exception in the fingerprinting process
		 * 
		 * @param e an exception with the error
		 */
		public void didFailWithException(Exception e);

		/**
		 * Called if the fingerprinter process has been interrupted
		 */
		public void didInterrupted();
	}

	private AudioFingerprinterListener listener;
	private final int SAMPLE_RATE = 11025;
	private final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
	private final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	final static short STATE_IDE = 0;
	final static short STATE_LISTENING = 1;
	final static short STATE_MATCHING = 2;

	private AudioRecord mRecordInstance = null;
	private Song song = null;
	private Thread thread;
	private short audioData[];
	private int bufferSize;
	private int secondsToRecord;
	private short state = STATE_IDE;

	private Bundle results;
	private EchonestAPI echonest_api;
	DeezerAPI deezer_api;
	private SpotifyAPI spotify_api;
	private WhosampledAPI whosampled_api;

	@Override
	public void run() {
		try {
			// create the audio buffer
			// get the minimum buffer size
			int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);

			// and the actual buffer size for the audio to record
			// SAMPLE_RATE * seconds to record.
			bufferSize = Math.max(minBufferSize, SAMPLE_RATE * secondsToRecord);

			audioData = new short[bufferSize];	
			// start recorder
			mRecordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, minBufferSize);

			willStartListening();
			mRecordInstance.startRecording();
			// fill audio buffer with mic data.
			int samplesIn = 0;
			do {
				samplesIn += mRecordInstance.read(audioData, samplesIn, bufferSize - samplesIn);
				if (mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
					break;
			} while (samplesIn < bufferSize);

			if (mRecordInstance.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED)
			{
				mRecordInstance.release();
				mRecordInstance = null;
				didInterrupted();
				return;
			}

			mRecordInstance.stop();
			mRecordInstance.release();
			mRecordInstance = null;

			// create an echoprint codegen wrapper and get the code
			Codegen codegen = new Codegen();
			String fp_code = codegen.generate(audioData, samplesIn);

			if (fp_code.length() == 0) {
				throw new Exception("unable to generate the audio fingerprint");
			}

			didFinishListening();
			song = this.echonest_api.identifySong(fp_code);
		}
		catch (Exception e) {
			e.printStackTrace();
			didFailWithException(e);
			return;
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
				cover.compress(CompressFormat.JPEG, 100 /* ignored for PNG */, bos);
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
		didFinishMatching(results);

	}

	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
		try {
			listener = (AudioFingerprinterListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() +
					" must implement AudioFingerprinterListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// Tell the framework to try to keep this fragment around
		// during a configuration change.
		setRetainInstance(true);

		results = new Bundle();
		echonest_api = new EchonestAPI();
		deezer_api = new DeezerAPI();
		spotify_api = new SpotifyAPI();
		whosampled_api = new WhosampledAPI();
	}


	/**
	 * This is called when the fragment is going away.  It is NOT called
	 * when the fragment is being propagated between activity instances.
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		stop();
	}


	@Override
	public void onDetach() {
		super.onDestroy();
		listener = null;
	}

	public void fingerprint(int seconds) {
		this.secondsToRecord = seconds;
		// We are ready for our thread to go.
		thread = new Thread(this);
		thread.start();
	}

	/**
	 * stops the listening / fingerprinting process if there's one in process
	 */
	public void stop() {
		if (mRecordInstance != null) {
			mRecordInstance.stop();
		}
	}


	private void willStartListening() {
		state = STATE_LISTENING;
		if (listener == null) {
			return;
		}
		if (listener instanceof Activity) {
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					listener.willStartListening();
				}
			});
		} else
			listener.willStartListening();
	}

	private void didFinishListening() {
		state = STATE_MATCHING;
		if (listener == null) {
			return;
		}
		if (listener instanceof Activity) {
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					listener.didFinishListening();
				}
			});
		} else
			listener.didFinishListening();
	}

	private void didFinishMatching(final Bundle results) {
		state = STATE_IDE;
		if (listener == null) {
			return;
		}
		if (listener instanceof Activity) {
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					listener.didFinishMatching(results);
				}
			});
		} else
			listener.didFinishMatching(results);
	}
	private void didFailWithException(final Exception e) {
		state = STATE_IDE;
		if (listener == null) {
			return;
		}
		if (listener instanceof Activity) {
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					listener.didFailWithException(e);
				}
			});
		} else
			listener.didFailWithException(e);
	}

	private void didInterrupted() {
		state = STATE_IDE;
		if (listener == null) {
			return;
		}
		if (listener instanceof Activity) {
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					listener.didInterrupted();
				}
			});
		} else
			listener.didInterrupted();
	}

	public short getState() {
		return this.state;
	}
}
