package com.yakemuz.echonestAPI;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.yakemuz.model.Song;

public class EchonestAPI {

	private Commander cmd;
	private Params std_params;
	public static final String SERVER_URL = "http://developer.echonest.com/api/v4/";

	public EchonestAPI()
	{
		this.cmd = new Commander();
		this.std_params = new Params();
		this.std_params.add("api_key", "LY0FOZBHENDWNWDIE");
		this.cmd.setStdParams(this.std_params);
	}

	public Commander getCmd() {
		return cmd;
	}

	public Song identifySong(String fp_code) throws EchonestException, IOException
	{
		Params p = new Params();
		p.add("version", "4.12");
		p.add("code", fp_code);
		p.add("bucket", "tracks");
		p.add("bucket", "id:deezer");
		p.add("bucket", "id:spotify");
		p.add("bucket", "id:whosampled");
		Map<?, ?> results = cmd.sendCommand("song/identify", p);
		Map<?, ?> response = (Map<?, ?>) results.get("response");
		List<?> songs = (List<?>) response.get("songs");
		if (songs.isEmpty())
			throw new EchonestException("No match. This track may not be in the database");
		return new Song(this, (Map<?, ?>) songs.get(0));
	}
}
