package com.yakemuz.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class RetrieveWebPageContent {

	public static String getStringResults(String command) throws IOException
	{
		URL url = new URL(command);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		String result = "";
		InputStream is = null;
		try {
			if (urlConnection.getResponseCode() >= 300)
				is = new BufferedInputStream(urlConnection.getErrorStream());
			else
				is = new BufferedInputStream(urlConnection.getInputStream());
			result = convertStreamToString(is);
		}
		finally {
			urlConnection.disconnect();
		}
		return result;
	}

	public static String convertStreamToString(InputStream is) {

		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static Bitmap getBitmapFromURL(String src) {
		boolean redirect = false; /* il y a une redirection de l'url originale vers une autre url ou est situee la cover */
		try {
			URL url = new URL(src);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			int status = connection.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
						|| status == HttpURLConnection.HTTP_MOVED_PERM
						|| status == 307)
					redirect = true;
			}

			if (redirect) 
			{
				String new_url = connection.getHeaderField("Location");
				connection = (HttpURLConnection) new URL(new_url).openConnection();
			}

			InputStream is = connection.getInputStream();
			/*
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
	        int len=0;
	        byte[] buffer = new byte[1024];
	        while((len = bis.read(buffer)) != -1){
	            out.write(buffer, 0, len);
	        }
	        out.close();
	        bis.close();
	        byte[] data = out.toByteArray();
	        Bitmap cover = BitmapFactory.decodeByteArray(data, 0, data.length);
			 */
			Bitmap cover = BitmapFactory.decodeStream(is);
			/* cover.compress(Bitmap.CompressFormat.PNG, 100); */
			return cover;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Map<?, ?> getMapResults(String command) throws IOException
	{
		String results = RetrieveWebPageContent.getStringResults(command);
		JSONParser parser = new JSONParser();
		try {
			return (JSONObject) parser.parse(results);
		} catch (ParseException e) {
			throw new IOException("Parse Exception", e);
		}
	}
}
