/*
 * Created on Jan 16, 2015 by nadirayasmeen
 * 
 * Copyright(c) 2000-2010 Hannon Hill Corporation.  All rights reserved.
 */
package com.hannonhill.emailtrigger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.log4j.Logger;

public class WebService {

    private static final Logger LOG = Logger.getLogger(WebService.class);
    
	public static String httpGet(String urlStr) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		if (conn.getResponseCode() != 200) {
			throw new IOException(conn.getResponseMessage());
		}

		// Buffer the result into a string
		BufferedReader rd = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line);
		}
		rd.close();

		conn.disconnect();
		return sb.toString();
	}
	public static String httpPost(String urlStr, String parameters)
			throws Exception {

		StringBuffer response = new StringBuffer();
		try {
		URL url = new URL(urlStr);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setDoInput(true);
			conn.setAllowUserInteraction(true);
			conn.setRequestProperty("Content-Type", "application/json");

			 OutputStream out = conn.getOutputStream();
			 out.write(parameters.getBytes());
			 out.flush();
			 out.close();
				if(conn.getResponseCode() == 206){
					//If 206?? Already exists?? Update
					
						
					}
				
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Email Creation Failed : HTTP error code : "
						+ conn.getResponseCode() + " "
						+ conn.getResponseMessage() + ": " + conn.getURL());

			}

			BufferedReader in = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));

			while (in.readLine() != null) {
				response.append(in.readLine());
			}
			
			LOG.debug("Email successfully created.");
			in.close();
			conn.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();
	}
}
