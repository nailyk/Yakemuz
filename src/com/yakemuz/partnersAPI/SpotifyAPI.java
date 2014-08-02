package com.yakemuz.partnersAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.yakemuz.util.RetrieveWebPageContent;

public class SpotifyAPI {

	public static final String SERVER_URL = "http://ws.spotify.com/";

	@SuppressWarnings("unchecked")
	public Map<String,String> getTrackInfos(String track_id) throws IOException
	{
		String url = SERVER_URL + "lookup/1/.json?uri=spotify:track:" + track_id;
		JSONObject results = (JSONObject) RetrieveWebPageContent.getMapResults(url);
		results = (JSONObject) results.get("track");
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> album = new HashMap<String, String>();
		map.put("link_spotify", "http://open.spotify.com/track/" + track_id);
		album = (Map<String, String>) results.get("album");
		map.put("release_name", album.get("name"));
		map.put("release_date", album.get("released"));
		return map;
	}
}