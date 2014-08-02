package com.yakemuz.model;

import java.util.Map;

import com.yakemuz.echonestAPI.EchonestAPI;
import com.yakemuz.echonestAPI.EchonestException;

public class Artist extends EchonestItem {

	Artist(EchonestAPI en, String type, String path, Map<String, Object> data)
			throws EchonestException {
		super(en, type, path, data);
		// TODO Auto-generated constructor stub
	}

}
