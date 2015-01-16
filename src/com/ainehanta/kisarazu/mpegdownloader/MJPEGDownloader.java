package com.ainehanta.kisarazu.mpegdownloader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

public class MJPEGDownloader {

	public static void main(String[] args) {

		HttpURLConnection connection = null;
		URL url = null;
		byte[] imageBuffer = null;
		int contentLength = 0;
		
		// final String BOUNDARY = "ipcamera";
		final String CONTENT_LENGTH = "Content-Length";
		
		try {
			// Basic認証
			Authenticator.setDefault(new HTTPAuthenticator("admin", "admin"));
			
			url = new URL("http://192.168.0.51:7777/media/?action=stream");
			connection = (HttpURLConnection)url.openConnection();
			
			connection.connect();
			
			int responseCode = connection.getResponseCode();
			if(responseCode == 200)
			{
				String lineBuffer;
				BufferedInputStream bufInput = new BufferedInputStream(connection.getInputStream());
				BufferedReader bufRead = new BufferedReader(new InputStreamReader(bufInput));
				
				while(true)
				{
					lineBuffer = bufRead.readLine();
					System.out.println(lineBuffer);
					if(lineBuffer.startsWith(CONTENT_LENGTH))
					{
						lineBuffer = lineBuffer.split(":")[1];
						lineBuffer = lineBuffer.trim();
						contentLength = Integer.parseInt(lineBuffer);
						System.out.println("Content-Length: " + contentLength);
						
						//空行を読み飛ばす 
						bufRead.readLine();
						break;
					}
				}
				
				if(contentLength > 0)
				{
					imageBuffer = new byte[contentLength];
					for(int i=0;i < contentLength; i++)
					{
						imageBuffer[i] = (byte)bufInput.read();
					}
				}
				
				bufInput.close();
				bufRead.close();
				
				if(imageBuffer != null)
				{
					FileOutputStream fileOutput = new FileOutputStream("sample.jpg");
					fileOutput.write(imageBuffer);
					fileOutput.close();
				}
				
				System.out.println("Done.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(connection != null)
			{
				connection.disconnect();
			}
		}
	}
	
	static class HTTPAuthenticator extends Authenticator {
		private String username, password;

		public HTTPAuthenticator(String user, String pass) {
			username = user;
			password = pass;
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			System.out.println("Requesting Host  : " + getRequestingHost());
			System.out.println("Requesting Port  : " + getRequestingPort());
			System.out.println("Requesting Prompt : " + getRequestingPrompt());
			System.out.println("Requesting Protocol: "
					+ getRequestingProtocol());
			System.out.println("Requesting Scheme : " + getRequestingScheme());
			System.out.println("Requesting Site  : " + getRequestingSite());
			return new PasswordAuthentication(username, password.toCharArray());
		}
	}
}
