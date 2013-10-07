/* Copyright (c) 2013 The Montana Department of Transportation
 * 
 */
package gov.mt.mdt.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;

import com.esri.core.map.FeatureSet;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class OfflineCollectUtils {
	private static final String TAG = "OfflineCollectUtils";
	// stuff for createJsonFile
	private static File demoDataFile;
	private static String offlineDataSDCardDirName;
	protected static String OFFLINE_FILE_EXTENSION = ".json";
	
	/*
	 * Creates a JsonFile with the directory and filename 
	 */
	public static String getJSONFilePath(String filename, String directory) {
		demoDataFile = Environment.getExternalStorageDirectory();
		offlineDataSDCardDirName = directory;

		StringBuilder sb = new StringBuilder();
		sb.append(demoDataFile.getAbsolutePath());
		sb.append(File.separator);
		sb.append(offlineDataSDCardDirName);
		sb.append(File.separator);
		sb.append(filename);
		sb.append(OFFLINE_FILE_EXTENSION);
		
		System.out.println("OfflineCollectUtils: " + sb.toString());
		return sb.toString();
	}
	
	/*
	 * Creates a feature set given a json file
	 */
	public static FeatureSet createDataCollectFeatureSet(String path) {
		FeatureSet fs = null;
		try {
			JsonFactory factory = new JsonFactory();
			JsonParser parser = factory.createJsonParser(new FileInputStream(
					path));
			parser.nextToken();
			fs = FeatureSet.fromJson(parser);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out
				.println("OfflineCollectUtils: featureSet from JSON. " + path);
		return fs;

	}
	
	/** Removed, not used
	public static void setupFeatureSets(FeatureSet updateFS, String filePath,
			SpatialReference spatRef) {
		updateFS = createDataCollectFeatureSet(filePath);
		updateFS.setSpatialReference(spatRef);
	}**/

	/*
	 * Logging function, write to file
	 */
	public static void appendLog(String text, String path) {
		// name the logfile based on day.
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		String fileName = Environment.getExternalStorageDirectory() + path
				+ "log." + df.format(date) + ".txt";
		File logFile = new File(fileName);
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * Checks to see if there is an active network
	 * Does not verify that you are connect to anything!!!
	 */
	public static boolean onNetwork(Context context) {

		final ConnectivityManager conMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
		if (activeNetwork != null && activeNetwork.isConnected()) {
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * checks if the Internet is available.
	 */
	public static boolean isInternetAvailable(Context context){
		final boolean internetStatus = false;
		Thread internetStatusThread = new Thread(new Runnable(){
			
			@Override
			public void run() {
				boolean connect;
				HttpGet httpGet = new HttpGet("http://www.google.com");
			    HttpParams httpParameters = new BasicHttpParams();
			    // Set the timeout in milliseconds until a connection is established.
			    // The default value is zero, that means the timeout is not used.
			    int timeoutConnection = 3000;
			    HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			    // Set the default socket timeout (SO_TIMEOUT)
			    // in milliseconds which is the timeout for waiting for data.
			    int timeoutSocket = 5000;
			    HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		
			    DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			    try{
			        Log.d(TAG, "Checking network connection...");
			        httpClient.execute(httpGet);
			        Log.d(TAG, "Connection OK");
			        //internetStatus = true;
			        //return;
			        connect = true;
			    }
			    catch(ClientProtocolException e){
			        e.printStackTrace();
			        Log.d(TAG, "Connection unavailable");
			    }
			    catch(IOException e){
			        e.printStackTrace();
			        Log.d(TAG, "Connection unavailable");
			    }
		
			    //Log.d(TAG, "Connection unavailable");
			    
			   //internetStatus = false;
			    //return false;
			    //connect = false;
			}
		});
		internetStatusThread.start();
		return true;
	}
	
	
	
	public static boolean localFileExists(String path){
		File file = new File(path);
		if(file.exists()){
			return true;
		}else{
			return false;
		}	
	}
	
	/*
	 * Reads out a file as a string, used to read json from a local file.
	 */
	public static String readFileAsString(String filePath) throws java.io.IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		String line;
		String results = "";
		while((line = reader.readLine()) != null){
			results += line;
		}
		reader.close();
		//System.out.println("Config File: " + results);
		return results;
	}

	/*
	 * Renames the local file with the current time stamp.
	 */
	public static void renameFile(String fileName, String projectPath) {
		String newSitePath = OfflineCollectUtils.getJSONFilePath(fileName,
				projectPath);
		// this.getResources().getString(R.string.config_data_sdcard_offline_dir));
		File fromFile = new File(newSitePath);

		SimpleDateFormat sdate = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String currentDateTimeStamp = sdate.format(new Date());
		File toFile = new File(newSitePath + "." + currentDateTimeStamp
				+ ".json");
		fromFile.renameTo(toFile);

		System.out.println("Renamed File: " + fileName);
	}



}
