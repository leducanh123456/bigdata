package com.neo.cohgw.bs;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VASRequest {
	private final Logger logger = LoggerFactory.getLogger(VASRequest.class);
	private Map<String, String> parameter = null;
	private int timeOut = 2000;
	

	/**
	 * @param parameter
	 * @param cacheValue
	 */
	public VASRequest(Map<String, String> parameter) {
		this.parameter = parameter;
	}

	public String send(String url_api) {
		return send(getRequest(), url_api);
	}

	public String send(String request, String url_api) {
		String v = "";
		StringBuilder response = new StringBuilder();
		try {
			URL url = new URL(url_api);
			URLConnection connection = url.openConnection();
			HttpURLConnection httpConn = (HttpURLConnection) connection;
			httpConn.setConnectTimeout(timeOut);

			httpConn.setRequestMethod("POST");
			httpConn.setRequestProperty("Content-Length", String.valueOf(request.length()));
			// httpConn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
			// httpConn.setRequestProperty( "charset", "utf-8");

			httpConn.setRequestProperty("Content-Type", "application/xml; charset=utf-8");

			httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			httpConn.setUseCaches(false);

			OutputStream out = httpConn.getOutputStream();
			out.write(request.getBytes());
			out.flush();
			out.close();

			if (httpConn.getResponseCode() == 200) {
				InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
				BufferedReader in = new BufferedReader(isr);

				String value = null;
				while ((value = in.readLine()) != null) {
					response.append(value);
				}
				in.close();
				v = response.toString();
			} else {
				v = "ERROR_CODE:" + httpConn.getResponseCode();
			}
		} catch (Exception e) {
			v = "ERROR_TIMEOUT:" + e.toString();
			logger.info("time out  : {}",e);
		}
		return v;
	}

	public String getRequest() {
		if (this.parameter == null || this.parameter.isEmpty()) {
			return null;
		}
		StringBuilder request = new StringBuilder();
		request.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns=\"http://vasplatform.mobifone.vn\"><soapenv:Header/><soapenv:Body><request>");		
		request.append(this.getBodyRequest());
		request.append("</request></soapenv:Body></soapenv:Envelope>");
		return request.toString();
	}

	public String getBodyRequest() {
		StringBuilder request = new StringBuilder();
		// add parameter 
		request.append("<Parameters>");
		for (Map.Entry<String, String> param : this.parameter.entrySet())
			request.append("<Parameter><name>").append(param.getKey()).append("</name><value>").append(param.getValue()).append("</value></Parameter>");
		request.append("</Parameters>");
		return request.toString();
	}

	/**
	 * @return the parameter
	 */
	public Map<String, String> getParameter() {
		return parameter;
	}

	/**
	 * @param parameter
	 *            the parameter to set
	 */
	public void setParameter(Map<String, String> parameter) {
		this.parameter = parameter;
	}
}