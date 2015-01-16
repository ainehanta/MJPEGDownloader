package com.ainehanta.kisarazu.mpegdownloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

public class MJPEGDownloader {

	public static void main(String[] args) {

		HttpURLConnection connection = null;
		URL url = null;
		
		byte[] responseBuffer = new byte[30000];
		byte[] imageBuffer = new byte[30000];
		
		try {
			// Basic認証
			Authenticator.setDefault(new HTTPAuthenticator("admin", "admin"));
			
			url = new URL("http://192.168.0.51:7777/media/?action=stream");
			connection = (HttpURLConnection)url.openConnection();
			connection.connect();
			
			int responseCode = connection.getResponseCode();
			if(responseCode == 200)
			{
				BufferedInputStream bufInput = new BufferedInputStream(connection.getInputStream());
				
				for(int i=0;i < responseBuffer.length;i++)
				{
					responseBuffer[i] = (byte)(bufInput.read() & (byte)0xff);
				}
				
				bufInput.close();
				
				boolean startFlag = false;
				int imageBufferCount = 0;
				for(int i=0;i < responseBuffer.length;i++)
				{
					if(responseBuffer[i] == (byte)0xff)
					{
						if(responseBuffer[i+1] == (byte)0xd8)
						{
							startFlag = true;
							System.out.println("Start.");
						}
					}
					
					if(startFlag == true)
					{
						imageBuffer[imageBufferCount++] = responseBuffer[i];
						if(responseBuffer[i] == (byte)0xff)
						{
							if(responseBuffer[i+1] == (byte)0xd9)
							{
								imageBuffer[imageBufferCount++] = responseBuffer[i+1];
								System.out.println("End.");
								break;
							}
						}
					}
				}
				
				FileOutputStream fileOutput = new FileOutputStream("sample.jpg");
				fileOutput.write(imageBuffer, 0, imageBufferCount);
				
				fileOutput.close();
				
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
