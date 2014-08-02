package com.yakemuz.partnersAPI;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;

import com.yakemuz.echonestAPI.EchonestException;
import com.yakemuz.util.RetrieveWebPageContent;

public class MusicbrainzAPI {

	public static final String SERVER_URL = "http://musicbrainz.org/ws/2/";

	public Map<String,String> getTrackReleaseInfos(String release_id) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException
	{
		String url = SERVER_URL + "release/" + release_id;
		String results = RetrieveWebPageContent.getStringResults(url);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(results)));
		XPath xpath = XPathFactory.newInstance().newXPath();

		Map<String, String> map = new HashMap<String,String>();
		XPathExpression expr = xpath.compile("/metadata/release/title");
		map.put("release_name", expr.evaluate(document));
		expr = xpath.compile("/metadata/release/date");
		map.put("release_date", expr.evaluate(document));
		expr = xpath.compile("/metadata/release/cover-art-archive/front");
		map.put("cover", expr.evaluate(document));
		return map;
	}

	public Bitmap getTrackReleaseCover(String track_release_id) throws ClientProtocolException, EchonestException, IOException, ParseException, XPathExpressionException, SAXException, ParserConfigurationException 
	{
		String url_cover = "http://coverartarchive.org/release/" + track_release_id + "/front";
		Bitmap cover = RetrieveWebPageContent.getBitmapFromURL(url_cover);
		return cover;
	}
}
