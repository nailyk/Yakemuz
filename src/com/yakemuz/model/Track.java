package com.yakemuz.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;

import com.yakemuz.echonestAPI.EchonestAPI;
import com.yakemuz.echonestAPI.EchonestException;

public class Track extends EchonestItem {

	private final static String PATH = "track";
	private final static String TYPE = "track";

	Track(EchonestAPI en, String idOrMD5, String type) throws EchonestException, IOException {
		super(en, TYPE, PATH, idOrMD5, type);
	}

	@SuppressWarnings("unchecked")
	Track(EchonestAPI en, Map data) throws EchonestException {
		super(en, TYPE, PATH, data);
	}

	/**
	 * Creates a track given an ID
	 *
	 * @param en the EchoNest API
	 * @param id the ID of the track
	 * @return the track
	 * @throws EchonestException
	 */
	static Track createTrack(EchonestAPI en, String id) throws EchonestException {
		Map data = new HashMap();
		data.put("id", id);
		Track track = new Track(en, data);
		return track;
	}

	static Track createTrackFromSong(EchonestAPI en, Map data) throws EchonestException {
		Track track = new Track(en, data);
		return track;
	}

	/**
	 * Gets the title of the track
	 *
	 * @return the title of the track
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getTitle() throws EchonestException {
		return getTopLevelItem("title");
	}

	/**
	 * Gets the artist name for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getArtistName() throws EchonestException {
		return getTopLevelItem("artist");
	}

	/**
	 * Gets the preview url
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getPreviewUrl() throws EchonestException {
		return getTopLevelItem("preview_url");
	}

	/**
	 * Gets the audio url
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getAudioUrl() throws EchonestException {
		return getTopLevelItem("audio_url");
	}

	/**
	 * Gets the release name for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getReleaseName() throws EchonestException {
		return getTopLevelItem("release");
	}

	/**
	 * Gets the MD5 of the audio for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getAudioMD5() throws EchonestException {
		return getTopLevelItem("audio_md5");
	}

	/**
	 * Gets the foreign ID for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getForeignID() throws EchonestException {
		/* return only the track foreign id and not 'partner_name:track:foreign_track_id' */
		return getTopLevelItem("foreign_id").split(":")[2];
	}

	/**
	 * Gets the foreign ID for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getForeignReleaseID() throws EchonestException {

		/* return only the release foreign id and not 'partner_name:release:foreign_release_id' */
		return getTopLevelItem("foreign_release_id").split(":")[2];
	}

	/**
	 * Gets the song ID associated with this track
	 *
	 * @return the song id
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public String getSongID() throws EchonestException {
		return getTopLevelItem("song_id");
	}

	private String getTopLevelItem(String itemName) throws EchonestException {
		return getString(itemName);
	}

	/**
	 * Gets the key for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public int getKey() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getInteger("audio_summary.key");
	}

	/**
	 * Gets the tempo for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public double getTempo() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.tempo");
	}

	/**
	 * Gets the mode for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public int getMode() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getInteger("audio_summary.mode");
	}

	/**
	 * Gets the time signature for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public int getTimeSignature() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getInteger("audio_summary.time_signature");
	}

	/**
	 * Gets the duration for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public double getDuration() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.duration");
	}

	/**
	 * Gets the loudness for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public double getLoudness() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.loudness");
	}

	/**
	 * Gets the energy for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public double getEnergy() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.energy");
	}

	/**
	 * Gets the danceability for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public double getDanceability() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.danceability");
	}

	/**
	 * Gets the speechiness for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public double getSpeechiness() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.speechiness");
	}

	/**
	 * Gets the liveness for the track
	 *
	 * @return
	 * @throws EchonestException
	 * @throws ParseException 
	 * @throws IOException 
	 * @throws ClientProtocolException 
	 */
	public double getLiveness() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.liveness");
	}
}
