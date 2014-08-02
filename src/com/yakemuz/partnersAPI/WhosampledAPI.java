package com.yakemuz.partnersAPI;

import java.io.IOException;

public class WhosampledAPI {

	public static final String SERVER_URL = "http://www.whosampled.com/";

	public String getLink(String track_id) throws IOException
	{
		return SERVER_URL + "track/view/" + track_id;
	}
}
