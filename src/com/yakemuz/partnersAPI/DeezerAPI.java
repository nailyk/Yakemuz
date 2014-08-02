package com.yakemuz.partnersAPI;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;

import com.yakemuz.echonestAPI.EchonestException;
import com.yakemuz.util.RetrieveWebPageContent;

public class DeezerAPI {

	public static final String SERVER_URL = "http://api.deezer.com/";


	@SuppressWarnings("unchecked")
	public Map<String,String> getTrackInfos(String track_id) throws IOException
	{
		String url = SERVER_URL + "track/" + track_id;
		JSONObject results = (JSONObject) RetrieveWebPageContent.getMapResults(url);
		Map<String, String> map = new HashMap<String, String>();
		Map<String, String> album = new HashMap<String, String>();
		map.put("link_deezer", (String) results.get("link"));
		album = (Map<String, String>) results.get("album");
		map.put("release_name", album.get("title"));
		map.put("release_date", album.get("release_date"));
		map.put("cover", album.get("cover"));
		return map;
	}

	public Bitmap getTrackReleaseCover(String url_cover) throws ClientProtocolException, EchonestException, IOException, ParseException, XPathExpressionException, SAXException, ParserConfigurationException 
	{
		Bitmap cover = RetrieveWebPageContent.getBitmapFromURL(url_cover + "&size=big");
		return cover;
	}
}
