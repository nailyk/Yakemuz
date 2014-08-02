package com.yakemuz.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yakemuz.echonestAPI.EchonestAPI;
import com.yakemuz.echonestAPI.EchonestException;

public class Song extends EchonestItem {

	public enum SongType {

		christmas, live, studio
	}

	public enum SongTypeFlag {

		True, False, seed, any
	}

	private final static String PATH = "songs[0]";
	private Map<String, Track> trackMap = new HashMap<String, Track>();

	@SuppressWarnings("unchecked")
	public Song(EchonestAPI en, Map map) throws EchonestException
	{
		super(en, "song", PATH, map);
	}

	public Song(EchonestAPI en, String id) throws EchonestException, IOException
	{
		super(en, "song", PATH, id);
	}

	public String getTitle() {
		return getString("title");
	}

	public String getArtistName() {
		return getString("artist_name");
	}

	public String getArtistID() {
		return getString("artist_id");
	}

	public String getAudio() {
		return getString("audio");
	}

	public String getCoverArt() {
		return getReleaseImage();
	}

	public String getReleaseImage() {
		return getString("release_image");
	}

	public double getSongHotttnesss() throws EchonestException, IOException {
		fetchBucket("song_hotttnesss");
		return getDouble("song_hotttnesss");
	}

	public double getArtistHotttnesss() throws EchonestException, IOException {
		fetchBucket("artist_hotttnesss");
		return getDouble("artist_hotttnesss");
	}

	public double getArtistFamiliarity() throws EchonestException, IOException {
		fetchBucket("artist_familiarity");
		return getDouble("artist_familiarity");
	}

	public double getDuration() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.duration");
	}

	public double getLoudness() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.loudness");
	}

	public double getTempo() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.tempo");
	}

	public double getEnergy() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.energy");
	}

	public double getDanceability() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getDouble("audio_summary.danceability");
	}

	public String getAnalysisURL() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getString("audio_summary.analysis_url");
	}

	public int getTimeSignature() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getInteger("audio_summary.time_signature");
	}

	public int getMode() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getInteger("audio_summary.mode");
	}

	public int getKey() throws EchonestException, IOException {
		fetchBucket("audio_summary");
		return getInteger("audio_summary.key");
	}


	public Track getTrackOld(String idSpace) throws EchonestException, IOException {
		Track track = trackMap.get(idSpace);
		if (track == null) {
			// see if we already have the track data
			List tlist = (List) getObject("tracks");
			if (tlist == null) {
				String[] buckets = {"tracks", "id:" + idSpace};
				fetchBuckets(buckets, true);
				tlist = (List) getObject("tracks");
			}
			for (int i = 0; tlist != null && i < tlist.size(); i++) {
				Map tmap = (Map) tlist.get(i);
				String tidSpace = (String) tmap.get("catalog");
				if (idSpace.equals(tidSpace)) {
					track = new Track(en, tmap);
					trackMap.put(idSpace, track);
					break;
				}
			}
		}
		return track;
	}
}
