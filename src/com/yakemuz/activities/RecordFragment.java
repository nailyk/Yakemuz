package com.yakemuz.activities;

import edu.gvsu.masl.echoprint.Codegen;
import android.app.Activity;
import android.app.Fragment;
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
		 * Called when the fingerprinter process loop has finished
		 */
		public void didFinishListening(String fp_code);

		/**
		 * Called when the fingerprinter is about to start
		 */
		public void willStartListening();

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

	private AudioRecord mRecordInstance = null;
	private Thread thread;
	private short audioData[];
	private int bufferSize;
	private int secondsToRecord;

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

			didFinishListening(fp_code);
		}
		catch (Exception e) {
			e.printStackTrace();
			didFailWithException(e);
		}
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

	private void didFinishListening(final String fp_code) {
		if (listener == null) {
			return;
		}
		if (listener instanceof Activity) {
			Activity activity = (Activity) listener;
			activity.runOnUiThread(new Runnable() {
				public void run() {
					listener.didFinishListening(fp_code);
				}
			});
		} else
			listener.didFinishListening(fp_code);
	}

	private void willStartListening() {
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

	private void didFailWithException(final Exception e) {
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
}
