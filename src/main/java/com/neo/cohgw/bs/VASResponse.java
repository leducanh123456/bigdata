package com.neo.cohgw.bs;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")

public class VASResponse {
	private String status = null;
	private String statusMessage = null;
	private Map<String, String> parameter = null;
	private String response = null;
	
	/**
	 * @param response
	 */
	public VASResponse(String response) {
		this.response = response;
	}

	/**
	 * @return the parameter
	 */
	public Map<String, String> getParameter() {
		if (this.parameter == null) {
			this.parameter = extractParameters();
		}
		return parameter;
	}


	private String extractValue(String key) {
		String tag = "<" + key + ">";
		if (response.indexOf(tag) > 0) {
			String tagEnd = "</" + key + ">";
			return response.substring(response.indexOf(tag) + tag.length(), response.indexOf(tagEnd));
		} else {
			String regex = "<*:" + key + ">";
			Pattern pattern = Pattern.compile(regex);
			Matcher m = pattern.matcher(response);
			if (m.find()) {
				int start = m.end();
				m.find();
				int end = m.start();
				String a = response.substring(start, end);
				return a.substring(0, a.indexOf("<"));
			}
		}
		return null;
	}

	private Map<String, String> extractParameters() {
		// check exist Parameters in the response
		if (response.indexOf("Parameters>") > 0) {
			Map<String, String> map = new HashMap<String, String>();

			// get value of namespace
			String namespace = null;
			if (response.indexOf(":Parameters>") > 0) {
				String regex = "<*:Parameters>";
				Pattern pattern = Pattern.compile(regex);
				Matcher m = pattern.matcher(response);
				if (m.find()) {
					int start = m.start();
					String beforeStart = response.substring(0, start);
					namespace = response.substring(beforeStart.lastIndexOf("<") + 1, start + 1);
				} else
					return null;
			}
			String contentParams = response.substring(response.indexOf("<"+ namespace+"Parameters>") + ("<"+ namespace+"Parameters>").length(), response.indexOf("</"+ namespace+"Parameters>"));
			String regex = "<"+namespace+"Parameter>";
			Pattern pattern = Pattern.compile(regex);
			Matcher m = pattern.matcher(contentParams);
			while (m.find()) {
				int start = m.start();
				String name = contentParams.substring(contentParams.indexOf("<"+ namespace+"name>", start) + ("<"+ namespace+"name>").length(), contentParams.indexOf("</"+ namespace+"name>", start));
				String value = contentParams.substring(contentParams.indexOf("<"+ namespace+"value>", start) + ("<"+ namespace+"value>").length(), contentParams.indexOf("</"+ namespace+"value>", start));
				map.put(name, value);
			}
			return map;
		}

		return null;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		if (this.status == null) {
			this.status = extractValue("STATUS");
		}
		return status;
	}


	/**
	 * @return the statusMessage
	 */
	public String getStatusMessage() {
		if (this.statusMessage == null) {
			this.statusMessage = extractValue("STATUS_MESSAGE");
		}
		return statusMessage;
	}
}