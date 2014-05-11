package com.volcanicplaza.Minetrends;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.bukkit.Bukkit;
import org.codehaus.jackson.map.ObjectMapper;

public class sendRunnable implements Runnable {

	@SuppressWarnings("unused")
	@Override
	public void run() {
		URL url = null;
		String data = null;
		long startTime = 0;
		HttpURLConnection conn = null;
		
		//System.out.println(Minetrends.getData());
		
		 String urlParameters = "key=" + Minetrends.publicKey + "&data=" + Minetrends.getData();
		  try {
			  url = new URL(Minetrends.hostname + ":81");
			  
		  } catch (Exception ex){
			  ex.printStackTrace();
		  }
		
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST"); //use post method
			conn.setDoOutput(true); //we will send stuff
			conn.setDoInput(true); //we want feedback
			conn.setUseCaches(false); //no caches
			conn.setConnectTimeout(4000); //set timeout
			conn.setInstanceFollowRedirects(true);
			conn.setAllowUserInteraction(false);
			conn.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
			conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
		
			// Open a stream which can write to the URL******************************************
			DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
			
			startTime = System.currentTimeMillis();
			// Open a stream which can read the server response*********************************
			InputStream in = conn.getInputStream();
			BufferedReader rd  = new BufferedReader(new InputStreamReader(in));
			String responseSingle;
			while ((responseSingle = rd.readLine()) != null) {
			String response = null;
			response = response + responseSingle;
			}
			String response = rd.readLine();
			
			if (response != null){
				ObjectMapper mapper = new ObjectMapper();
				ResponseProtocol responseObj = mapper.readValue(response, ResponseProtocol.class);
				if (responseObj.getRESPONSE_CODE() != 200) {
					//Response code isn't normal.
					Minetrends.plugin.getLogger().info("Error: <" + responseObj.getRESPONSE_CODE() + "> " + responseObj.getRESPONSE_MESSAGE());
				}
			}
			
			rd.close(); //close the reader
			//System.out.println("Response: " + response);
			if (in != null) {
				in.close();
			}
		} catch (SocketTimeoutException e) {
			Bukkit.getLogger().warning("<Minetrends> Unable to send data to Minetrends. Connection timed out.");
		} catch (IOException e) {
			Bukkit.getLogger().warning("Unkonwn Minetrends Error! Please report!");
			e.printStackTrace();
		} finally { //in this case, we are ensured to close the connection itself
			if (conn != null)
			conn.disconnect();
		}
		long endTime = System.currentTimeMillis();
		long duration = endTime - startTime;
		//System.out.println("Took " + duration + " ms.");
		//Done communicating with server
		}


}

