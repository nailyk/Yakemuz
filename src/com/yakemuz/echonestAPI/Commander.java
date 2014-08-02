package com.yakemuz.echonestAPI;

import java.io.IOException;
import java.util.Map;
import com.yakemuz.util.RetrieveWebPageContent;

public class Commander {

	private Params std_params;

	public Commander()
	{
		this.std_params = new Params();
	}

	public Map<?, ?> sendCommand(String command, Params params) throws EchonestException, IOException
	{
		StringBuilder str_url = new StringBuilder();
		str_url.append(EchonestAPI.SERVER_URL);
		str_url.append(command);
		str_url.append(params.toString(true));
		str_url.append(this.std_params.toString(params.size() == 0));
		Map<?, ?> results = getCheckedResults(str_url.toString());
		return results;
	}

	private Map<?, ?> getCheckedResults(String command) throws EchonestException, IOException
	{
		Map<?, ?> results = null;
		results = RetrieveWebPageContent.getMapResults(command);
		checkStatus(results);
		return results;
	}

	private void checkStatus(Map<?, ?> results) throws EchonestException {
		Map<?, ?> response = (Map<?, ?>) results.get("response");
		Map<?, ?> status = (Map<?, ?>) response.get("status");
		String version = (String) status.get("version");
		String message = (String) status.get("message");
		Long scode = (Long) status.get("code");

		int code = scode.intValue();

		if (!version.startsWith("4.")) {
			throw new EchonestException(
					EchonestException.CLIENT_SERVER_INCONSISTENCY,
					"Unexpected API version number");
		}

		if (code != 0) {
			throw new EchonestException(code, message);
		}
	}

	public void setStdParams(Params std_params) {
		this.std_params = std_params;
	}

	public Params getStdParams() {
		return this.std_params;
	}
}
